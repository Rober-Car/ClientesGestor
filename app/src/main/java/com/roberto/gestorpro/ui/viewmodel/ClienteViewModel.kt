/* ============================================================
 * ============ BLOQUE 1: IMPORTS =============================
 * ============================================================ */
package com.roberto.gestorpro.ui.viewmodel

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.gestorpro.data.entity.ClienteEntity
import com.roberto.gestorpro.data.entity.ServicioEntity
import com.roberto.gestorpro.data.entity.toCliente
import com.roberto.gestorpro.data.firebase.BajaClienteRemotoRepository
import com.roberto.gestorpro.data.firebase.ClienteRemotoRepository
import com.roberto.gestorpro.data.repository.ClienteRepository
import com.roberto.gestorpro.data.repository.MovimientoRepository
import com.roberto.gestorpro.data.repository.ReservaRepository
import com.roberto.gestorpro.data.repository.ServicioRepository
import com.roberto.gestorpro.model.Cliente
import com.roberto.gestorpro.model.EstadoCliente
import com.roberto.gestorpro.util.IdCliente
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/* ============================================================
 * ============ BLOQUE 2: DOCUMENTACIÓN DEL ARCHIVO ===========
 * ============================================================ */
/**
 * ClienteViewModel.kt
 * -------------------
 * ✔ TIPO: archivo de código fuente Kotlin (ViewModel)
 * Es el archivo que define el ViewModel encargado de la lógica de los clientes.
 * Sirve para conectar la interfaz de usuario con el repositorio de clientes.
 */

/* ============================================================
 * ============ BLOQUE 3: VIEWMODEL DE CLIENTES ===============
 * ============================================================ */
/**
 * @HiltViewModel
 * --------------
 * ✔ TIPO: anotación (dagger.hilt.android.lifecycle.HiltViewModel)
 * Es la anotación que marca esta clase como ViewModel inyectable de Hilt.
 * Sirve para que Hilt construya el ViewModel y le inyecte sus dependencias automáticamente.
 */

/**
 * ClienteViewModel
 * ----------------
 * ✔ TIPO: clase (ViewModel de Android)
 * Es el ViewModel que gestiona los datos de los clientes en la pantalla.
 * Sirve para que la interfaz sobreviva a cambios de configuración
 * y obtenga los datos de clientes a través del repositorio.
 */
