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
import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.roberto.gestorpro.cliente.navigation.Routes
import com.roberto.gestorpro.cliente.ui.components.AppNavigationBackButton
import com.roberto.gestorpro.cliente.ui.components.AppPrimaryButton
import com.roberto.gestorpro.cliente.ui.components.AppSecondaryButton
import com.roberto.gestorpro.cliente.ui.viewmodel.MainViewModel
import com.roberto.gestorpro.cliente.ui.viewmodel.TipoResultadoVinculacion
import kotlinx.coroutines.launch

/**
 * InicioScreen
 * ------------
 * Pantalla de vinculación del CLIENTE al centro: introduce el código maestro
 * y su DNI y pulsa "Continuar". La aplicación decide automáticamente la vía:
 *  - VÍA 1: el centro ya creó la ficha → se vincula a ella;
 *  - VÍA 2: sin ficha pero con perfil pendiente completo → se crea la ficha.
 * También ofrece "No tengo vinculación" para entrar al Home sin vincular.
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
    var necesitaRegistro by rememberSaveable { mutableStateOf(false) }
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppNavigationBackButton(onClick = { navController.popBackStack() })
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Vincularme al centro",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Text(
                text = "Introduce el código maestro de tu centro y tu DNI " +
                    "para vincular tu cuenta.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = codigo,
                onValueChange = {
                    codigo = it
                    mensajeError = ""
                    necesitaRegistro = false
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
                    necesitaRegistro = false
                },
                label = { Text("DNI") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (necesitaRegistro) {
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = mensajeError.ifBlank {
                        "No encontramos una ficha con este DNI. Comprueba con tu " +
                            "centro o regístrate primero."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(12.dp))
                AppSecondaryButton(
                    text = "Registrarme",
                    onClick = { navController.navigate(Routes.COMPLETAR_PERFIL) },
                    enabled = !operandoRemoto
                )
            } else if (mensajeError.isNotBlank()) {
                Text(
                    text = mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            AppPrimaryButton(
                text = "Continuar",
                onClick = {
                    mensajeError = ""
                    necesitaRegistro = false
                    scope.launch {
                        mainViewModel.limpiarMensaje()
                        val resultado = mainViewModel.vincularConCodigoYDNI(codigo, dni)
                        when (resultado.tipo) {
                            TipoResultadoVinculacion.VINCULADO -> {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                            TipoResultadoVinculacion.NECESITA_PERFIL -> {
                                necesitaRegistro = true
                                mensajeError = resultado.mensaje ?: ""
                            }
                            TipoResultadoVinculacion.ERROR -> {
                                mensajeError = resultado.mensaje
                                    ?: "No se pudo completar la vinculación. Inténtalo de nuevo"
                            }
                        }
                    }
                },
                enabled = !operandoRemoto && codigo.isNotBlank() && dni.isNotBlank()
            )

            AppSecondaryButton(
                text = "No tengo vinculación",
                onClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                enabled = !operandoRemoto
            )

            if (operandoRemoto) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
    }
}
