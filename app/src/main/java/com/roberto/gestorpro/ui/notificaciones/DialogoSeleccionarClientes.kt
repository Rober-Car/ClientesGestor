package com.roberto.gestorpro.ui.notificaciones

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import com.roberto.gestorpro.model.Cliente

/**
 * ModoSeleccion
 * -------------
 * Cómo se seleccionan los clientes en el diálogo: uno solo (INDIVIDUAL) o
 * varios (GRUPO).
 */
enum class ModoSeleccion {
    UNO,
    MUCHOS
}

/**
 * DialogoSeleccionarClientes
 * --------------------------
 * Diálogo para elegir clientes como destinatarios de una notificación.
 * - UNO: selección única con RadioButton (destino INDIVIDUAL).
 * - MUCHOS: selección múltiple con Checkbox (destino GRUPO).
 *
 * Reutiliza el patrón de selección con Checkbox de
 * DialogoEditarServiciosContratados (Admin).
 */
@Composable
fun DialogoSeleccionarClientes(
    clientes: List<Cliente>,
    seleccionadosIniciales: Set<Int>,
    modoSeleccion: ModoSeleccion,
    onDismiss: () -> Unit,
    onConfirmar: (Set<Int>) -> Unit
) {
    var seleccionados by remember {
        mutableStateOf(seleccionadosIniciales)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (modoSeleccion == ModoSeleccion.UNO) {
                    "Seleccionar cliente"
                } else {
                    "Seleccionar clientes"
                },
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            if (clientes.isEmpty()) {
                Text(
                    text = "No hay clientes disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(clientes, key = { it.idCliente }) { cliente ->
                        val seleccionado = cliente.idCliente in seleccionados
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    seleccionados = when (modoSeleccion) {
                                        ModoSeleccion.UNO -> setOf(cliente.idCliente)
                                        ModoSeleccion.MUCHOS ->
                                            if (seleccionado) {
                                                seleccionados - cliente.idCliente
                                            } else {
                                                seleccionados + cliente.idCliente
                                            }
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (modoSeleccion == ModoSeleccion.UNO) {
                                RadioButton(
                                    selected = seleccionado,
                                    onClick = {
                                        seleccionados = setOf(cliente.idCliente)
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF1E88E5)
                                    )
                                )
                            } else {
                                Checkbox(
                                    checked = seleccionado,
                                    onCheckedChange = { marcado ->
                                        seleccionados = if (marcado) {
                                            seleccionados + cliente.idCliente
                                        } else {
                                            seleccionados - cliente.idCliente
                                        }
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cliente.nombre,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmar(seleccionados) },
                enabled = seleccionados.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E88E5),
                    contentColor = Color.White
                )
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
