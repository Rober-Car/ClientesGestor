package com.roberto.gestorpro.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.roberto.gestorpro.data.entity.MovimientoEntity
import com.roberto.gestorpro.model.EstadoMovimiento
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * MovimientoItem.kt
 * -----------------
 * ✔ TIPO: archivo de código fuente Kotlin (componente reutilizable)
 * Es el archivo que define el componente MovimientoItem.
 * Sirve para representar cada movimiento (servicio) dentro de una lista en la aplicación.
 */

/**
 * MovimientoItem
 * --------------
 * ✔ TIPO: función @Composable (componente reutilizable)
 * Es un componente que encapsula una Card con los datos de un movimiento.
 * Sirve para mostrar cada servicio contratado por un cliente de forma uniforme en una lista.
 */
@Composable
fun MovimientoItem(
    /**
     * movimiento
     * ----------
     * ✔ TIPO: parámetro (param) → MovimientoEntity
     * Es el movimiento (servicio) que se va a mostrar en la tarjeta.
     * Sirve para acceder a todos sus datos: servicio, fechas, precio y estado.
     */
    movimiento: MovimientoEntity,

    /**
     * nombreServicios
     * ---------------
     * ✔ TIPO: parámetro (param) → String
     * Es el nombre legible de los servicios del movimiento (resuelto fuera del
     * componente contra el catálogo de ServicioEntity). Como el componente está
     * sin consumidores, se muestra vacío por defecto.
     */
    nombreServicios: String = "",

    /**
     * onClick
     * -------
     * ✔ TIPO: parámetro (param) → () -> Unit (lambda)
     * Es la acción que se ejecuta al pulsar la tarjeta del movimiento.
     * Sirve para navegar al detalle o edición del movimiento al tocarlo.
     */
    onClick: () -> Unit,

    /**
     * modifier
     * --------
     * ✔ TIPO: parámetro (param) → Modifier
     * Es el modificador opcional que recibe la tarjeta.
     * Sirve para ajustar tamaño, relleno o posición de MovimientoItem desde fuera.
     */
    modifier: Modifier = Modifier
) {

    /**
     * colorEstado
     * -----------
     * ✔ TIPO: variable inmutable (val) → Color
     * Es el color asociado al estado del movimiento.
     * Sirve para pintar la barra lateral de la tarjeta según el estado (naranja o verde).
     */
    val colorEstado = when (movimiento.estado) {
        EstadoMovimiento.PENDIENTE -> Color(0xFFFF9800)
        EstadoMovimiento.PAGADO -> Color(0xFF4CAF50)
    }

    /**
     * textoEstado
     * -----------
     * ✔ TIPO: variable inmutable (val) → String
     * Es el texto legible del estado del movimiento.
     * Sirve para traducir el enum a "Pendiente" o "Pagado".
     */
    val textoEstado = when (movimiento.estado) {
        EstadoMovimiento.PENDIENTE -> "Pendiente"
        EstadoMovimiento.PAGADO -> "Pagado"
    }

    /**
     * formatter
     * ---------
     * ✔ TIPO: variable inmutable (val) → DateTimeFormatter
     * Es el formateador de fechas con patrón "dd/MM/yyyy".
     * Sirve para convertir los timestamps de inicio y fin a texto legible.
     */
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    /**
     * fechaInicioStr
     * --------------
     * ✔ TIPO: variable inmutable (val) → String
     * Es la fecha de inicio del servicio ya formateada.
     * Sirve para mostrar cuándo comenzó el servicio en la tarjeta.
     */
    val fechaInicioStr = Instant.ofEpochMilli(movimiento.fechaInicio)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(formatter)

    /**
     * fechaFinStr
     * -----------
     * ✔ TIPO: variable inmutable (val) → String
     * Es la fecha de fin del servicio ya formateada.
     * Sirve para mostrar cuándo finalizó el servicio en la tarjeta.
     */
    val fechaFinStr = Instant.ofEpochMilli(movimiento.fechaFin)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(formatter)

    /**
     * Card
     * ----
     * ✔ TIPO: función @Composable (androidx.compose.material3.Card)
     * Es la tarjeta clicable que envuelve los datos del movimiento.
     * Sirve para mostrar el servicio con esquinas redondeadas, elevación
     * y fondo blanco, ejecutando onClick al pulsarla.
     */
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {

        /**
         * Row del contenido
         * -----------------
         * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Row)
         * Es la fila horizontal de la tarjeta del movimiento.
         * Sirve para alinear los datos del servicio dentro de la tarjeta.
         */
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            /**
             * Column de datos
             * ---------------
             * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Column)
             * Es el bloque vertical que agrupa nombre, fechas, precio y estado.
             * Sirve para mostrar los datos del servicio apilados en la tarjeta.
             */
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {

                /**
                 * Row de servicio y precio
                 * ------------------------
                 * Fila que muestra el nombre del servicio a la izquierda
                 * y el precio a la derecha, en la misma línea.
                 */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = nombreServicios,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${movimiento.precioFinal} €",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                /**
                 * Row de fechas y estado
                 * ----------------------
                 * Fila que muestra las fechas del servicio a la izquierda
                 * y el estado (Pendiente/Pagado) a la derecha, en la misma línea.
                 */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$fechaInicioStr - $fechaFinStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = textoEstado,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorEstado
                    )
                }
            }
        }
    }
}
