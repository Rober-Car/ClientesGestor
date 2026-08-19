package com.roberto.gestorpro.data.repository

import com.roberto.gestorpro.data.dao.MovimientoDao
import com.roberto.gestorpro.data.entity.MovimientoEntity
import com.roberto.gestorpro.model.EstadoMovimiento
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

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
class MovimientoRepository @Inject constructor(

    /**
     * movimientoDao
     * -------------
     * ✔ TIPO: parámetro (param) → MovimientoDao
     * Es el DAO de movimientos que recibirá el repositorio.
     * Sirve para que el repositorio acceda a la base de datos a través de las operaciones del DAO.
     */
    private val movimientoDao: MovimientoDao
){

    /**
     * insertarMovimiento
     * ------------------
     * ✔ TIPO: método (fun) suspend de Kotlin
     * Es la operación que inserta un movimiento en la base de datos a través del DAO.
     * Sirve para guardar un nuevo MovimientoEntity desde la capa de repositorio.
     */
    suspend fun insertarMovimiento(movimiento: MovimientoEntity) {
        movimientoDao.insertarMovimiento(movimiento)
    }

    /**
     * actualizarMovimiento
     * --------------------
     * ✔ TIPO: método (fun) suspend de Kotlin
     * Es la operación que actualiza los datos de un movimiento ya existente.
     * Sirve para guardar los cambios de un MovimientoEntity a través del DAO.
     */
    suspend fun actualizarMovimiento(movimiento: MovimientoEntity) {
        movimientoDao.actualizarMovimiento(movimiento)
    }

    /**
     * eliminarMovimiento
     * ------------------
     * ✔ TIPO: método (fun) suspend de Kotlin
     * Es la operación que elimina un movimiento de la base de datos.
     * Sirve para borrar un MovimientoEntity a través del DAO.
     */
    suspend fun eliminarMovimiento(movimiento: MovimientoEntity) {
        movimientoDao.eliminarMovimiento(movimiento)
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
