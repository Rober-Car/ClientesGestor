package com.roberto.gestorpro.cliente.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.roberto.gestorpro.cliente.navigation.Routes
import com.roberto.gestorpro.cliente.model.EstadoIndicadorCliente
import com.roberto.gestorpro.cliente.ui.viewmodel.MainViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    val idCliente by mainViewModel.idCliente.collectAsStateWithLifecycle()
    val nombreNegocio by mainViewModel.nombreNegocio.collectAsStateWithLifecycle()
    val logoNegocio by mainViewModel.logoNegocio.collectAsStateWithLifecycle()
    val estadoHome by mainViewModel.estadoHome.collectAsStateWithLifecycle()
    val vinculado = idCliente != null

    LifecycleResumeEffect(idCliente) {
        if (idCliente != null) {
            mainViewModel.refrescarEstadoHome()
        }
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
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (logoNegocio.isNotBlank()) {
                    AsyncImage(
                        model = logoNegocio,
                        contentDescription = "Logo del gimnasio",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "GestorPro Cliente",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (vinculado) {
                        nombreNegocio.ifBlank { "Tu gimnasio" }
                    } else {
                        "Todavía no estás vinculado a un gimnasio"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!vinculado) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "No estás vinculado con tu gimnasio.\n" +
                            "Debes vincularte para ver las clases.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }

            val estadoVisual = when (estadoHome.estado) {
                EstadoIndicadorCliente.ACTIVO -> EstadoVisualCliente.ACTIVO
                EstadoIndicadorCliente.PAGO_VENCIDO -> EstadoVisualCliente.PAGO_VENCIDO
                EstadoIndicadorCliente.BAJA -> EstadoVisualCliente.BAJA
                EstadoIndicadorCliente.REGISTRADO -> EstadoVisualCliente.REGISTRADO
                EstadoIndicadorCliente.ARCHIVADO -> EstadoVisualCliente.ARCHIVADO
                null -> null
            }

            if (vinculado && estadoVisual != null) {
                HomeClientEstadoIndicator(
                    estado = estadoVisual,
                    fecha = estadoHome.fechaRelevante?.let(::formatearFecha) ?: when {
                        estadoVisual == EstadoVisualCliente.ACTIVO ||
                            estadoVisual == EstadoVisualCliente.PAGO_VENCIDO ||
                            estadoVisual == EstadoVisualCliente.BAJA ->
                            "Fecha no disponible"
                        else -> null
                    },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    HomeClientMenuCard(
                        titulo = "Clases",
                        descripcion = "Consulta y reserva",
                        icono = Icons.Default.FitnessCenter,
                        color = Color(0xFFFB8C00),
                        onClick = { navController.navigate(Routes.CLASES) }
                    )
                }
                item {
                    HomeClientMenuCard(
                        titulo = "Notificaciones",
                        descripcion = "Consulta tus avisos",
                        icono = Icons.Default.Notifications,
                        color = Color(0xFF7E57C2),
                        onClick = { navController.navigate(Routes.NOTIFICACIONES) }
                    )
                }
                item {
                    HomeClientMenuCard(
                        titulo = "Ajustes",
                        descripcion = "Configuración general",
                        icono = Icons.Default.Settings,
                        color = Color(0xFF78909C),
                        onClick = { navController.navigate(Routes.CONFIGURACION) }
                    )
                }
                item {
                    HomeClientMenuCard(
                        titulo = "Rutinas",
                        descripcion = "Rutinas de entrenamiento",
                        icono = Icons.Default.FitnessCenter,
                        color = Color(0xFF26A69A),
                        onClick = { navController.navigate(Routes.RUTINAS) }
                    )
                }
            }
        }
    }
}

/**
 * HomeClientMenuCard
 * ------------------
 * Tarjeta de navegación del Home de GestorPro Cliente.
 *
 * Comparte el mismo concepto visual que la MenuCard de GestorPro Admin:
 * composición vertical (icono arriba en contenedor coloreado, título y
 * descripción debajo), proporción compacta y bordes suaves. Es un componente
 * privado del Home para no alterar la MenuCard compartida (usada en otras
 * pantallas como Cuenta).
 */
@Composable
private fun HomeClientMenuCard(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    color: Color = Color(0xFF1E88E5),
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * EstadoVisualCliente
 * -------------------
 * Modelo visual (sin datos) de los tres estados que puede mostrar el indicador
 * de estado del cliente en el Home de GestorPro Cliente. Solo se usa para la
 * presentación; la conexión con Firestore corresponde a la Fase 2.
 */
private enum class EstadoVisualCliente {
    ACTIVO, PAGO_VENCIDO, BAJA, REGISTRADO, ARCHIVADO
}

/**
 * HomeClientEstadoIndicator
 * -------------------------
 * Bloque visual, neutro y discreto, que muestra el estado del cliente en el
 * Home: una bola de color a la izquierda y el texto de estado (con protagonismo)
 * junto a la fecha (secundaria). El fondo es neutro; solo la bola y el título
 * adoptan el color semántico. Queda preparado para recibir estado y fecha reales
 * en la Fase 2 sin alterar su presentación.
 */
@Composable
private fun HomeClientEstadoIndicator(
    estado: EstadoVisualCliente,
    fecha: String?,
    modifier: Modifier = Modifier
) {
    val (color, titulo, prefijo) = when (estado) {
        EstadoVisualCliente.ACTIVO ->
            Triple(Color(0xFF43A047), "Activo", "Hasta el")
        EstadoVisualCliente.PAGO_VENCIDO ->
            Triple(Color(0xFFE53935), "Pago vencido", "Venció el")
        EstadoVisualCliente.BAJA ->
            Triple(Color(0xFF78909C), "Baja", "Desde el")
        EstadoVisualCliente.REGISTRADO ->
            Triple(Color(0xFF64B5F6), "Registrado", null)
        EstadoVisualCliente.ARCHIVADO ->
            Triple(Color(0xFF78909C), "Archivado", null)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (prefijo != null && fecha != null) {
                    Text(
                        text = "$prefijo $fecha",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatearFecha(millis: Long): String = Instant.ofEpochMilli(millis)
    .atZone(ZoneId.systemDefault())
    .toLocalDate()
    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

@Preview(showBackground = true)
@Composable
fun HomeClientEstadoIndicatorPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            HomeClientEstadoIndicator(EstadoVisualCliente.ACTIVO, "31/08/2026")
            Spacer(modifier = Modifier.height(12.dp))
            HomeClientEstadoIndicator(EstadoVisualCliente.PAGO_VENCIDO, "10/07/2026")
            Spacer(modifier = Modifier.height(12.dp))
            HomeClientEstadoIndicator(EstadoVisualCliente.BAJA, "05/03/2026")
        }
    }
}
