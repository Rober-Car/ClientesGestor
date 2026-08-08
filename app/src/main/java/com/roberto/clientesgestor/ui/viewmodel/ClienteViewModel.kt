package com.roberto.clientesgestor.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.roberto.clientesgestor.data.repository.ClienteRepository
import dagger.hilt.android.lifecycle.HiltViewModel

/**
 * ClienteViewModel.kt
 * -------------------título
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
class ClienteViewModel(
    /**
     * clienteRepository
     * -----------------
     * ✔ TIPO: parámetro (param) → ClienteRepository
     * Es el repositorio que se inyecta en el ViewModel.
     * Sirve para que el ViewModel acceda a las operaciones de la base de datos sin conocer el DAO.
     */
    private val clienteRepository: ClienteRepository
) : ViewModel() {

}