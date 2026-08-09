package com.roberto.clientesgestor.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.roberto.clientesgestor.navigation.Routes

/**
 * LoginScreen.kt
 * --------------
 * ✔ TIPO: archivo de código fuente Kotlin (pantalla de autenticación)
 * Es el archivo que define la pantalla de inicio de sesión de la aplicación.
 * Sirve para autenticar al usuario y dar acceso al menú principal de ClientesGestor.
 */

/**
 * LoginScreen
 * -----------
 * ✔ TIPO: función @Composable
 * Es la pantalla de inicio de sesión que muestra el formulario de acceso.
 * Sirve para capturar email y contraseña y navegar a Home al pulsar "Entrar".
 */
@Composable
fun LoginScreen(
    /**
     * navController
     * -------------
     * ✔ TIPO: parámetro (param) → NavHostController
     * Es el controlador de navegación que recibe la pantalla de Login.
     * Sirve para poder navegar hasta el menú principal (HOME) cuando el login sea correcto.
     */
    navController: NavHostController
) {

    /**
     * mensaje
     * -------
     * ✔ TIPO: variable (var) → String
     * Es el texto de bienvenida que se muestra en la pantalla.
     * Sirve para guardar el saludo inicial y cambiarlo a "Bienvenido" al pulsar Entrar.
     */
    var mensaje by remember { mutableStateOf("ClientesGestor") }

    /**
     * email
     * -----
     * ✔ TIPO: variable (var) → String
     * Es el texto que escribe el usuario en el campo de Email.
     * Sirve para guardar el email introducido y validar el formulario.
     */
    var email by remember { mutableStateOf("") }

    /**
     * password
     * --------
     * ✔ TIPO: variable (var) → String
     * Es el texto que escribe el usuario en el campo de Contraseña.
     * Sirve para guardar la contraseña introducida y validar el formulario.
     */
    var password by remember { mutableStateOf("") }

    /**
     * formularioValido
     * ----------------
     * ✔ TIPO: variable inmutable (val) → Boolean
     * Es el resultado de comprobar si el formulario está completo.
     * Sirve para habilitar o deshabilitar el botón "Entrar":
     * solo es válido cuando email y password no están en blanco.
     */
    val formularioValido =
        email.isNotBlank() &&
                password.isNotBlank()

    /**
     * Column
     * ------
     * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Column)
     * Es el contenedor vertical que agrupa todos los elementos del formulario.
     * Sirve para colocar los campos y el botón uno debajo de otro,
     * centrados vertical y horizontalmente en la pantalla.
     */
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /**
         * Text de mensaje
         * ---------------
         * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
         * Es el texto de bienvenida que muestra el mensaje de la variable.
         * Sirve para presentar el título de la aplicación al usuario.
         */
        Text(text = mensaje, modifier = Modifier.padding(32.dp))

        /**
         * Spacer
         * ------
         * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Spacer)
         * Es un espacio vacío de altura fija entre los elementos.
         * Sirve para separar visualmente los campos del formulario.
         */
        Spacer(modifier = Modifier.height(16.dp))

        /**
         * OutlinedTextField de email
         * --------------------------
         * ✔ TIPO: función @Composable (androidx.compose.material3.OutlinedTextField)
         * Es el campo de texto donde el usuario escribe su correo electrónico.
         * Sirve para capturar el email y guardarlo en la variable email al teclear.
         */
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        /**
         * OutlinedTextField de password
         * -----------------------------
         * ✔ TIPO: función @Composable (androidx.compose.material3.OutlinedTextField)
         * Es el campo de texto donde el usuario escribe su contraseña.
         * Sirve para capturar la contraseña y guardarla en la variable password al teclear.
         */
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        /**
         * Text de comprobación
         * --------------------
         * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
         * Es un texto provisional que muestra el email escrito en tiempo real.
         * Sirve para comprobar visualmente que la variable email se actualiza correctamente.
         */
        Text(text = "Has escrito: $email")
        Spacer(modifier = Modifier.height(16.dp))

        /**
         * Text de descripción
         * -------------------
         * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
         * Es el texto que describe el propósito de la aplicación.
         * Sirve para indicar que ClientesGestor gestiona clientes y cuotas.
         */
        Text("Gestión de clientes y cuotas")

        /**
         * Button Entrar
         * -------------
         * ✔ TIPO: función @Composable (androidx.compose.material3.Button)
         * Es el botón que envía el formulario de inicio de sesión.
         * Sirve para cambiar el mensaje a "Bienvenido" y navegar al menú principal,
         * quedando deshabilitado mientras el formulario no sea válido.
         */
        Button(
            onClick = {
                mensaje = "Bienvenido"
                navController.navigate(Routes.HOME)
            },
            enabled = formularioValido
        ) {
            Text("Entrar")
        }
    }
}
