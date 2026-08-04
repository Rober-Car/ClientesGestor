package com.roberto.clientesgestor.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
    navController: NavHostController
) {

    /**
     * mensaje
     * -------
     * ✔ TIPO: variable con estado (var) mediante delegación `by`
     * ✔ ORIGEN DEL ESTADO: remember { mutableStateOf(...) }
     * Es un estado observable que Compose recuerda entre recomposiciones.
     * Sirve para mostrar el nombre de la app y actualizar el texto al entrar.
     */
    var mensaje by remember {
        mutableStateOf("ClientesGestor")
    }

    /**
     * email
     * -----
     * ✔ TIPO: variable con estado (var)
     * Es el estado que guarda el texto escrito en el campo Email.
     * Sirve para controlar el valor del campo y validar el formulario.
     */
    var email by remember {
        mutableStateOf("")
    }

    /**
     * password
     * --------
     * ✔ TIPO: variable con estado (var)
     * Es el estado que guarda el texto escrito en el campo Contraseña.
     * Sirve para controlar el valor del campo y validar el formulario.
     */
    var password by remember {
        mutableStateOf("")
    }

    /**
     * formularioValido
     * ----------------
     * ✔ TIPO DE DATO: Boolean
     * Es una expresión lógica que comprueba email y password con .isNotBlank() y el operador &&.
     * Sirve para activar o desactivar el botón "Entrar" según el formulario esté completo.
     */
    val formularioValido =
        email.isNotBlank() &&
                password.isNotBlank()

    /**
     * Column
     * ------
     * ✔ TIPO: contenedor vertical
     * Es un layout que coloca elementos uno debajo del otro.
     * Sirve para organizar el login de forma centrada en toda la pantalla.
     */
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /**
         * Text(text = mensaje)
         * --------------------
         * ✔ TIPO: Text
         * Es el texto que muestra el contenido de la variable "mensaje".
         * Sirve para enseñar el nombre de la app o el mensaje actualizado.
         */
        Text(
            text = mensaje,
            modifier = Modifier.padding(32.dp)
        )

        /**
         * deja un hueco vertical de 16dp entre los dos elementos.
         */
        Spacer(modifier = Modifier.height(16.dp))

        /**
         * OutlinedTextField (Email)
         * -------------------------
         * ✔ TIPO: OutlinedTextField (Material 3)
         * Es un campo de texto con borde para que el usuario escriba información.
         * Sirve para capturar el email manteniéndolo sincronizado con la variable de estado.
         */
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            }, label = {
                Text("Email")
            }
        )

        /**
         * deja un hueco vertical de 16dp entre los dos elementos.
         */
        Spacer(modifier = Modifier.height(16.dp))

        /**
         * OutlinedTextField (Contraseña)
         * -----------------------------
         * ✔ TIPO: OutlinedTextField (Material 3)
         * Es un campo de texto con borde para introducir la contraseña.
         * Sirve para capturar la contraseña manteniéndola sincronizada con la variable de estado.
         */
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text("Contraseña")
            }
        )

        /**
         * deja un hueco vertical de 16dp entre los dos elementos.
         */
        Spacer(modifier = Modifier.height(16.dp))

        /**
         * Text("Has escrito: $email")
         * --------------------------
         * ✔ TIPO: Text
         * Es un texto informativo que muestra en pantalla lo escrito en el email.
         * Sirve para comprobar en tiempo real el valor del estado.
         */
        Text(
            text = "Has escrito: $email"
        )

        /**
         * Spacer
         * ------
         * ✔ TIPO: Spacer
         * Es un elemento invisible que ocupa espacio.
         * Sirve para separar el texto del email del texto informativo.
         */
        Spacer(
            modifier = Modifier.height(16.dp)
        )

        /**
         * Text("Gestión de clientes y cuotas")
         * ------------------------------------
         * ✔ TIPO: Text
         * Es una frase fija que se muestra debajo del mensaje principal.
         * Sirve para presentar el propósito de la aplicación.
         */
        Text("Gestión de clientes y cuotas")

        /**
         * Button
         * ------
         * ✔ TIPO: función @Composable (androidx.compose.material3.Button)
         * Es un componente interactivo que el usuario puede pulsar.
         * Sirve para actualizar el mensaje y navegar a Home cuando el formulario es válido.
         */
        Button(
            onClick = {
                mensaje = "Bienvenido"
                navController.navigate(Routes.HOME)
            },
            enabled = formularioValido
        ) {

            /**
             * Text("Entrar")
             * -------------
             * ✔ TIPO: Text
             * Es el texto que se muestra dentro del botón.
             * Sirve para indicar la acción de acceso a la aplicación.
             */
            Text("Entrar")
        }
    }
}
