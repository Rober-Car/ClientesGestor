package com.roberto.gestorpro.cliente.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.roberto.gestorpro.cliente.navigation.Routes
import com.roberto.gestorpro.cliente.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun RecuperarPasswordScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val autenticando by mainViewModel.autenticando.collectAsStateWithLifecycle()

    var email by rememberSaveable { mutableStateOf("") }
    var mensaje by rememberSaveable { mutableStateOf("") }
    var esError by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Recuperar contraseña",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Text(
                text = "Introduce el email de tu cuenta y recibirás un enlace " +
                    "para restablecer la contraseña.",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    mensaje = ""
                },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (mensaje.isNotBlank()) {
                Text(
                    text = mensaje,
                    color = if (esError) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        mensaje = ""
                        val error = mainViewModel.enviarCorreoRecuperacion(email.trim())
                        if (error == null) {
                            esError = false
                            mensaje = "Si el email existe, recibirás un enlace para " +
                                "restablecer tu contraseña"
                        } else {
                            esError = true
                            mensaje = error
                        }
                    }
                },
                enabled = !autenticando && email.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) {
                Text("Enviar correo", color = Color.White)
            }

            if (autenticando) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
    }
}
