package com.roberto.gestorpro.ui.servicios

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.roberto.gestorpro.data.entity.ServicioEntity
import com.roberto.gestorpro.navigation.Routes
import com.roberto.gestorpro.ui.viewmodel.ServicioViewModel

/**
 * ServiciosScreen
 * ---------------
 * Pantalla principal de gestión de SERVICIOS del ADMIN.
 * Muestra los servicios separados en ACTIVOS y DE BAJA, y permite
 * crear, editar, dar de baja, reactivar y eliminar servicios.
 */
@Composable
fun ServiciosScreen(
    navController: NavHostController,
    viewModel: ServicioViewModel = hiltViewModel()
) {
    val activos by viewModel.activos.collectAsStateWithLifecycle()
    val inactivos by viewModel.inactivos.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val errorSincronizacion by viewModel.errorSincronizacion.collectAsStateWithLifecycle()
    val servicioSinSincronizar by viewModel.servicioSinSincronizar.collectAsStateWithLifecycle()

    var servicioDarDeBaja by remember { mutableStateOf<ServicioEntity?>(null) }
    var servicioEliminar by remember { mutableStateOf<ServicioEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarServicios()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.CREAR_SERVICIO) },
                containerColor = Color(0xFF1E88E5)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear servicio",
                    tint = Color.White
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                    text = "Servicios",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            error?.let { mensaje ->
                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            if (errorSincronizacion != null || servicioSinSincronizar != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = errorSincronizacion
                                ?: "Hay cambios pendientes de sincronizar con la nube.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        OutlinedButton(
                            onClick = { viewModel.reintentarSincronizacion() },
                            enabled = servicioSinSincronizar != null
                        ) {
                            Text("Reintentar sincronización")
                        }
                    }
                }
            }

            if (activos.isEmpty() && inactivos.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(
                        text = "No hay servicios creados.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Pulsa + para crear el primer servicio",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item { TituloSeccion("ACTIVOS") }
                    if (activos.isEmpty()) {
                        item { TextoVacio("No hay servicios activos") }
                    } else {
                        items(activos, key = { it.idServicio }) { servicio ->
                            ServicioCard(
                                servicio = servicio,
                                onEntrar = {
                                    navController.navigate(Routes.detalleServicio(servicio.idServicio))
                                },
                                onEditar = {
                                    navController.navigate(Routes.editarServicio(servicio.idServicio))
                                },
                                onDarDeBaja = { servicioDarDeBaja = servicio },
                                onReactivar = { viewModel.reactivar(servicio) },
                                onEliminar = { servicioEliminar = servicio }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.size(8.dp)) }
                    item { TituloSeccion("DE BAJA") }
                    if (inactivos.isEmpty()) {
                        item { TextoVacio("No hay servicios de baja") }
                    } else {
                        items(inactivos, key = { it.idServicio }) { servicio ->
                            ServicioCard(
                                servicio = servicio,
                                onEntrar = {
                                    navController.navigate(Routes.detalleServicio(servicio.idServicio))
                                },
                                onEditar = {
                                    navController.navigate(Routes.editarServicio(servicio.idServicio))
                                },
                                onDarDeBaja = { },
                                onReactivar = { viewModel.reactivar(servicio) },
                                onEliminar = { servicioEliminar = servicio }
                            )
                        }
                    }
                }
            }
        }
    }

    if (servicioDarDeBaja != null) {
        AlertDialog(
            onDismissRequest = { servicioDarDeBaja = null },
            title = { Text("Dar de baja servicio") },
            text = {
                Text(
                    "¿Seguro que quieres dar de baja \"${servicioDarDeBaja!!.nombre}\"? " +
                        "Se eliminarán sus sesiones futuras y las reservas asociadas. " +
                        "Las sesiones pasadas se conservan."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.darDeBaja(servicioDarDeBaja!!)
                        servicioDarDeBaja = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text("Dar de baja")
                }
            },
            dismissButton = {
                TextButton(onClick = { servicioDarDeBaja = null }) { Text("Cancelar") }
            }
        )
    }

    if (servicioEliminar != null) {
        AlertDialog(
            onDismissRequest = { servicioEliminar = null },
            title = { Text("Eliminar servicio") },
            text = {
                Text(
                    "¿Seguro que quieres eliminar \"${servicioEliminar!!.nombre}\"? " +
                        "Se eliminarán todas sus sesiones y reservas. Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminar(servicioEliminar!!)
                        servicioEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { servicioEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

/**
 * TituloSeccion
 * -------------
 * Encabezado de las secciones ACTIVOS / DE BAJA.
 */
@Composable
private fun TituloSeccion(titulo: String) {
    Text(
        text = titulo,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
    )
}

/**
 * TextoVacio
 * ----------
 * Mensaje de sección vacía.
 */
@Composable
private fun TextoVacio(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Gray,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

/**
 * formatearPrecioServicio
 * -----------------------
 * Precio del servicio legible: "30 €" cuando es entero y "12,50 €" cuando
 * tiene decimales. No añade redondeo ni cambia el valor almacenado.
 */
private fun formatearPrecioServicio(precio: Double): String {
    return if (precio % 1.0 == 0.0) {
        "${precio.toInt()} €"
    } else {
        "${String.format(java.util.Locale.ROOT, "%.2f", precio).replace('.', ',')} €"
    }
}

/**
 * ServicioCard
 * ------------
 * Tarjeta de un servicio.
 * - Activo: acciones secundarias "Editar" y "Dar de baja".
 * - De baja: acciones secundarias "Reactivar" y "Eliminar" (sin editar).
 * Jerarquía visual: el icono y el nombre del servicio son el elemento
 * principal; las acciones quedan como botones de texto discretos al pie.
 */
@Composable
private fun ServicioCard(
    servicio: ServicioEntity,
    onEntrar: () -> Unit,
    onEditar: () -> Unit,
    onDarDeBaja: () -> Unit,
    onReactivar: () -> Unit,
    onEliminar: () -> Unit
) {
    val colorPrimario = if (servicio.activo) Color(0xFF1E88E5) else Color.Gray
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEntrar() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (servicio.activo) {
                Color(0xFF1E88E5).copy(alpha = 0.08f)
            } else {
                Color.Gray.copy(alpha = 0.08f)
            }
        ),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colorPrimario.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = colorPrimario,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = servicio.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (servicio.descripcion.isNotBlank()) {
                        Text(
                            text = servicio.descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatearPrecioServicio(servicio.precio),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorPrimario,
                    maxLines = 1
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (servicio.activo) {
                    TextButton(
                        onClick = onEditar,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar")
                    }
                    TextButton(
                        onClick = onDarDeBaja,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Dar de baja", color = Color(0xFFFF9800))
                    }
                } else {
                    TextButton(
                        onClick = onReactivar,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Reactivar", color = Color(0xFF4CAF50))
                    }
                    TextButton(
                        onClick = onEliminar,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar", color = Color.Red)
                    }
                }
            }
        }
    }
}
