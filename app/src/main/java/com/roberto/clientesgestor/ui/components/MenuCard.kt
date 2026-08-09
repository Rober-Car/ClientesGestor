package com.roberto.clientesgestor.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

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
    /**
     * titulo
     * ------
     * ✔ TIPO: parámetro (param) → String
     * Es el título de la opción del menú.
     * Sirve para mostrar el nombre de la sección en la tarjeta.
     */
    titulo: String,

    /**
     * descripcion
     * -----------
     * ✔ TIPO: parámetro (param) → String
     * Es la descripción breve de la opción del menú.
     * Sirve para explicar qué hace cada sección en la tarjeta.
     */
    descripcion: String,

    /**
     * icono
     * -----
     * ✔ TIPO: parámetro (param) → ImageVector
     * Es el icono que representa la sección del menú.
     * Sirve para mostrar visualmente cada opción en la tarjeta.
     */
    icono: ImageVector,

    /**
     * onClick
     * -------
     * ✔ TIPO: parámetro (param) → () -> Unit (lambda)
     * Es la acción que se ejecuta al pulsar la tarjeta del menú.
     * Sirve para navegar a la sección correspondiente al tocarla.
     */
    onClick: () -> Unit
) {

    /**
     * Card
     * ----
     * ✔ TIPO: función @Composable (androidx.compose.material3.Card)
     * Es la tarjeta clicable que envuelve la opción del menú.
     * Sirve para mostrar cada sección con esquinas redondeadas,
     * elevación y fondo blanco, ejecutando onClick al pulsarla.
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
         * Row del contenido
         * -----------------
         * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Row)
         * Es la fila horizontal de la tarjeta del menú.
         * Sirve para alinear el icono circular, los textos y la flecha.
         */
        Row(
            modifier = Modifier.padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            /**
             * Surface circular
             * ----------------
             * ✔ TIPO: función @Composable (androidx.compose.material3.Surface)
             * Es el círculo blanco que enmarca el icono de la sección.
             * Sirve como fondo redondeado para que el icono destaque.
             */
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp
            ) {

                /**
                 * Box del icono
                 * -------------
                 * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Box)
                 * Es el contenedor que centra el icono dentro del círculo.
                 * Sirve para situar el icono en el centro del Surface.
                 */
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    /**
                     * Icon de la sección
                     * ------------------
                     * ✔ TIPO: función @Composable (androidx.compose.material3.Icon)
                     * Es el icono que representa la opción del menú.
                     * Sirve para identificar visualmente cada sección.
                     */
                    Icon(
                        imageVector = icono,
                        contentDescription = "Clientes",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            /**
             * Column de textos
             * ----------------
             * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Column)
             * Es el bloque vertical que agrupa título y descripción.
             * Sirve para mostrar los textos de la opción apilados en la tarjeta.
             */
            Column(
                modifier = Modifier.weight(1f)
            ) {

                /**
                 * Text del título
                 * ---------------
                 * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
                 * Es el título de la sección del menú.
                 * Sirve para nombrar la opción dentro de la tarjeta.
                 */
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                /**
                 * Text de la descripción
                 * ----------------------
                 * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
                 * Es la descripción breve de la sección del menú.
                 * Sirve para explicar la utilidad de la opción dentro de la tarjeta.
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
             * Icon de flecha
             * --------------
             * ✔ TIPO: función @Composable (androidx.compose.material3.Icon)
             * Es la flecha de la derecha de la tarjeta.
             * Sirve para indicar que la opción lleva a otra pantalla.
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
