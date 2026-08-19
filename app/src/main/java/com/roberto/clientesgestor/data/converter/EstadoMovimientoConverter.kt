package com.roberto.clientesgestor.data.converter

import androidx.room.TypeConverter
import com.roberto.clientesgestor.model.EstadoMovimiento

/**
 * EstadoMovimientoConverter.kt
 * ----------------------------
 * ✔ TIPO: archivo de código fuente Kotlin (conversor de datos)
 * Es el archivo que define el conversor entre EstadoMovimiento y String.
 * Sirve para que Room pueda guardar el enum EstadoMovimiento como texto en la base de datos.
 */

/**
 * EstadoMovimientoConverter
 * -------------------------
 * ✔ TIPO: clase (conversor de Room con anotación @TypeConverter)
 * Es la clase que convierte el estado del movimiento entre enum y String.
 * Sirve para que Room pueda almacenar y recuperar EstadoMovimiento de forma persistente,
 * ya que por defecto Room no sabe guardar enums y necesita convertirlos a texto.
 */
class EstadoMovimientoConverter {

    /**
     * fromEstadoMovimiento
     * --------------------
     * ✔ TIPO: método (fun) → String
     * Es la función que convierte un EstadoMovimiento en su nombre como String.
     * Sirve para guardar el estado del movimiento como texto en la base de datos
     * (ejemplo: EstadoMovimiento.PENDIENTE se guarda como "PENDIENTE").
     */
    @TypeConverter
    fun fromEstadoMovimiento(estado: EstadoMovimiento): String {
        return estado.name
    }

    /**
     * toEstadoMovimiento
     * ------------------
     * ✔ TIPO: método (fun) → EstadoMovimiento
     * Es la función que convierte un String en su EstadoMovimiento correspondiente.
     * Sirve para recuperar el estado del movimiento desde la base de datos como enum
     * (ejemplo: el texto "PAGADO" se convierte en EstadoMovimiento.PAGADO).
     */
    @TypeConverter
    fun toEstadoMovimiento(estado: String): EstadoMovimiento {
        return EstadoMovimiento.valueOf(estado)
    }
}
