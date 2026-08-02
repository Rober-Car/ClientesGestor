package com.roberto.clientesgestor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * MenuCard.kt
 * -----------
 * ✔ TIPO: archivo de código fuente Kotlin (componente reutilizable)
 * Es el archivo que define el componente MenuCard usado en el menú principal.
 * Sirve para mostrar las opciones del menú con un diseño consistente.
 */

/**
 * MenuCard
 * --------
 * ✔ TIPO: función @Composable (componente reutilizable)
 * Es un componente que encapsula una Card con icono, título y descripción.
 * Sirve para representar cada opción del menú principal sin repetir código.
 */
@Composable
fun MenuCard(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    onClick: () -> Unit
) {

    /**
     * Card
     * ----
     * ✔ TIPO: Card (contenedor Material Design)
     * Es un contenedor visual con elevación y bordes que agrupa contenido.
     * Sirve para crear bloques independientes dentro de la interfaz.
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
         * Sirve para organizar el contenido horizontalmente dentro de la tarjeta.
         */
        Row(
            modifier = Modifier
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            /**
             * Surface (círculo del icono)
             * --------------------------
             * ✔ TIPO: Surface
             * Es un círculo blanco que envuelve el icono de la tarjeta.
             * Sirve para resaltar visualmente el icono de la opción.
             */
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    /**
                     * Icon
                     * ----
                     * ✔ TIPO: Icon
                     * Es el icono vectorial que representa la opción del menú.
                     * Sirve para acompañar al texto con un símbolo visual.
                     */
                    Icon(
                        imageVector = icono,
                        contentDescription = "Clientes",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            /**
             * Spacer
             * ------
             * ✔ TIPO: Spacer
             * Es un elemento invisible que ocupa espacio.
             * Sirve para separar el icono del texto.
             */
            Spacer(
                modifier = Modifier.width(16.dp)
            )

            /**
             * Column
             * ------
             * ✔ TIPO: Column (layout vertical)
             * Es un contenedor que coloca elementos uno debajo del otro.
             * Sirve para organizar el texto dentro de la tarjeta.
             */
            Column(
                modifier = Modifier.weight(1f)
            ) {

                /**
                 * Text(titulo)
                 * ------------
                 * ✔ TIPO: Text
                 * Es el título de la tarjeta.
                 * Sirve para identificar la sección representada por la Card.
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
                 * Sirve para explicar la función asociada al título.
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

            /**
             * Icon (flecha)
             * -------------
             * ✔ TIPO: Icon
             * Es la flecha que apunta hacia la derecha.
             * Sirve para indicar que la tarjeta es accesible.
             */
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ir a Clientes",
                tint = Color.Gray,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}
