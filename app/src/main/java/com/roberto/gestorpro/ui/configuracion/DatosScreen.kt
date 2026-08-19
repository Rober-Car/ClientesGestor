package com.roberto.gestorpro.ui.configuracion

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.roberto.gestorpro.ui.viewmodel.DatosViewModel

@Composable
fun DatosScreen(
    navController: NavHostController,
    viewModel: DatosViewModel = hiltViewModel()
) {
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val mostrarDialogoRestaurar by viewModel.mostrarDialogoRestaurar.collectAsStateWithLifecycle()

    val exportarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportarDatos(uri)
        }
    }

    val importarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importarDatos(uri)
        }
    }

    val restaurarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.solicitarRestaurar(uri)
        }
    }

    if (mostrarDialogoRestaurar) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelarRestaurar() },
            title = { Text("¿Restaurar copia?") },
            text = {
                Text("Los datos actuales serán reemplazados por los datos del archivo.")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmarRestaurar() }) {
                    Text("RESTAURAR", color = Color(0xFF1E88E5))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelarRestaurar() }) {
                    Text("CANCELAR", color = Color(0xFF1E88E5))
                }
            }
        )
    }

    Scaffold { innerPadding ->
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
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Datos",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Text(
                text = "Gestión de datos",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1E88E5),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            DatosItem(
                titulo = "Exportar datos",
                descripcion = "Guardar datos en archivo",
                icono = Icons.Default.CloudUpload,
                onClick = { exportarLauncher.launch("gestorpro_backup.json") }
            )

            DatosItem(
                titulo = "Importar datos",
                descripcion = "Añadir datos desde archivo",
                icono = Icons.Default.CloudDownload,
                onClick = { importarLauncher.launch(arrayOf("application/json")) }
            )

            DatosItem(
                titulo = "Restaurar copia de seguridad",
                descripcion = "Reemplazar datos actuales",
                icono = Icons.Default.Restore,
                onClick = { restaurarLauncher.launch(arrayOf("application/json")) }
            )

            if (mensaje != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = mensaje!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun DatosItem(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = Color(0xFF1E88E5),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF1E88E5),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
