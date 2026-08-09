package com.roberto.clientesgestor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope


import com.roberto.clientesgestor.data.entity.ClienteEntity
import com.roberto.clientesgestor.data.entity.toCliente
import com.roberto.clientesgestor.data.repository.ClienteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * launch
 * ------
 * ✔ TIPO: import (kotlinx.coroutines.launch)
 * Es la función de corrutinas que lanza un bloque de código en segundo plano.
 * Sirve para ejecutar operaciones suspendidas, como guardar en Room, dentro de viewModelScope.
 */
import kotlinx.coroutines.launch

/**
 * ClienteViewModel.kt
 * -------------------
 * ✔ TIPO: archivo de código fuente Kotlin (ViewModel)
 * Es el archivo que define el ViewModel encargado de la lógica de los clientes.
 * Sirve para conectar la interfaz de usuario con el repositorio de clientes.
 */

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

    /**
     * Clientes
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
     * insertarCliente
     * ---------------
     * ✔ TIPO: método (fun) de ClienteViewModel
     * Es la función que guarda un nuevo cliente en la base de datos.
     * Sirve para que la interfaz inserte un cliente sin bloquear la UI,
     * lanzando la operación de Room en segundo plano.
     */
    fun insertarCliente(cliente: ClienteEntity) {

        /**
         * viewModelScope.launch
         * ---------------------
         * ✔ TIPO: corrutina lanzada en el ámbito del ViewModel
         * Es la corrutina que ejecuta la inserción del cliente en segundo plano.
         * Sirve para no bloquear el hilo principal de la UI
         * y cancelar la operación automáticamente si el ViewModel se destruye.
         */
        viewModelScope.launch {

            /**
             * insertarClienteRepo
             * -------------------
             * ✔ TIPO: llamada suspendida a ClienteRepository
             * Es la operación que inserta el cliente a través del repositorio.
             * Sirve para que el DAO guarde el ClienteEntity en la base de datos Room.
             */
            clienteRepository.insertarClienteRepo(cliente)
        }
    }
}
