package com.roberto.gestorpro.data.repository

import android.util.Log
import com.roberto.gestorpro.data.dao.MovimientoDao
import com.roberto.gestorpro.data.firebase.ClienteRemotoRepository
import com.roberto.gestorpro.data.entity.MovimientoEntity
import com.roberto.gestorpro.model.EstadoMovimiento
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * MovimientoRepository.kt
 * -----------------------
 * ✔ TIPO: archivo de código fuente Kotlin (repositorio de datos)
 * Es el archivo que define la capa de repositorio de movimientos (servicios).
 * Sirve para separar la interfaz de usuario del acceso directo a la base de datos de movimientos.
 */

/**
 * MovimientoRepository
 * --------------------
 * ✔ TIPO: clase (repositorio de datos, inyectada con Hilt)
 * Es la capa que separa la interfaz de usuario del acceso directo a la base de datos.
 * Sirve para centralizar las operaciones con movimientos usando MovimientoDao internamente.
 * Se inyecta automáticamente con Hilt gracias a la anotación @Inject.
 */
@Singleton
class MovimientoRepository @Inject constructor(

    /**
     * movimientoDao
     * -------------
     * ✔ TIPO: parámetro (param) → MovimientoDao
     * Es el DAO de movimientos que recibirá el repositorio.
     * Sirve para que el repositorio acceda a la base de datos a través de las operaciones del DAO.
     */
    private val movimientoDao: MovimientoDao,
    private val clienteRemotoRepository: ClienteRemotoRepository
){

    private val sincronizacionMutex = Mutex()
    private val _periodosPendientes = MutableStateFlow<Set<Int>>(emptySet())
    val periodosPendientes: StateFlow<Set<Int>> = _periodosPendientes.asStateFlow()

    private val _errorSincronizacion = MutableStateFlow<String?>(null)
    val errorSincronizacion: StateFlow<String?> = _errorSincronizacion.asStateFlow()

    companion object {
        private const val TAG = "MovimientoRepository"
    }

    /**
     * insertarMovimiento
     * ------------------
     * ✔ TIPO: método (fun) suspend de Kotlin
     * Es la operación que inserta un movimiento en la base de datos a través del DAO.
     * Sirve para guardar un nuevo MovimientoEntity desde la capa de repositorio.
     */
    suspend fun insertarMovimiento(movimiento: MovimientoEntity) {
        ejecutarPersistenciaYPeriodo(movimiento.idCliente, "INSERTAR") {
            movimientoDao.insertarMovimiento(movimiento)
        }
    }

    /**
     * actualizarMovimiento
     * --------------------
     * ✔ TIPO: método (fun) suspend de Kotlin
     * Es la operación que actualiza los datos de un movimiento ya existente.
     * Sirve para guardar los cambios de un MovimientoEntity a través del DAO.
     */
    suspend fun actualizarMovimiento(movimiento: MovimientoEntity) {
        ejecutarPersistenciaYPeriodo(movimiento.idCliente, "ACTUALIZAR") {
            movimientoDao.actualizarMovimiento(movimiento)
        }
    }

    /**
     * eliminarMovimiento
     * ------------------
     * ✔ TIPO: método (fun) suspend de Kotlin
     * Es la operación que elimina un movimiento de la base de datos.
     * Sirve para borrar un MovimientoEntity a través del DAO.
     */
    suspend fun eliminarMovimiento(movimiento: MovimientoEntity) {
        ejecutarPersistenciaYPeriodo(movimiento.idCliente, "ELIMINAR") {
            movimientoDao.eliminarMovimiento(movimiento)
        }
    }

    /**
     * Recalcula el periodo usando el contenido ya persistido en Room y lo
     * replica sin depender de que exista una pantalla observando movimientos.
     */
    suspend fun sincronizarPeriodoActual(idCliente: Int) {
        withContext(NonCancellable + Dispatchers.IO) {
            sincronizarPeriodoActualInterno(idCliente, "RECONCILIAR")
        }
    }

    private suspend fun ejecutarPersistenciaYPeriodo(
        idCliente: Int,
        operacion: String,
        persistir: suspend () -> Unit
    ) {
        // La escritura local y el disparo de la réplica sobreviven al cierre de
        // la pantalla que inició la operación.
        withContext(NonCancellable + Dispatchers.IO) {
            persistir()
            sincronizarPeriodoActualInterno(idCliente, operacion)
        }
    }

    private suspend fun sincronizarPeriodoActualInterno(
        idCliente: Int,
        operacion: String
    ) {
        sincronizacionMutex.withLock {
            try {
                val movimientos = movimientoDao
                    .obtenerMovimientosPorCliente(idCliente)
                    .first()
                val actual = movimientos.maxByOrNull { it.fechaFin }

                Log.d(
                    TAG,
                    "Sincronizando periodo: idCliente=$idCliente " +
                        "operacion=$operacion " +
                        "fechaInicioActual=${actual?.fechaInicio} " +
                        "fechaFinActual=${actual?.fechaFin}"
                )

                val resultado = clienteRemotoRepository.actualizarPeriodoActualRemoto(
                    idCliente = idCliente,
                    fechaInicioActual = actual?.fechaInicio,
                    fechaFinActual = actual?.fechaFin
                )

                if (resultado.exito) {
                    _periodosPendientes.value =
                        _periodosPendientes.value - idCliente
                    _errorSincronizacion.value = null
                    Log.i(
                        TAG,
                        "Periodo sincronizado: idCliente=$idCliente resultado=OK"
                    )
                } else {
                    _periodosPendientes.value =
                        _periodosPendientes.value + idCliente
                    _errorSincronizacion.value =
                        "Cliente $idCliente: ${resultado.mensaje}"
                    Log.w(
                        TAG,
                        "Periodo pendiente: idCliente=$idCliente " +
                            "resultado=ERROR mensaje=${resultado.mensaje}"
                    )
                }
            } catch (e: Exception) {
                _periodosPendientes.value =
                    _periodosPendientes.value + idCliente
                _errorSincronizacion.value =
                    "Cliente $idCliente: ${e.message ?: "error inesperado"}"
                Log.e(
                    TAG,
                    "Error calculando/sincronizando periodo: idCliente=$idCliente",
                    e
                )
            }
        }
    }

    /**
     * obtenerMovimientosPorCliente
     * ----------------------------
     * ✔ TIPO: método (fun) de Kotlin → Flow<List<MovimientoEntity>>
     * Es la operación que recupera todos los movimientos de un cliente concreto.
     * Sirve para obtener la lista de servicios de un cliente de forma reactiva
     * desde la capa de repositorio.
     */
    fun obtenerMovimientosPorCliente(idCliente: Int): Flow<List<MovimientoEntity>> {
        return movimientoDao.obtenerMovimientosPorCliente(idCliente)
    }

    /**
     * obtenerMovimientoPorId
     * ----------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → MovimientoEntity?
     * Es la operación que recupera un movimiento buscándolo por su ID.
     * Sirve para obtener un MovimientoEntity concreto (o null si no existe)
     * a través del DAO desde la capa de repositorio.
     */
    suspend fun obtenerMovimientoPorId(idMovimiento: Int): MovimientoEntity? {
        return movimientoDao.obtenerMovimientoPorId(idMovimiento)
    }

    /**
     * obtenerMovimientosPorEstado
     * ---------------------------
     * ✔ TIPO: método (fun) de Kotlin → Flow<List<MovimientoEntity>>
     * Es la operación que recupera los movimientos filtrados por su estado.
     * Sirve para obtener la lista de MovimientoEntity con un EstadoMovimiento concreto
     * de forma reactiva a través del DAO desde la capa de repositorio.
     */
    fun obtenerMovimientosPorEstado(
        estado: EstadoMovimiento
    ): Flow<List<MovimientoEntity>> {
        return movimientoDao.obtenerMovimientosPorEstado(estado)
    }

    /**
     * obtenerTodosLosMovimientos
     * --------------------------
     * ✔ TIPO: método (fun) de Kotlin → Flow<List<MovimientoEntity>>
     * Es la operación que recupera todos los movimientos guardados en la base de datos.
     * Sirve para obtener la lista completa de servicios de todos los clientes
     * de forma reactiva desde la capa de repositorio.
     */
    fun obtenerTodosLosMovimientos(): Flow<List<MovimientoEntity>> {
        return movimientoDao.obtenerTodosLosMovimientos()
    }
}
