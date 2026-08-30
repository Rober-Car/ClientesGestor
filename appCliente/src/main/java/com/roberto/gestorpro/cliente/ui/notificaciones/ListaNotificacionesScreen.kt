package com.roberto.gestorpro.cliente.ui.notificaciones

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController

private data class NotificacionVisual(
    val titulo: String,
    val mensaje: String,
    val fecha: String,
    val icono: androidx.compose.ui.graphics.vector.ImageVector,
    val leida: Boolean
)

private val notificacionesDeEjemplo = listOf(
    NotificacionVisual(
        titulo = "Recordatorio de clase",
        mensaje = "Tu clase de CrossFit comienza a las 18:00.",
        fecha = "Hoy · 17:00",
        icono = Icons.Default.Event,
        leida = false
    ),
    NotificacionVisual(
        titulo = "Nuevo aviso del gimnasio",
        mensaje = "El horario de esta semana ha cambiado.",
        fecha = "Ayer",
        icono = Icons.Default.Notifications,
        leida = true
    ),
    NotificacionVisual(
        titulo = "Información",
        mensaje = "Recuerda revisar tus próximas actividades.",
        fecha = "28/08/2026",
        icono = Icons.Default.Info,
        leida = true
    ),
    NotificacionVisual(
        titulo = "Aviso de entrenamiento",
        mensaje = "Consulta las recomendaciones para tu próxima sesión.",
        fecha = "27/08/2026",
        icono = Icons.Default.FitnessCenter,
        leida = true
    )
)

/**
 * Pantalla visual de la lista de notificaciones recibidas.
 * Los elementos son solo datos de ejemplo y no se guardan ni se leen de Firebase.
 */
@Composable
fun ListaNotificacionesScreen(navController: NavHostController) {
    val morado = Color(0xFF7E57C2)

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
                        text = "Notificaciones",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (notificacionesDeEjemplo.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EstadoVacioNotificaciones()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Notificaciones",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(notificacionesDeEjemplo) { notificacion ->
                        NotificacionCard(notificacion, morado)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificacionCard(
    notificacion: NotificacionVisual,
    morado: Color
) {
    val colorIcono = if (notificacion.leida) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        morado
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notificacion.leida) {
                MaterialTheme.colorScheme.surfaceContainerLow
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .size(8.dp)
                    .background(
                        color = if (notificacion.leida) Color.Transparent else morado,
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = notificacion.icono,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notificacion.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (notificacion.leida) FontWeight.SemiBold else FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notificacion.mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = notificacion.fecha,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorIcono
                )
            }
        }
    }
}

@Composable
private fun EstadoVacioNotificaciones() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = Color(0xFF7E57C2),
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "No tienes notificaciones",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Cuando recibas un aviso aparecerá aquí.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EstadoVacioNotificacionesPreview() {
    MaterialTheme {
        EstadoVacioNotificaciones()
    }
}
