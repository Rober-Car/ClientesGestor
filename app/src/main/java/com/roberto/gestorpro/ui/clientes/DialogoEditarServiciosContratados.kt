package com.roberto.gestorpro.ui.clientes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.roberto.gestorpro.data.entity.ServicioEntity

/**
 * DialogoEditarServiciosContratados
 * ---------------------------------
 * Diálogo para que el ADMIN elija los servicios ACTIVOS que tiene contratados
 * un cliente. Permite seleccionar varios sin límite.
 *
 * Los ids contratados cuyo servicio ya no esté activo (dado de baja o
 * eliminado) NO se muestran en el selector, pero se conservan en la lista
 * resultante: la gestión de baja del servicio es responsabilidad aparte.
 */
@Composable
fun DialogoEditarServiciosContratados(
    contratadosActuales: List<Int>,
    serviciosActivos: List<ServicioEntity>,
    onDismiss: () -> Unit,
    onGuardar: (List<Int>) -> Unit
) {
    val idsActivos = serviciosActivos.map { it.idServicio }.toSet()
    // Se preseleccionan los servicios activos que ya tiene contratados.
    var seleccionados by remember {
        mutableStateOf(contratadosActuales.filter { it in idsActivos }.toSet())
    }

    // Ids contratados que ya no están entre los activos: se conservan tal cual.
    val preservados = contratadosActuales.filterNot { it in idsActivos }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar servicios",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            if (serviciosActivos.isEmpty()) {
                Text(
                    text = "No hay servicios activos disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(serviciosActivos, key = { it.idServicio }) { servicio ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = servicio.idServicio in seleccionados,
                                onCheckedChange = { marcado ->
                                    seleccionados = if (marcado) {
                                        seleccionados + servicio.idServicio
                                    } else {
                                        seleccionados - servicio.idServicio
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = servicio.nombre,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGuardar((preservados + seleccionados).distinct())
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E88E5),
                    contentColor = Color.White
                )
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
