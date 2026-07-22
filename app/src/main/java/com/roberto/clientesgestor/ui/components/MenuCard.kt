package com.roberto.clientesgestor.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

@Composable
fun MenuCard(

    titulo: String,
    descripcion : String,
    onClick: () -> Unit

) {

    /**
     * Card
     * ----
     * ✔ TIPO: Card (contenedor Material Design)
     * Es un contenedor visual con elevación y bordes que agrupa contenido.
     * Sirve para crear bloques independientes dentro de la interfaz.
     *
     * Sintaxis:
     * Card(onClick = ..., modifier = Modifier) { contenido }
     *
     * Uso:
     * Se utiliza para secciones que necesitan un agrupamiento visual claro.
     */
    Card(
        onClick = onClick,

        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),

        shape = RoundedCornerShape(20.dp),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        /**
         * Row
         * ---
         * ✔ TIPO: Row (layout horizontal)
         * Es un contenedor que coloca elementos uno al lado del otro.
         * Sirve para organizar contenido horizontalmente dentro de la tarjeta.
         *
         * (Detalles completos ya explicados en la primera aparición de Row)
         */
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            /**
             * Icon
             * ----
             * ✔ TIPO: Icon (Composable)
             * Es un componente que muestra un icono vectorial.
             * Sirve para añadir elementos gráficos representativos.
             *
             * Sintaxis:
             * Icon(imageVector = Icons.Default.X, contentDescription = "...", modifier = ...)
             *
             * Uso:
             * Se utiliza para acompañar texto con un símbolo visual.
             */
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Clientes",
                tint = Color.Blue,
                modifier = Modifier
                    .size(40.dp)
                    .offset(y = (-25).dp)
            )

            Spacer(
                modifier = Modifier.width(16.dp)
            )

            /**
             * Column
             * ------
             * ✔ TIPO: Column (layout vertical)
             * Es un contenedor que coloca elementos uno debajo del otro.
             * Sirve para organizar el texto dentro de la tarjeta.
             *
             * (Detalles completos ya explicados en la primera aparición de Column)
             */
            Column(

                modifier = Modifier.weight(1f)

            ) {

                /**
                 * Text(titulo)
                 * ------------
                 * ✔ TIPO: Text
                 * Es el título de la tarjeta.
                 * Sirve para identificar la sección representada por esta Card.
                 *
                 * (Detalles completos ya explicados en la primera aparición de Text)
                 */
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                /**
                 * Text(descripcion)
                 * -----------------
                 * ✔ TIPO: Text
                 * Es la descripción de la tarjeta.
                 * Sirve para explicar la función o contenido asociado al título.
                 *
                 * (Detalles completos ya explicados en la primera aparición de Text)
                 */
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                )

            }
        }

    }

}
