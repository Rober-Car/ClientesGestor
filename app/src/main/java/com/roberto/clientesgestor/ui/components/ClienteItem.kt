package com.roberto.clientesgestor.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.roberto.clientesgestor.model.Estado

@Composable
fun ClienteItem(

    nombre: String,
    telefono: String,
    estado: Estado,
    onClick: () -> Unit,
    modifier: Modifier = Modifier

) {

    val textoEstado = when(estado){
        Estado.ACTIVO  -> "Activo"
        Estado.MOROSO -> "Moroso"
        Estado.BAJA  -> "Baja"
    }

    val colorEstado = when (estado) {
        Estado.ACTIVO -> Color.Green
        Estado.MOROSO -> Color.Red
        Estado.BAJA -> Color.Gray
    }

    /**
     * Card
     * ----
     * ✔ TIPO: Card (contenedor Material Design)
     * Es un contenedor visual con elevación, bordes y color configurable.
     * Sirve para agrupar información del cliente en un bloque visual único.
     *
     * Sintaxis:
     * Card(
     *     onClick = ...,
     *     modifier = ...,
     *     shape = ...,
     *     elevation = CardDefaults.cardElevation(...),
     *     colors = CardDefaults.cardColors(...)
     * ) { contenido }
     *
     * Uso:
     * Se utiliza para representar elementos clicables dentro de listas.
     */
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ){

        /**
         * Column
         * ------
         * ✔ TIPO: Column (layout vertical)
         * Es un contenedor que coloca elementos uno debajo del otro.
         * Sirve para organizar el contenido textual y visual del cliente.
         *
         * (Detalles completos ya explicados en la primera aparición de Column)
         */
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            /**
             * Row
             * ---
             * ✔ TIPO: Row (layout horizontal)
             * Es un contenedor que coloca elementos uno al lado del otro.
             * Sirve para mostrar icono, nombre y estado en una misma línea.
             *
             * (Detalles completos ya explicados en la primera aparición de Row)
             */
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {

                /**
                 * Icon (persona)
                 * --------------
                 * ✔ TIPO: Icon (Composable)
                 * Es un componente que muestra un icono vectorial.
                 * Sirve para representar visualmente al cliente.
                 *
                 * (Detalles completos ya explicados en la primera aparición de Icon)
                 */
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Icono de persona",
                    tint = Color.Gray,
                    modifier = Modifier.size(30.dp)
                )

                /**
                 * Spacer
                 * ------
                 * ✔ TIPO: Spacer
                 * Es un elemento invisible que ocupa espacio.
                 * Sirve para separar el icono del texto.
                 *
                 * (Detalles completos ya explicados en la primera aparición de Spacer)
                 */
                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                /**
                 * Text(nombre)
                 * ------------
                 * ✔ TIPO: Text
                 * Es el nombre del cliente.
                 * Sirve para identificar al cliente dentro de la lista.
                 *
                 * (Detalles completos ya explicados en la primera aparición de Text)
                 */
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                /**
                 * Text(textoEstado)
                 * -----------------
                 * ✔ TIPO: Text
                 * Es el estado del cliente (Activo, Moroso, Baja).
                 * Sirve para mostrar su situación actual.
                 *
                 * (Detalles completos ya explicados en la primera aparición de Text)
                 */
                Text(
                    text = textoEstado,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            /**
             * Row (teléfono)
             * --------------
             * ✔ TIPO: Row
             * Es un contenedor horizontal.
             * Sirve para mostrar icono y número de teléfono juntos.
             *
             * (Detalles completos ya explicados en la primera aparición de Row)
             */
            Row() {

                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Icono de telefono",
                    tint = Color.Gray,
                    modifier = Modifier.size(30.dp)
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                /**
                 * Text(telefono)
                 * --------------
                 * ✔ TIPO: Text
                 * Es el número de teléfono del cliente.
                 * Sirve para mostrar información de contacto.
                 *
                 * (Detalles completos ya explicados en la primera aparición de Text)
                 */
                Text(
                    text = telefono,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

        }

    }
}
