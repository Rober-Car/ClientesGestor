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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.roberto.gestorpro.cliente.navigation.Routes
import com.roberto.gestorpro.cliente.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

/**
 * InicioScreen
 * ------------
 * Pantalla "¿Tu gimnasio ya te ha registrado?" del CLIENTE autenticado pero
 * sin ficha vinculada. Ofrece:
 *  - código maestro + DNI + Continuar (VÍA 1 o VÍA 2);
 *  - "No tengo código" → completar perfil (VÍA 2).
 */
@Composable
fun InicioScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val operandoRemoto by mainViewModel.operandoRemoto.collectAsStateWithLifecycle()
    val mensaje by mainViewModel.mensaje.collectAsStateWithLifecycle()

    var codigo by rememberSaveable { mutableStateOf("") }
    var dni by rememberSaveable { mutableStateOf("") }
    var mensajeError by rememberSaveable { mutableStateOf("") }
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
                    text = "Vincularme a un gimnasio",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Text(
                text = "¿Tu gimnasio ya te ha registrado?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Introduce el código maestro de tu gimnasio y tu DNI. " +
                    "Si tu gimnasio ya te registró, vincularemos tu cuenta a tu ficha. " +
                    "Si no, la crearemos con tus datos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = codigo,
                onValueChange = {
                    codigo = it
                    mensajeError = ""
                },
                label = { Text("Código maestro") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = dni,
                onValueChange = {
                    dni = it.uppercase()
                    mensajeError = ""
                },
                label = { Text("DNI") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (mensajeError.isNotBlank()) {
                Text(
                    text = mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    mensajeError = ""
                    scope.launch {
                        mainViewModel.limpiarMensaje()
                        val error = mainViewModel.vincularConCodigoYDNI(codigo, dni)
                        if (error == null) {
                            navController.navigate(Routes.HOME) {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            mensajeError = error
                        }
                    }
                },
                enabled = !operandoRemoto && codigo.isNotBlank() && dni.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) {
                Text("Continuar", color = Color.White)
            }

            OutlinedButton(
                onClick = {
                    navController.navigate(Routes.COMPLETAR_PERFIL)
                },
                enabled = !operandoRemoto,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("No tengo vinculación")
            }

            if (operandoRemoto) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
    }
}