@HiltViewModel
class ClienteViewModel @Inject constructor(
    private val clienteRepository: ClienteRepository,
    private val movimientoRepository: MovimientoRepository,
    private val clienteRemotoRepository: ClienteRemotoRepository,
    private val servicioRepository: ServicioRepository,
    private val reservaRepository: ReservaRepository,
    private val bajaClienteRemotoRepository: BajaClienteRemotoRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ClienteViewModel"
    }

    /* ============================================================
     * ============ BLOQUE 4: ESTADO DEL VIEWMODEL ================
     * ============================================================ */
    /**
     * _errorSincronizacion / errorSincronizacion
     * ------------------------------------------
     * ✔ TIPO: propiedad (private val) → MutableStateFlow<String?> y (val) → StateFlow<String?>
     * Es el mensaje del último fallo de réplica Room → Firestore.
     * Sirve para informar al ADMIN sin revertir el cambio local y para saber
     * cuándo ofrecer el reintento manual de sincronización.
     */
    private val _errorSincronizacion = MutableStateFlow<String?>(null)
    val errorSincronizacion = _errorSincronizacion.asStateFlow()

    /**
     * _clienteSinSincronizar / clienteSinSincronizar
     * ----------------------------------------------
     * ✔ TIPO: propiedad (private val) → MutableStateFlow<ClienteEntity?> y (val) → StateFlow<ClienteEntity?>
     * Es la ficha local que quedó pendiente de replicar tras un fallo remoto.
     * Sirve para poder reintentar la operación exacta (alta o edición).
     */
    private val _clienteSinSincronizar = MutableStateFlow<ClienteEntity?>(null)
    val clienteSinSincronizar = _clienteSinSincronizar.asStateFlow()

    /**
     * _sincronizacionPendienteEsAlta
     * ------------------------------
     * ✔ TIPO: propiedad (private val) → MutableStateFlow<Boolean>
     * Indica si la réplica pendiente es un alta (true) o una edición (false).
     * Sirve al reintento para llamar a la operación remota correcta.
     */
    private val _sincronizacionPendienteEsAlta = MutableStateFlow(false)

    /**
     * _sincronizacionPendienteServicios
     * ----------------------------------
     * Indica si la réplica pendiente es la de SOLO los servicios contratados
     * (true) en lugar de un alta/edición completa (false). Sirve al reintento
     * para llamar a la operación remota correcta.
     */
    private val _sincronizacionPendienteServicios = MutableStateFlow(false)
    /**
     * _error
     * ------
     * ✔ TIPO: propiedad (private val) → MutableStateFlow<String?>
     * Es el flujo privado de errores del ViewModel, mutable solo dentro de la clase.
     * Sirve para guardar el mensaje de error actual y cambiarlo cuando ocurre una excepción.
     */
    private val _error = MutableStateFlow<String?>(null)

    /**
     * error
     * -----
     * ✔ TIPO: propiedad (val) → StateFlow<String?>
     * Es la versión de solo lectura del flujo _error.
     * Sirve para que la interfaz observe el mensaje de error en tiempo real y lo muestre al usuario.
     */
    val error = _error.asStateFlow()

    private val _clienteSeleccionado = MutableStateFlow<Cliente?>(null)

    val clienteSeleccionado = _clienteSeleccionado.asStateFlow()

    /**
     * _clienteEditando
     * ----------------
     * ✔ TIPO: propiedad privada (val) → MutableStateFlow<ClienteEntity?>
     * Es el flujo que guarda el cliente que se está editando en el formulario.
     * Sirve para conservar en memoria los datos originales del cliente mientras se editan.
     */
    private val _clienteEditando = MutableStateFlow<ClienteEntity?>(null)

    /**
     * clienteEditando
     * ---------------
     * ✔ TIPO: propiedad (val) → StateFlow<ClienteEntity?>
     * Es la versión pública e inmutable de _clienteEditando.
     * Sirve para que la pantalla de modificar cliente observe y precargue los datos del cliente.
     */
    val clienteEditando = _clienteEditando.asStateFlow()

    /**
     * clientes
     * --------
     * ✔ TIPO: propiedad (val) → StateFlow<List<Cliente>>
     * Es el flujo de clientes convertido en StateFlow con stateIn(),
     * transformando cada ClienteEntity en Cliente con toCliente().
     * Sirve para que la interfaz observe en tiempo real la lista de clientes de la base de datos,
     * usando emptyList() como valor inicial mientras los datos cargan.
     */
    val clientes = clienteRepository.obtenerClientesRepo()
        .map { lista ->
            lista.map { it.toCliente() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * morososIds
     * ----------
     * ✔ TIPO: propiedad (val) → StateFlow<Set<Int>>
     * Es el flujo que contiene los IDs de los clientes que son morosos.
     * Sirve para que la lista de clientes pueda filtrar y marcar visualmente
     * a los clientes morosos calculando su estado desde los movimientos.
     */
    val morososIds = clienteRepository.obtenerIdsMorososRepo()
        .map { it.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    /**
     * _serviciosMap / serviciosMap
     * ----------------------------
     * Mapa idServicio -> nombre con TODOS los servicios (activos e inactivos).
     * Sirve para resolver los nombres de los servicios contratados del cliente
     * en el perfil, incluso si alguno se dio de baja posteriormente.
     */
    private val _serviciosMap = MutableStateFlow<Map<Int, String>>(emptyMap())
    val serviciosMap = _serviciosMap.asStateFlow()

    /**
     * _serviciosActivos / serviciosActivos
     * ------------------------------------
     * Lista de servicios activos del negocio. Sirve al selector de servicios
     * contratados: solo los activos pueden asignarse como nuevos servicios.
     */
    private val _serviciosActivos = MutableStateFlow<List<ServicioEntity>>(emptyList())
    val serviciosActivos = _serviciosActivos.asStateFlow()

    /* ============================================================
     * ============ BLOQUE 5: OPERACIONES DEL VIEWMODEL ===========
     * ============================================================ */
    /**
     * limpiarError
     * ------------
     * ✔ TIPO: método (fun) de ClienteViewModel
     * Es la función que borra el mensaje de error del ViewModel.
     * Sirve para que la interfaz elimine la tarjeta de aviso al entrar en el formulario
     * o cuando el usuario vuelve a editar un campo, evitando errores obsoletos.
     */
    fun limpiarError() {
        _error.value = null
    }

    /**
     * cargarServicios
     * ---------------
     * Observa los servicios (todos) y los servicios activos para el perfil
     * y el selector de servicios contratados del cliente.
     */
    fun cargarServicios() {
        viewModelScope.launch {
            servicioRepository.obtenerTodosLosServicios().collect { lista ->
                _serviciosMap.value = lista.associate { it.idServicio to it.nombre }
            }
        }
        viewModelScope.launch {
            servicioRepository.obtenerServiciosActivos().collect { _serviciosActivos.value = it }
        }
    }

    /**
     * guardarServiciosContratados
     * ---------------------------
     * Actualiza SOLO la lista de servicios contratados del cliente, tanto en
     * Room (fuente de verdad local) como en Firestore (write-through). La
     * nueva lista sustituye a la anterior por completo. Si la réplica remota
     * falla, el cambio local se conserva, se informa y queda preparado el
     * reintento manual con el mismo mecanismo que el alta/edición.
     */
    fun guardarServiciosContratados(idCliente: Int, idsServicios: List<Int>) {
        viewModelScope.launch {
            val actual = clienteRepository.obtenerClientePorIdRepo(idCliente) ?: return@launch
            val actualizado = actual.copy(serviciosContratados = idsServicios.distinct())
            clienteRepository.actualizarClienteRepo(actualizado)

            _errorSincronizacion.value = null
            _clienteSinSincronizar.value = null
            _sincronizacionPendienteServicios.value = false

            val resultado = clienteRemotoRepository.actualizarServiciosContratadosRemoto(
                idCliente,
                actualizado.serviciosContratados
            )
            if (resultado.exito) {
                _sincronizacionPendienteEsAlta.value = false
            } else {
                _errorSincronizacion.value =
                    "Guardado en el dispositivo, pero no sincronizado con la nube: ${resultado.mensaje}"
                _clienteSinSincronizar.value = actualizado
                _sincronizacionPendienteServicios.value = true
            }
        }
    }

    /**
     * insertarCliente
     * ---------------
     * ✔ TIPO: método (fun) de ClienteViewModel
     * Es la función que guarda un nuevo cliente en la base de datos.
     * Sirve para que la interfaz inserte un cliente sin bloquear la UI, lanzando la
     * operación de Room en segundo plano; limpia el error anterior, comprueba que el DNI
     * no esté ya registrado y ejecuta onExito(idGenerado) al terminar correctamente,
     * entregando el id del cliente creado (por ejemplo para guardar la sesión en Mi perfil).
     */
    fun insertarCliente(cliente: ClienteEntity, onExito: (Int) -> Unit = {}) {
        viewModelScope.launch {

            _error.value = null

            val existe = clienteRepository.obtenerClientePorDniRepo(cliente.dni) != null

            if (existe) {
                _error.value = "El DNI ya está registrado"
                return@launch
            }

            try {
                // Id ESTABLE de ámbito alto (IdCliente): no depende del
                // autoincrement de Room (que en cada PC/instalación vuelve a
                // empezar en 1 y podría colisionar con clientes ya existentes
                // en Firestore). El MISMO id se guarda en Room y se replica a
                // Firestore.
                var entidad = cliente
                var intentos = 0
                do {
                    val idCandidato = IdCliente.nuevo()
                    val idEnUso = clienteRepository.obtenerClientePorIdRepo(idCandidato) != null
                    if (!idEnUso) {
                        entidad = cliente.copy(idCliente = idCandidato)
                        break
                    }
                    intentos++
                } while (intentos < 5)

                if (entidad.idCliente == 0) {
                    _error.value = "No se pudo generar un identificador para el cliente"
                    return@launch
                }

                clienteRepository.insertarClienteRepo(entidad)
                if (replicar(entidad, esAlta = true)) {
                    onExito(entidad.idCliente)
                }
            } catch (e: SQLiteConstraintException) {
                _error.value = "El DNI ya está registrado"
            }
        }
    }

    /**
     * incorporarClientesRemotos
     * -------------------------
     * Recupera los clientes del negocio desde Firestore y los añade a Room
     * SOLO si no existen localmente (por idCliente o por DNI). Se usa al
     * abrir la lista de clientes para que un PC nuevo con Room vacía recupere
     * los clientes ya existentes en la nube sin duplicarlos ni modificarlos.
     * No escribe en Firestore (solo lectura remota + inserción local).
     */
    fun incorporarClientesRemotos() {
        viewModelScope.launch {
            try {
                val remotos = clienteRemotoRepository.obtenerClientesRemotosDelNegocio()
                if (remotos.isEmpty()) return@launch

                var incorporados = 0
                for (remoto in remotos) {
                    val existePorId =
                        clienteRepository.obtenerClientePorIdRepo(remoto.idCliente) != null
                    val existePorDni =
                        clienteRepository.obtenerClientePorDniRepo(remoto.dni) != null
                    if (!existePorId && !existePorDni) {
                        clienteRepository.insertarClienteRepo(remoto)
                        incorporados++
                    }
                }
                Log.i(
                    TAG,
                    "Clientes remotos incorporados a Room: $incorporados de ${remotos.size}"
                )
            } catch (e: Exception) {
                Log.e(TAG, "No se pudieron recuperar los clientes remotos", e)
                _error.value = "No se pudieron recuperar los clientes de la nube"
            }
        }
    }

    /**
     * replicar
     * --------
     * ✔ TIPO: método (fun) privado suspend de Kotlin
     * Es la réplica write-through de una ficha local hacia Firestore.
     * Sirve para mantener el espejo remoto sin revertir nunca el cambio
     * local: si falla, deja el estado preparado para el reintento manual.
     */
    private suspend fun replicar(
        entidad: ClienteEntity,
        esAlta: Boolean,
        dniAnterior: String? = null
    ): Boolean {
        _errorSincronizacion.value = null
        _clienteSinSincronizar.value = null
        _sincronizacionPendienteServicios.value = false

        val resultado = if (esAlta) {
            clienteRemotoRepository.crearClienteRemoto(entidad)
        } else {
            clienteRemotoRepository.actualizarClienteRemoto(entidad, dniAnterior)
        }

        if (resultado.exito) {
            _sincronizacionPendienteEsAlta.value = false
            return true
        } else {
            _errorSincronizacion.value =
                "Guardado en el dispositivo, pero no sincronizado con la nube: ${resultado.mensaje}"
            _clienteSinSincronizar.value = entidad
            _sincronizacionPendienteEsAlta.value = esAlta
            return false
        }
    }

    /**
     * reintentarSincronizacion
     * ------------------------
     * ✔ TIPO: método (fun) de Kotlin (lanza corrutina)
     * Repite la última operación remota pendiente (alta o edición).
     * Sirve al botón "Reintentar sincronización" de las pantallas.
     */
    fun reintentarSincronizacion() {
        val pendiente = _clienteSinSincronizar.value ?: return
        viewModelScope.launch {
            if (_sincronizacionPendienteServicios.value) {
                _errorSincronizacion.value = null
                val resultado = clienteRemotoRepository.actualizarServiciosContratadosRemoto(
                    pendiente.idCliente,
                    pendiente.serviciosContratados
                )
                if (resultado.exito) {
                    _clienteSinSincronizar.value = null
                    _sincronizacionPendienteServicios.value = false
                    _sincronizacionPendienteEsAlta.value = false
                } else {
                    _errorSincronizacion.value =
                        "Guardado en el dispositivo, pero no sincronizado con la nube: ${resultado.mensaje}"
                }
            } else {
                replicar(pendiente, _sincronizacionPendienteEsAlta.value)
            }
        }
    }

    /**
     * actualizarCliente
     * -----------------
     * ✔ TIPO: método (fun) de ClienteViewModel
     * Es la función que actualiza los datos de un cliente ya existente.
     * Sirve para que la interfaz modifique un cliente sin bloquear la UI, lanzando la
     * operación de Room en segundo plano; limpia el error anterior, comprueba que el nuevo
     * DNI no pertenezca a otro cliente y ejecuta onExito() al terminar correctamente.
     */
    fun actualizarCliente(cliente: ClienteEntity, onExito: () -> Unit = {}) {

        /**
         * viewModelScope.launch
         * ---------------------
         * ✔ TIPO: corrutina lanzada en el ámbito del ViewModel
         * Es la corrutina que ejecuta la actualización del cliente en segundo plano.
         * Sirve para no bloquear el hilo principal de la UI.
         */
        viewModelScope.launch {

            _error.value = null

            val existente = clienteRepository.obtenerClientePorDniRepo(cliente.dni)

            if (existente != null && existente.idCliente != cliente.idCliente) {
                _error.value = "El DNI ya está registrado"
                return@launch
            }

            // DNI previo para mantener atómico el índice negocio+DNI en Firestore
            // (si el ADMIN cambia el DNI, el índice viejo se borra y el nuevo nace
            // en el mismo Batch que la réplica).
            val dniAnterior = clienteRepository.obtenerClientePorIdRepo(cliente.idCliente)?.dni

            try {
                clienteRepository.actualizarClienteRepo(cliente)
                movimientoRepository.recalcularMorosidadDeCliente(cliente.idCliente)
                if (replicar(cliente, esAlta = false, dniAnterior = dniAnterior)) {
                    onExito()
                }
            } catch (e: SQLiteConstraintException) {
                _error.value = "El DNI ya está registrado"
            }
        }
    }

    /**
     * eliminarCliente
     * ---------------
     * ✔ TIPO: método (fun) de ClienteViewModel
     * Es la función que elimina un cliente de la base de datos.
     * Sirve para que la interfaz borre un cliente sin bloquear la UI,
     * lanzando la operación de Room en segundo plano.
     */
    fun eliminarCliente(cliente: ClienteEntity) {

        /**
         * viewModelScope.launch
         * ---------------------
         * ✔ TIPO: corrutina lanzada en el ámbito del ViewModel
         * Es la corrutina que ejecuta la eliminación del cliente en segundo plano.
         * Sirve para no bloquear el hilo principal de la UI.
         */
        viewModelScope.launch {
            clienteRepository.eliminarClienteRepo(cliente)
        }
    }

    /**
     * archivarCliente
     * ---------------
     * ✔ TIPO: método (fun) de ClienteViewModel
     * Es la función que cambia el estado de un cliente a ARCHIVADO.
     * Sirve para ocultar un cliente de la lista principal conservando todos sus datos.
     */
    fun archivarCliente(cliente: ClienteEntity) {
        viewModelScope.launch {
            val actualizado = cliente.copy(estado = EstadoCliente.ARCHIVADO)
            clienteRepository.actualizarClienteRepo(actualizado)
            replicar(actualizado, esAlta = false)
        }
    }

    fun archivarCliente(cliente: Cliente) {
        viewModelScope.launch {
            val entity = clienteRepository.obtenerClientePorIdRepo(cliente.idCliente) ?: return@launch
            val actualizado = entity.copy(estado = EstadoCliente.ARCHIVADO)
            clienteRepository.actualizarClienteRepo(actualizado)
            replicar(actualizado, esAlta = false)
        }
    }

    /**
     * restaurarCliente
     * ----------------
     * ✔ TIPO: método (fun) de ClienteViewModel
     * Es la función que restaura un cliente archivado cambiando su estado a ACTIVO.
     * Sirve para devolver un cliente archivado a la lista principal.
     */
    fun restaurarCliente(cliente: ClienteEntity) {
        viewModelScope.launch {
            val actualizado = cliente.copy(
                estado = EstadoCliente.ACTIVO,
                fechaBaja = null
            )
            clienteRepository.actualizarClienteRepo(actualizado)
            movimientoRepository.recalcularMorosidadDeCliente(actualizado.idCliente)
            replicar(actualizado, esAlta = false)
        }
    }

    fun restaurarCliente(cliente: Cliente) {
        viewModelScope.launch {
            val entity = clienteRepository.obtenerClientePorIdRepo(cliente.idCliente) ?: return@launch
            val actualizado = entity.copy(
                estado = EstadoCliente.ACTIVO,
                fechaBaja = null
            )
            clienteRepository.actualizarClienteRepo(actualizado)
            movimientoRepository.recalcularMorosidadDeCliente(actualizado.idCliente)
            replicar(actualizado, esAlta = false)
        }
    }

    /**
     * darDeBaja
     * ---------
     * BAJA DIRECTA de un cliente (sin solicitud previa). Produce las MISMAS
     * consecuencias de negocio que aceptar una solicitud de baja:
     *   1. Room: cliente -> BAJA + fechaBaja;
     *   2. Room: cancela las reservas futuras del cliente (libera plazas);
     *   3. Firestore: replica el cliente en BAJA;
     *   4. Firestore: cancela reservas futuras y genera BAJA_CONFIRMADA si la
     *      configuración lo permite (BajaClienteRemotoRepository).
     * Los servicios contratados se conservan.
     */
    fun darDeBaja(cliente: ClienteEntity, onExito: () -> Unit = {}) {
        viewModelScope.launch {
            _error.value = null
            val fechaBaja = cliente.fechaBaja ?: System.currentTimeMillis()
            val entidad = cliente.copy(estado = EstadoCliente.BAJA, fechaBaja = fechaBaja)

            try {
                clienteRepository.actualizarClienteRepo(entidad)
                movimientoRepository.recalcularMorosidadDeCliente(entidad.idCliente)
            } catch (e: Exception) {
                _error.value = "No se pudo guardar el cliente"
                return@launch
            }

            try {
                reservaRepository.cancelarReservasFuturasDeCliente(
                    entidad.idCliente,
                    System.currentTimeMillis()
                )
            } catch (e: Exception) {
                Log.e(TAG, "No se pudieron cancelar las reservas futuras en Room", e)
            }

            val dniAnterior = clienteRepository.obtenerClientePorIdRepo(entidad.idCliente)?.dni
            if (replicar(entidad, esAlta = false, dniAnterior = dniAnterior)) {
                bajaClienteRemotoRepository.bajaEfectiva(entidad.idCliente, fechaBaja)
                onExito()
            }
        }
    }

    fun obtenerClientePorId(id: Int) {
        viewModelScope.launch {
            val clienteEntity = clienteRepository.obtenerClientePorIdRepo(id)

            _clienteSeleccionado.value = clienteEntity?.toCliente()
        }
    }

    /**
     * obtenerClienteParaEditar
     * ------------------------
     * ✔ TIPO: método (fun) de ClienteViewModel
     * Es la función que carga de la base de datos el cliente que se va a editar.
     * Sirve para que la pantalla de modificar cliente obtenga los datos originales
     * y pueda rellenar el formulario antes de guardar los cambios.
     */
    fun obtenerClienteParaEditar(id: Int) {
        viewModelScope.launch {
            _clienteEditando.value = clienteRepository.obtenerClientePorIdRepo(id)
        }
    }

}
