package com.roberto.gestorpro.cliente.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.roberto.gestorpro.cliente.model.EstadoCliente
import com.roberto.gestorpro.cliente.model.EstadoSolicitud
import com.roberto.gestorpro.cliente.navigation.Routes
import com.roberto.gestorpro.cliente.ui.components.MenuCard
import com.roberto.gestorpro.cliente.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

/**
 * CuentaScreen
 * ------------
 * Gestión de la cuenta del CLIENTE: cerrar sesión y solicitar la baja del
 * gimnasio (la solicitud queda PENDIENTE hasta que el ADMIN la resuelva; el
 * cliente permanece ACTIVO mientras tanto).
 */
@Composable
fun CuentaScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val solicitudesBaja by mainViewModel.solicitudesBaja.collectAsStateWithLifecycle()
    val cargandoSolicitudes by mainViewModel.cargandoSolicitudesBaja.collectAsStateWithLifecycle()
    val operandoSolicitud by mainViewModel.operandoSolicitudBaja.collectAsStateWithLifecycle()
    val errorSolicitud by mainViewModel.errorSolicitudBaja.collectAsStateWithLifecycle()
    val mensaje by mainViewModel.mensaje.collectAsStateWithLifecycle()
    val cliente by mainViewModel.cliente.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }
    var mostrarConfirmarBaja by remember { mutableStateOf(false) }
    var mostrarPendienteInfo by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        mainViewModel.cargarSolicitudesBaja()
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            mainViewModel.limpiarMensaje()
        }
    }

    val tienePendiente = solicitudesBaja.any { it.estado == EstadoSolicitud.PENDIENTE }
    val ultimaRechazada = solicitudesBaja.firstOrNull()?.let {
        it.estado == EstadoSolicitud.RECHAZADA
    } ?: false

    val estadoBaja = cliente?.estado == EstadoCliente.BAJA

    val descripcionBaja = when {
        estadoBaja -> "Ya estás dado de baja"
        tienePendiente -> "Solicitud de baja pendiente de revisión"
        ultimaRechazada -> "Tu solicitud fue rechazada. Puedes volver a solicitarla."
        else -> "Solicitar la baja del gimnasio"
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
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
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Mi cuenta",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            MenuCard(
                titulo = "Solicitar baja",
                descripcion = descripcionBaja,
                icono = Icons.Default.Person,
                onClick = {
                    when {
                        estadoBaja -> {
                            // Sin acción: el cliente ya está de baja.
                        }
                        tienePendiente -> mostrarPendienteInfo = true
                        else -> mostrarConfirmarBaja = true
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            MenuCard(
                titulo = "Cerrar sesión",
                descripcion = "Salir de la aplicación",
                icono = Icons.Default.ExitToApp,
                onClick = { mostrarDialogoCerrarSesion = true }
            )

            if (cargandoSolicitudes) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Comprobando solicitudes...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            errorSolicitud?.let { mensajeError ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mensajeError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (mostrarDialogoCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrarSesion = false },
            title = {
                Text(
                    text = "Cerrar sesión",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF1E88E5)
                )
            },
            text = {
                Text("¿Seguro que quieres cerrar sesión?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoCerrarSesion = false
                        mainViewModel.cerrarSesion()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    Text("Cerrar sesión", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCerrarSesion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarConfirmarBaja) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarBaja = false },
            title = {
                Text(
                    text = "Solicitar baja",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFF44336)
                )
            },
            text = {
                Text(
                    "¿Seguro que quieres solicitar la baja del gimnasio? " +
                        "La solicitud quedará pendiente de revisión por el administrador " +
                        "y seguirás activo hasta que se confirme."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmarBaja = false
                        coroutineScope.launch {
                            val error = mainViewModel.solicitarBaja(null)
                            if (error != null) {
                                snackbarHostState.showSnackbar(error)
                            }
                        }
                    },
                    enabled = !operandoSolicitud,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    Text("Solicitar baja", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmarBaja = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarPendienteInfo) {
        AlertDialog(
            onDismissRequest = { mostrarPendienteInfo = false },
            title = {
                Text(
                    text = "Solicitud de baja pendiente",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFFF9800)
                )
            },
            text = {
                Text(
                    "Ya tienes una solicitud de baja pendiente de revisión. " +
                        "El administrador la aceptará o la rechazará. " +
                        "Mientras tanto sigues activo en el gimnasio."
                )
            },
            confirmButton = {
                TextButton(onClick = { mostrarPendienteInfo = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}
