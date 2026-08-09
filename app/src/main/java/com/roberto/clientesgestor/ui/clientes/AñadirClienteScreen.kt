package com.roberto.clientesgestor.ui.clientes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.roberto.clientesgestor.ui.viewmodel.ClienteViewModel

/**
 * AñadirClienteScreen.kt
 * ----------------------
 * ✔ TIPO: archivo de código fuente Kotlin (pantalla de alta de clientes)
 * Es el archivo que define la pantalla para añadir un nuevo cliente.
 * Sirve para capturar los datos del cliente antes de guardarlos en la base de datos.
 */

/**
 * AñadirClienteScreen
 * -------------------
 * ✔ TIPO: función @Composable
 * Es la pantalla de alta de un cliente con los campos del formulario.
 * Sirve para que el administrador introduzca los datos de un nuevo cliente
 * y los prepare para ser guardados a través del ClienteViewModel.
 */
@Composable
fun AñadirClienteScreen(
    /**
     * viewModel
     * ---------
     * ✔ TIPO: parámetro (param) → ClienteViewModel (inyectado por Hilt)
     * Es el ViewModel de clientes que recibe la pantalla.
     * Sirve para guardar el nuevo cliente en la base de datos cuando se complete el formulario.
     */
    viewModel: ClienteViewModel = hiltViewModel()
) {

    /**
     * nombre
     * ------
     * ✔ TIPO: variable con estado (var) → String
     * Es el nombre del cliente que se escribe en el campo.
     * Sirve para guardar el texto del campo "Nombre" mientras se rellena el formulario.
     */
    var nombre by remember { mutableStateOf("") }

    /**
     * apellidos
     * ---------
     * ✔ TIPO: variable con estado (var) → String
     * Es el apellido o apellidos del cliente que se escriben en el campo.
     * Sirve para guardar el texto del campo "Apellidos" mientras se rellena el formulario.
     */
    var apellidos by remember { mutableStateOf("") }

    /**
     * dni
     * ---
     * ✔ TIPO: variable con estado (var) → String
     * Es el DNI del cliente que se escribe en el campo.
     * Sirve para guardar el texto del campo "DNI" mientras se rellena el formulario.
     */
    var dni by remember { mutableStateOf("") }

    /**
     * telefono
     * --------
     * ✔ TIPO: variable con estado (var) → String
     * Es el teléfono del cliente que se escribe en el campo.
     * Sirve para guardar el texto del campo "Teléfono" mientras se rellena el formulario.
     */
    var telefono by remember { mutableStateOf("") }

    /**
     * email
     * -----
     * ✔ TIPO: variable con estado (var) → String
     * Es el email del cliente que se escribe en el campo.
     * Sirve para guardar el texto del campo "Email" mientras se rellena el formulario.
     */
    var email by remember { mutableStateOf("") }

    /**
     * fechaNacimiento
     * ---------------
     * ✔ TIPO: variable con estado (var) → Long?
     * Es la fecha de nacimiento del cliente convertida a timestamp.
     * Sirve para guardar la fecha en milisegundos; es null mientras no se escriba una fecha válida.
     */
    var fechaNacimiento by remember { mutableStateOf<Long?>(null) }

    /**
     * Column
     * ------
     * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Column)
     * Es el contenedor vertical del formulario de alta.
     * Sirve para apilar todos los campos uno debajo de otro,
     * ocupando toda la pantalla y permitiendo hacer scroll si no caben.
     */
    Column( modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())

    ) {

        /**
         * TextField de Nombre
         * -------------------
         * ✔ TIPO: función @Composable (androidx.compose.material3.TextField)
         * Es el campo de texto donde se escribe el nombre del cliente.
         * Sirve para capturar el nombre y guardarlo en la variable nombre al teclear.
         */
        TextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") }
        )

        /**
         * TextField de Apellidos
         * ----------------------
         * ✔ TIPO: función @Composable (androidx.compose.material3.TextField)
         * Es el campo de texto donde se escriben los apellidos del cliente.
         * Sirve para capturar los apellidos y guardarlos en la variable apellidos al teclear.
         */
        TextField(
            value = apellidos,
            onValueChange = { apellidos = it },
            label = { Text("Apellidos") }
        )

        /**
         * TextField de DNI
         * ----------------
         * ✔ TIPO: función @Composable (androidx.compose.material3.TextField)
         * Es el campo de texto donde se escribe el DNI del cliente.
         * Sirve para capturar el DNI y guardarlo en la variable dni al teclear.
         */
        TextField(
            value = dni,
            onValueChange = { dni = it },
            label = { Text("DNI") }
        )

        /**
         * TextField de Teléfono
         * ---------------------
         * ✔ TIPO: función @Composable (androidx.compose.material3.TextField)
         * Es el campo de texto donde se escribe el teléfono del cliente.
         * Sirve para capturar el teléfono y guardarlo en la variable telefono al teclear.
         */
        TextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono") }
        )

        /**
         * TextField de Email
         * ------------------
         * ✔ TIPO: función @Composable (androidx.compose.material3.TextField)
         * Es el campo de texto donde se escribe el email del cliente.
         * Sirve para capturar el email y guardarlo en la variable email al teclear.
         */
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        /**
         * TextField de Fecha de nacimiento
         * --------------------------------
         * ✔ TIPO: función @Composable (androidx.compose.material3.TextField)
         * Es el campo de texto donde se escribe la fecha de nacimiento del cliente.
         * Sirve para capturar la fecha y guardarla en la variable fechaNacimiento
         * convertida a Long (o null si el texto no es un número válido).
         */
        TextField(
            value = fechaNacimiento.toString(),
            onValueChange = { fechaNacimiento = it.toLongOrNull() },
            label = { Text("Fecha de nacimiento") }
        )
    }
}
