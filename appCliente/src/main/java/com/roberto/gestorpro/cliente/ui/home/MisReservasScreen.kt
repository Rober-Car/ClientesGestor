package com.roberto.gestorpro.cliente.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.roberto.gestorpro.cliente.ui.viewmodel.ReservaVisible
import com.roberto.gestorpro.cliente.ui.viewmodel.ReservasClienteViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Muestra las reservas futuras activas del cliente. */
@Composable
fun MisReservasScreen(
    navController: NavHostController,
    viewModel: ReservasClienteViewModel = hiltViewModel()
) {
    val cargando by viewModel.cargando.collectAsStateWithLifecycle()
    val operando by viewModel.operando.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val noVinculado by viewModel.noVinculado.collectAsStateWithLifecycle()
    val reservas by viewModel.reservasVisibles.collectAsStateWithLifecycle()
    var reservaParaCancelar by remember { mutableStateOf<ReservaVisible?>(null) }

    LifecycleResumeEffect(Unit) {
        viewModel.cargar()
        onPauseOrDispose { }
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
                    Text(
                        text = "Mis reservas",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (error != null && reservas.isNotEmpty()) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            when {
                cargando -> BoxCargandoReservas()
                noVinculado -> MensajeReservas("No estás vinculado con tu gimnasio.")
                error != null && reservas.isEmpty() -> ErrorReservas(
                    mensaje = error ?: "No se pudieron cargar tus reservas",
                    onReintentar = viewModel::cargar
                )
                reservas.isEmpty() -> MensajeReservas("No tienes reservas próximas.")
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reservas, key = { it.reserva.idReserva }) { reserva ->
                        TarjetaReserva(
                            reserva = reserva,
                            operando = operando,
                            onCancelar = { reservaParaCancelar = reserva }
                        )
                    }
                }
            }
        }
    }

    reservaParaCancelar?.let { reserva ->
        AlertDialog(
            onDismissRequest = { reservaParaCancelar = null },
            title = { Text("Cancelar reserva") },
            text = { Text("¿Quieres cancelar esta reserva?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelar(reserva.sesion.idSesion)
                        reservaParaCancelar = null
                    },
                    enabled = !operando
                ) {
                    Text("Cancelar reserva")
                }
            },
            dismissButton = {
                TextButton(onClick = { reservaParaCancelar = null }) {
                    Text("Volver")
                }
            }
        )
    }
}

@Composable
private fun TarjetaReserva(
    reserva: ReservaVisible,
    operando: Boolean,
    onCancelar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = reserva.nombreServicio,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = formatearFechaSesion(reserva.sesion.fecha),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${reserva.sesion.hora} · ${reserva.sesion.duracionMinutos} min",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Estado: Programada",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onCancelar,
                enabled = !operando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (operando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Cancelar reserva")
                }
            }
        }
    }
}

@Composable
private fun BoxCargandoReservas() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MensajeReservas(texto: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorReservas(
    mensaje: String,
    onReintentar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onReintentar) {
            Text("Reintentar")
        }
    }
}

private fun formatearFechaSesion(millis: Long): String = try {
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy", Locale.forLanguageTag("es-ES")))
        .replaceFirstChar { it.titlecase(Locale.forLanguageTag("es-ES")) }
} catch (_: Exception) {
    "Fecha no disponible"
}
