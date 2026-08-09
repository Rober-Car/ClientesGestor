package com.roberto.clientesgestor.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * ServicioItem.kt
 * ---------------
 * ✔ TIPO: archivo de código fuente Kotlin (componente reutilizable)
 * Es el archivo que define el componente ServicioItem.
 * Sirve para representar cada servicio contratado por un cliente en su perfil.
 */

/**
 * ServicioItem
 * ------------
 * ✔ TIPO: función @Composable (componente reutilizable)
 * Es un componente que muestra una fila con el icono y el nombre de un servicio.
 * Sirve para listar los servicios contratados del cliente de forma uniforme.
 */
@Composable
fun ServicioItem(
    nombreServicio: String,
    iconoServicio: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(
            imageVector = iconoServicio,
            contentDescription = nombreServicio,
            tint = Color(0xFF64B5F6),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = nombreServicio,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
