package com.roberto.clientesgestor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.roberto.clientesgestor.model.EstadoCliente
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import java.io.File

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
    /**
     * nombre
     * ------
     * ✔ TIPO: parámetro (param) → String
     * Es el nombre completo del cliente.
     * Sirve para mostrarlo en la tarjeta del cliente.
     */
    nombre: String,

    /**
     * telefono
     * --------
     * ✔ TIPO: parámetro (param) → String
     * Es el teléfono de contacto del cliente.
     * Sirve para mostrarlo en la tarjeta del cliente junto a su icono.
     */
    telefono: String,

    /**
     * estado
     * ------
     * ✔ TIPO: parámetro (param) → EstadoCliente (enum)
     * Es el estado actual del cliente.
     * Sirve para colorear la barra lateral y el texto del estado.
     */
    estado: EstadoCliente,

    foto: String,

    /**
     * esMoroso
     * --------
     * ✔ TIPO: parámetro (param) → Boolean
     * Indica si el cliente es moroso (calculado desde movimientos).
     * Sirve para mostrar un borde rojo en la foto cuando el cliente tiene pagos vencidos.
     */
    esMoroso: Boolean = false,

    /**
     * onClick
     * -------
     * ✔ TIPO: parámetro (param) → () -> Unit (lambda)
     * Es la acción que se ejecuta al pulsar la tarjeta del cliente.
     * Sirve para navegar al perfil del cliente al tocarlo.
     */
    onClick: () -> Unit,

    /**
     * modifier
     * --------
     * ✔ TIPO: parámetro (param) → Modifier
     * Es el modificador opcional que recibe la tarjeta.
     * Sirve para ajustar tamaño, relleno o posición de ClienteItem desde fuera.
     */
    modifier: Modifier = Modifier
) {

    /**
     * textoEstado
     * -----------
     * ✔ TIPO: variable inmutable (val) → String
     * Es el texto legible del estado del cliente.
     * Sirve para traducir el enum a "Activo", "Moroso" o "Baja".
     */
    val textoEstado = when (estado) {
        EstadoCliente.ACTIVO -> "Activo"
        EstadoCliente.MOROSO -> "Moroso"
        EstadoCliente.BAJA -> "Baja"
        EstadoCliente.REGISTRADO -> "Registrado"
    }

    /**
     * colorEstado
     * -----------
     * ✔ TIPO: variable inmutable (val) → Color
     * Es el color asociado al estado del cliente.
     * Sirve para pintar la barra lateral de la tarjeta según el estado.
     */
    val colorEstado = if (esMoroso) {
        Color.Red
    } else when (estado) {
        EstadoCliente.ACTIVO -> Color(0xFF4CAF50)
        EstadoCliente.MOROSO -> Color.Red
        EstadoCliente.BAJA -> Color.Gray
        EstadoCliente.REGISTRADO -> Color.White
    }

    /**
     * Card
     * ----
     * ✔ TIPO: función @Composable (androidx.compose.material3.Card)
     * Es la tarjeta clicable que envuelve los datos del cliente.
     * Sirve para mostrar el cliente con esquinas redondeadas, elevación
     * y fondo blanco, ejecutando onClick al pulsarla.
     */
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {

        /**
         * Row del contenido
         * -----------------
         * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Row)
         * Es la fila horizontal de la tarjeta.
         * Sirve para alinear la barra de estado, la foto y los textos del cliente.
         */
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {

            /**
             * Box de la barra de estado
             * -------------------------
             * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Box)
             * Es la barra vertical de color situada al inicio de la tarjeta.
             * Sirve para indicar de un vistazo el estado del cliente con su color.
             */
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(colorEstado)
            )

            /**
             * Foto del cliente
             * ----------------
             * ✔ TIPO: bloque condicional (if/else) + AsyncImage o Box con icono
             * Es la foto del cliente en formato cuadrado con esquinas redondeadas y borde azul.
             * Sirve como imagen representativa del cliente en la lista; si no hay foto,
             * se muestra un cuadrado de relleno con el icono de persona.
             */
                if (foto.isNotEmpty()) {
                    AsyncImage(
                        model = File(foto),
                        contentDescription = "Foto del cliente",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, Color(0xFF64B5F6), RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(2.dp, Color(0xFF64B5F6), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Icono de persona",
                            tint = Color(0xFF64B5F6),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

            /**
             * Column de textos
             * ----------------
             * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Column)
             * Es el bloque vertical que agrupa nombre y teléfono del cliente.
             * Sirve para mostrar los textos del cliente apilados en la tarjeta.
             */
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {

                /**
                 * Text del nombre
                 * ---------------
                 * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
                 * Es el nombre del cliente dentro de la tarjeta.
                 * Sirve para identificarlo visualmente en la lista.
                 */
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                /**
                 * Row del teléfono
                 * ----------------
                 * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Row)
                 * Es la fila que junta el icono de teléfono con su número.
                 * Sirve para mostrar el teléfono del cliente de forma compacta.
                 */
                Row() {

                    /**
                     * Icon de teléfono
                     * ----------------
                     * ✔ TIPO: función @Composable (androidx.compose.material3.Icon)
                     * Es el icono de teléfono del cliente.
                     * Sirve como indicador visual del dato de contacto.
                     */
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Icono de telefono",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    /**
                     * Text del teléfono
                     * -----------------
                     * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
                     * Es el número de teléfono del cliente.
                     * Sirve para mostrar el dato de contacto junto a su icono.
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
