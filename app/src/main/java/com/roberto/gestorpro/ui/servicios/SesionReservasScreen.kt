package com.roberto.gestorpro.ui.servicios

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.roberto.gestorpro.model.ReservaClienteDetalle
import com.roberto.gestorpro.navigation.Routes
import com.roberto.gestorpro.ui.components.AppNavigationBackButton
import com.roberto.gestorpro.ui.components.ClienteItem
import com.roberto.gestorpro.ui.viewmodel.ClienteViewModel
import com.roberto.gestorpro.ui.viewmodel.ServicioViewModel
import com.roberto.gestorpro.ui.viewmodel.SesionViewModel

/**
 * SesionReservasScreen
 * --------------------
 * Pantalla de detalle de una sesión con su lista de reservas, dentro del
 * nuevo flujo de SERVICIOS → SESIONES. Trabaja con SesionEntity.
 */
@Composable
fun SesionReservasScreen(
    navController: NavHostController,
    idSesion: Int,
    viewModel: SesionViewModel = hiltViewModel(),
    clienteViewModel: ClienteViewModel = hiltViewModel(),
    servicioViewModel: ServicioViewModel = hiltViewModel()
) {
    val sesion by viewModel.sesionDetalle.collectAsStateWithLifecycle()
    val reservas by viewModel.reservasDetalle.collectAsStateWithLifecycle()
    val morososIds by clienteViewModel.morososIds.collectAsStateWithLifecycle()
    val servicio by servicioViewModel.servicioSeleccionado.collectAsStateWithLifecycle()

    LaunchedEffect(idSesion) {
        viewModel.cargarSesion(idSesion)
        viewModel.cargarReservasSesion(idSesion)
    }

    LaunchedEffect(sesion?.idServicio) {
        sesion?.let { servicioViewModel.cargarServicio(it.idServicio) }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.limpiarDetalle()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppNavigationBackButton(onClick = { navController.popBackStack() })
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Reservas de la sesión",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (sesion == null) {
                Text(
                    text = "Cargando sesión...",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                )
            } else {
                val s = sesion!!
                val plazasDisponibles = s.capacidad - reservas.size
                val textoPlazas = if (plazasDisponibles == 1) {
                    "1 plaza disponible"
                } else {
                    "$plazasDisponibles plazas disponibles"
                }
                val textoReservadas = if (reservas.size == 1) {
                    "1 reservada"
                } else {
                    "${reservas.size} reservadas"
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = servicio?.nombre ?: "Servicio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E88E5)
                        )
                        FilaDatoSesionReserva(
                            icono = Icons.Default.EventSeat,
                            texto = "$textoPlazas · $textoReservadas"
                        )
                    }
                }

                Text(
                    text = "Clientes reservados (${reservas.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (reservas.isEmpty()) {
                    Text(
                        text = "No hay reservas para esta sesión",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(reservas, key = { it.idCliente }) { reserva ->
                            ClienteItem(
                                nombre = "${reserva.nombre} ${reserva.apellidos}".trim(),
                                telefono = reserva.telefono,
                                estado = reserva.estado,
                                foto = reserva.foto,
                                esMoroso = reserva.idCliente in morososIds,
                                onClick = {
                                    navController.navigate(
                                        Routes.perfilCliente(reserva.idCliente)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * FilaDatoSesionReserva
 * ---------------------
 * Fila reutilizable con icono y texto en el resumen de la sesión.
 */
@Composable
private fun FilaDatoSesionReserva(
    icono: ImageVector,
    texto: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = Color(0xFF1E88E5),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
