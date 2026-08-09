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
    navController: NavHostController
) {
    var mensaje by remember { mutableStateOf("ClientesGestor") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val formularioValido =
        email.isNotBlank() &&
                password.isNotBlank()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = mensaje, modifier = Modifier.padding(32.dp))
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Has escrito: $email")
        Spacer(modifier = Modifier.height(16.dp))

        Text("Gestión de clientes y cuotas")

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
