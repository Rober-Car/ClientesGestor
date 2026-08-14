/* ============================================================
 * ============ BLOQUE 1: IMPORTS =============================
 * ============================================================ */
package com.roberto.clientesgestor.ui.viewmodel

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.clientesgestor.data.entity.ClienteEntity
import com.roberto.clientesgestor.data.entity.toCliente
import com.roberto.clientesgestor.data.repository.ClienteRepository
import com.roberto.clientesgestor.model.Cliente
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
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
    private val clienteRepository: ClienteRepository
) : ViewModel() {

    /* ============================================================
     * ============ BLOQUE 4: ESTADO DEL VIEWMODEL ================
     * ============================================================ */
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
     * insertarCliente
     * ---------------
     * ✔ TIPO: método (fun) de ClienteViewModel
     * Es la función que guarda un nuevo cliente en la base de datos.
     * Sirve para que la interfaz inserte un cliente sin bloquear la UI, lanzando la
     * operación de Room en segundo plano; limpia el error anterior, comprueba que el DNI
     * no esté ya registrado y ejecuta onExito() al terminar correctamente (por ejemplo,
     * para volver a la lista de clientes).
     */
    fun insertarCliente(cliente: ClienteEntity, onExito: () -> Unit = {}) {
        viewModelScope.launch {

            _error.value = null

            val existe = clienteRepository.obtenerClientePorDniRepo(cliente.dni) != null

            if (existe) {
                _error.value = "El DNI ya está registrado"
                return@launch
            }

            try {
                clienteRepository.insertarClienteRepo(cliente)
                onExito()
            } catch (e: SQLiteConstraintException) {
                _error.value = "El DNI ya está registrado"
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

            try {
                clienteRepository.actualizarClienteRepo(cliente)
                onExito()
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
