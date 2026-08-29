package com.roberto.gestorpro.cliente.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.roberto.gestorpro.cliente.ui.viewmodel.SesionVisible
import com.roberto.gestorpro.cliente.ui.viewmodel.SesionesClienteViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * ClasesScreen
 * ------------
 * Pantalla "Clases de hoy" del CLIENTE.
 * Muestra exclusivamente las sesiones del DÍA ACTUAL de los servicios
 * contratados y activos del cliente, ordenadas por hora. No permite navegar
 * entre días ni reservar (la reserva es una fase posterior).
 */
@Composable
fun ClasesScreen(
    navController: NavHostController,
    viewModel: SesionesClienteViewModel = hiltViewModel()
) {
    val cargando by viewModel.cargando.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val noVinculado by viewModel.noVinculado.collectAsStateWithLifecycle()
    val sinServicios by viewModel.sinServicios.collectAsStateWithLifecycle()
    val sinSesionesHoy by viewModel.sinSesionesHoy.collectAsStateWithLifecycle()
    val sesiones by viewModel.sesiones.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.cargar()
    }

    val formateadorFecha = remember {
        DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy", Locale("es", "ES"))
    }
    val fechaHoy = remember {
        LocalDate.now()
            .format(formateadorFecha)
            .replaceFirstChar { it.titlecase() }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                    Column {
                        Text(
                            text = "Clases de hoy",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = fechaHoy,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            when {
                cargando -> CargandoClases(
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
                noVinculado -> MensajeClases(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    texto = "No estás vinculado con tu gimnasio.\n" +
                        "Debes vincularte para poder ver las clases."
                )
                error != null -> ErrorClases(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    mensaje = error ?: "",
                    onReintentar = { viewModel.reintentar() }
                )
                sinServicios -> MensajeClases(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    texto = "No tienes servicios contratados."
                )
                sinSesionesHoy -> MensajeClases(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    texto = "No hay clases programadas para hoy."
                )
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sesiones, key = { it.idSesion }) { sesion ->
                            TarjetaSesion(sesion)
                        }
                    }
                }
            }
        }
    }
}

/**
 * TarjetaSesion
 * -------------
 * Tarjeta de una sesión de hoy: nombre del servicio, hora y duración, y estado
 * de plazas ("Completa" si no quedan). No incluye reserva todavía.
 */
@Composable
private fun TarjetaSesion(sesion: SesionVisible) {
    val completa = sesion.plazasDisponibles <= 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = sesion.nombreServicio,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${sesion.hora} · ${sesion.duracionMinutos} min",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (completa) {
                Text(
                    text = "Completa",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text(
                    text = "${sesion.plazasDisponibles} plazas disponibles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * CargandoClases
 * --------------
 * Estado de carga (spinner centrado en el espacio restante).
 */
@Composable
private fun CargandoClases(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * MensajeClases
 * -------------
 * Estado informativo centrado (cliente no vinculado, sin servicios, sin
 * sesiones hoy) dentro de una tarjeta, con el mismo estilo del placeholder.
 */
@Composable
private fun MensajeClases(modifier: Modifier = Modifier, texto: String) {
    Box(
        modifier = modifier.padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            )
        }
    }
}

/**
 * ErrorClases
 * -----------
 * Estado de error centrado con botón de reintento.
 */
@Composable
private fun ErrorClases(
    modifier: Modifier = Modifier,
    mensaje: String,
    onReintentar: () -> Unit
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onReintentar) {
            Text("Reintentar")
        }
    }
}
