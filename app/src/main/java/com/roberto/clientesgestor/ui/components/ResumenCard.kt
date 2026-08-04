package com.roberto.clientesgestor.ui.components

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
    titulo: String,
    cantidad: Int,
    estaSeleccionada: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val containerColor = if (estaSeleccionada) {
        Color(0xFFE0E0E0)
    } else {
        Color.White
    }

    val elevacion = if (estaSeleccionada) {
        CardDefaults.cardElevation(
            defaultElevation = 10.dp)

    }else{
        CardDefaults.cardElevation(
            defaultElevation = 6.dp)
    }

    /**
     * Card
     * ----
     * ✔ TIPO: Card (contenedor Material Design)
     * Es un contenedor visual con elevación y bordes que agrupa contenido.
     * Sirve para mostrar la tarjeta de resumen de forma clicable.
     */
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = elevacion,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {

        /**
         * Column
         * ------
         * ✔ TIPO: Column (layout vertical)
         * Es un contenedor que coloca elementos uno debajo del otro.
         * Sirve para apilar la barra, el título y la cantidad.
         */
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /**
             * Surface (barra de color)
             * ------------------------
             * ✔ TIPO: Surface
             * Es una barra de color que indica el estado del resumen.
             * Sirve para diferenciar visualmente cada tarjeta.
             */
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = color
            ) {}

            /**
             * Text(titulo)
             * ------------
             * ✔ TIPO: Text
             * Es el título del resumen.
             * Sirve para identificar el estado representado por la tarjeta.
             */
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(4.dp)
            )

            /**
             * Spacer
             * ------
             * ✔ TIPO: Spacer
             * Es un elemento invisible que ocupa espacio.
             * Sirve para separar el título de la cantidad.
             */
            Spacer(
                modifier = Modifier.height(8.dp)
            )

            /**
             * Text(cantidad)
             * --------------
             * ✔ TIPO: Text
             * Es el número de clientes del resumen.
             * Sirve para mostrar la cantidad asociada al estado.
             */
            Text(
                text = cantidad.toString(),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

