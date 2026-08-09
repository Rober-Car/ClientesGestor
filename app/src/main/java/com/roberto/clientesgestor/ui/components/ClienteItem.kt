package com.roberto.clientesgestor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.roberto.clientesgestor.model.EstadoCliente

/**
 * ClienteItem.kt
 * --------------
 * ✔ TIPO: archivo de código fuente Kotlin (componente reutilizable)
 * Es el archivo que define el componente ClienteItem.
 * Sirve para representar cada cliente dentro de una lista en la aplicación.
 */

/**
 * ClienteItem
 * -----------
 * ✔ TIPO: función @Composable (componente reutilizable)
 * Es un componente que encapsula una Card con los datos de un cliente.
 * Sirve para mostrar cada cliente de forma uniforme en una lista.
 */
@Composable
fun ClienteItem(
    nombre: String,
    telefono: String,
    estado: EstadoCliente,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textoEstado = when (estado) {
        EstadoCliente.ACTIVO -> "Activo"
        EstadoCliente.MOROSO -> "Moroso"
        EstadoCliente.BAJA -> "Baja"
    }

    val colorEstado = when (estado) {
        EstadoCliente.ACTIVO -> Color(0xFF4CAF50)
        EstadoCliente.MOROSO -> Color.Red
        EstadoCliente.BAJA -> Color.Gray
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(colorEstado)
            )

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Icono de persona",
                tint = Color(0xFF64B5F6),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(50.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Row() {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Icono de telefono",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = telefono,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
