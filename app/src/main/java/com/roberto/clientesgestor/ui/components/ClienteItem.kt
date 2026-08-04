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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.roberto.clientesgestor.model.Estado

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
    estado: Estado,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    /**
     * textoEstado
     * -----------
     * ✔ TIPO: variable inmutable (val) → String
     * Es el texto legible del estado del cliente mediante when(estado).
     * Sirve para mostrar "Activo", "Moroso" o "Baja" en la tarjeta.
     */
    val textoEstado = when (estado) {
        Estado.ACTIVO -> "Activo"
        Estado.MOROSO -> "Moroso"
        Estado.BAJA -> "Baja"
    }

    /**
     * colorEstado
     * -----------
     * ✔ TIPO: variable inmutable (val) → Color
     * Es el color asociado al estado del cliente mediante when(estado).
     * Sirve para colorear visualmente el estado del cliente.
     */
    val colorEstado = when (estado) {
        Estado.ACTIVO -> Color(0xFF4CAF50)
        Estado.MOROSO -> Color.Red
        Estado.BAJA -> Color.Gray
    }

    /**
     * Card
     * ----
     * ✔ TIPO: Card (contenedor Material Design)
     * Es un contenedor visual con elevación, bordes y color configurable.
     * Sirve para agrupar la información del cliente en un bloque visual único.
     */
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {

            /**
             * Row (barra y contenido)
             * -----------------------
             * ✔ TIPO: Row (layout horizontal)
             * Es una fila que contiene la barra de color y los datos del cliente.
             * Sirve para que la barra y el contenido compartan la misma altura.
             *
             * Modifier.height(IntrinsicSize.Min):
             * ✔ TIPO: modificador de layout
             * Es un modificador que fija la altura del Row según la altura mínima de sus hijos.
             * Sirve para que el Box con fillMaxHeight() pueda llenar toda la altura del Row.
             */
            Row(
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {

                /**
                 * Box (barra de color)
                 * -------------------
                 * ✔ TIPO: Box (layout básico)
                 * Es una barra vertical estrecha coloreada con el color del estado.
                 * Sirve para indicar de un vistazo el estado del cliente (verde, rojo o gris).
                 *
                 * .width(4.dp) → ancho de la barra.
                 * .fillMaxHeight() → la barra se estira hasta la altura del Row.
                 * .background(colorEstado) → pinta la barra con el color del estado.
                 */
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .fillMaxHeight()
                        .background(colorEstado)
                )


                /**
                 * Icon (persona)
                 * --------------
                 * ✔ TIPO: Icon
                 * Es el icono vectorial que representa al cliente.
                 * Sirve para identificar visualmente al cliente.
                 */
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Icono de persona",
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(50.dp)
                )

                /**
                 * Column
                 * ------
                 * ✔ TIPO: Column (layout vertical)
                 * Es un contenedor que coloca elementos uno debajo del otro.
                 * Sirve para organizar el contenido del cliente.
                 */
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {


                        /**
                         * Text(nombre)
                         * ------------
                         * ✔ TIPO: Text
                         * Es el nombre del cliente.
                         * Sirve para identificar al cliente dentro de la lista.
                         */
                        Text(
                            text = nombre,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )

                    /**
                     * Spacer
                     * ------
                     * ✔ TIPO: Spacer
                     * Es un elemento invisible que ocupa espacio.
                     * Sirve para separar el icono del texto.
                     */
                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )


                    /**
                     * Row (teléfono)
                     * --------------
                     * ✔ TIPO: Row (layout horizontal)
                     * Es un contenedor que coloca elementos uno al lado del otro.
                     * Sirve para mostrar el icono y el teléfono juntos.
                     */
                    Row() {

                        /**
                         * Icon (teléfono)
                         * ---------------
                         * ✔ TIPO: Icon
                         * Es el icono que representa el teléfono del cliente.
                         * Sirve para identificar visualmente el campo de teléfono.
                         */
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Icono de telefono",
                            tint = Color(0xFF64B5F6),
                            modifier = Modifier.size(25.dp)
                        )

                        /**
                         * Spacer
                         * ------
                         * ✔ TIPO: Spacer
                         * Es un elemento invisible que ocupa espacio.
                         * Sirve para separar el icono del teléfono del número.
                         */
                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        /**
                         * Text(telefono)
                         * --------------
                         * ✔ TIPO: Text
                         * Es el número de teléfono del cliente.
                         * Sirve para mostrar información de contacto.
                         */
                        Text(
                            text = telefono,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
    }
}
