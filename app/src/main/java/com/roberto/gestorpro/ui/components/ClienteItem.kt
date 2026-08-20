package com.roberto.gestorpro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.roberto.gestorpro.model.EstadoCliente
import java.io.File

@Composable
fun ClienteItem(
    nombre: String,
    telefono: String,
    estado: EstadoCliente,
    foto: String,
    esMoroso: Boolean = false,
    onClick: () -> Unit,
    onArchivar: (() -> Unit)? = null,
    onRestaurar: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var mostrarDialogoArchivar by remember { mutableStateOf(false) }
    var mostrarDialogoRestaurar by remember { mutableStateOf(false) }

    val colorEstado = when {
        esMoroso -> Color.Red
        estado == EstadoCliente.ACTIVO -> Color(0xFF4CAF50)
        estado == EstadoCliente.BAJA -> Color.Gray
        estado == EstadoCliente.ARCHIVADO -> Color(0xFFFF9800)
        else -> Color(0xFF1E88E5)
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorEstado.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (foto.isNotEmpty()) {
                AsyncImage(
                    model = File(foto),
                    contentDescription = "Foto del cliente",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, colorEstado, RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorEstado.copy(alpha = 0.15f))
                        .border(2.dp, colorEstado, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Icono de persona",
                        tint = colorEstado,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.bodyLarge
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Telefono",
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = telefono,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            if (onArchivar != null) {
                IconButton(onClick = { mostrarDialogoArchivar = true }) {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = "Archivar cliente",
                        tint = Color(0xFFFF9800)
                    )
                }
            }

            if (onRestaurar != null) {
                IconButton(onClick = { mostrarDialogoRestaurar = true }) {
                    Icon(
                        imageVector = Icons.Default.Unarchive,
                        contentDescription = "Restaurar cliente",
                        tint = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }

    if (mostrarDialogoArchivar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoArchivar = false },
            title = { Text("Archivar cliente") },
            text = { Text("¿Seguro que quieres archivar a $nombre? No aparecerá en la lista principal, pero podrás restaurarlo más adelante.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoArchivar = false
                    onArchivar?.invoke()
                }) {
                    Text("Archivar", color = Color(0xFFFF9800))
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoArchivar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarDialogoRestaurar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoRestaurar = false },
            title = { Text("Restaurar cliente") },
            text = { Text("¿Seguro que quieres restaurar a $nombre? Volverá a aparecer en la lista principal como activo.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoRestaurar = false
                    onRestaurar?.invoke()
                }) {
                    Text("Restaurar", color = Color(0xFF4CAF50))
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoRestaurar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
