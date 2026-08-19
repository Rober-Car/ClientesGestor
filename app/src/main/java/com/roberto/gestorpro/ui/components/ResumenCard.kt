package com.roberto.gestorpro.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * ResumenCard.kt
 * --------------
 * ✔ TIPO: archivo de código fuente Kotlin (componente reutilizable)
 * Es el archivo que define el componente ResumenCard.
 * Sirve para mostrar los resúmenes de clientes (Activos, Bajas, Morosos).
 */

/**
 * ResumenCard
 * -----------
 * ✔ TIPO: función @Composable (componente reutilizable)
 * Es un componente que encapsula una Card con barra de color y cantidad.
 * Sirve para mostrar de un vistazo el número de clientes por estado.
 */
@Composable
fun ResumenCard(
    /**
     * titulo
     * ------
     * ✔ TIPO: parámetro (param) → String
     * Es el título de la tarjeta de resumen.
     * Sirve para indicar qué estado de clientes cuenta (Todos, Activos, Bajas, Morosos).
     */
    titulo: String,

    /**
     * cantidad
     * --------
     * ✔ TIPO: parámetro (param) → Int
     * Es el número de clientes que representa la tarjeta.
     * Sirve para mostrar la cifra destacada de cada resumen.
     */
    cantidad: Int,

    /**
     * estaSeleccionada
     * ----------------
     * ✔ TIPO: parámetro (param) → Boolean
     * Es el indicador de si el filtro de esta tarjeta está activo.
     * Sirve para resaltar la tarjeta seleccionada con otro color de fondo.
     */
    estaSeleccionada: Boolean,

    /**
     * color
     * -----
     * ✔ TIPO: parámetro (param) → Color
     * Es el color de la barra superior de la tarjeta.
     * Sirve para asociar cada resumen a un color (verde, gris, rojo).
     */
    color: Color,

    /**
     * onClick
     * -------
     * ✔ TIPO: parámetro (param) → () -> Unit (lambda)
     * Es la acción que se ejecuta al pulsar la tarjeta de resumen.
     * Sirve para seleccionar el filtro de clientes correspondiente al tocarla.
     */
    onClick: () -> Unit,

    /**
     * modifier
     * --------
     * ✔ TIPO: parámetro (param) → Modifier
     * Es el modificador opcional que recibe la tarjeta.
     * Sirve para ajustar el peso y el relleno de cada ResumenCard desde fuera.
     */
    modifier: Modifier = Modifier
) {

    /**
     * containerColor
     * --------------
     * ✔ TIPO: variable inmutable (val) → Color
     * Es el color de fondo de la tarjeta de resumen.
     * Sirve para resaltar con azul claro la tarjeta seleccionada
     * y dejar en blanco las que no lo están.
     */
    val containerColor = if (estaSeleccionada) {
        Color(0xFFBBDEFB)
    } else {
        Color.White
    }

    /**
     * Card
     * ----
     * ✔ TIPO: función @Composable (androidx.compose.material3.Card)
     * Es la tarjeta clicable de resumen.
     * Sirve para mostrar el resumen con esquinas redondeadas y elevación,
     * ejecutando onClick al pulsarla.
     */
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {

        /**
         * Column del contenido
         * --------------------
         * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Column)
         * Es el bloque vertical de la tarjeta de resumen.
         * Sirve para apilar la barra de color, el título y la cantidad centrados.
         */
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /**
             * Surface de la barra de color
             * ----------------------------
             * ✔ TIPO: función @Composable (androidx.compose.material3.Surface)
             * Es la barra superior de color de la tarjeta.
             * Sirve para identificar el tipo de resumen con su color.
             */
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp),
                color = color
            ) {}

            /**
             * Text del título
             * ---------------
             * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
             * Es el título de la tarjeta de resumen.
             * Sirve para indicar qué estado de clientes representa.
             */
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            /**
             * Text de la cantidad
             * -------------------
             * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
             * Es el número de clientes del resumen.
             * Sirve para mostrar la cifra de forma destacada en la tarjeta.
             */
            Text(
                text = cantidad.toString(),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
