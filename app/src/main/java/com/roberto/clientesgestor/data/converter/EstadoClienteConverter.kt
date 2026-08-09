package com.roberto.clientesgestor.data.converter

import androidx.room.TypeConverter
import com.roberto.clientesgestor.model.EstadoCliente

/**
 * EstadoClienteConverter.kt
 * -------------------------
 * ✔ TIPO: archivo de código fuente Kotlin (conversor de datos)
 * Es el archivo que define el conversor entre EstadoCliente y String.
 * Sirve para que Room pueda guardar el enum EstadoCliente como texto en la base de datos.
 */

/**
 * EstadoClienteConverter
 * ----------------------
 * ✔ TIPO: clase (conversor de Room con anotación @TypeConverter)
 * Es la clase que convierte el estado del cliente entre enum y String.
 * Sirve para que Room pueda almacenar y recuperar EstadoCliente de forma persistente,
 * ya que por defecto Room no sabe guardar enums y necesita convertirlos a texto.
 */
class EstadoClienteConverter {

    /**
     * fromEstadoCliente
     * -----------------
     * ✔ TIPO: método (fun) → String
     * Es la función que convierte un EstadoCliente en su nombre como String.
     * Sirve para guardar el estado del cliente como texto en la base de datos
     * (ejemplo: EstadoCliente.ACTIVO se guarda como "ACTIVO").
     */
    @TypeConverter
    fun fromEstadoCliente(estado: EstadoCliente): String {
        return estado.name
    }

    /**
     * toEstadoCliente
     * ---------------
     * ✔ TIPO: método (fun) → EstadoCliente
     * Es la función que convierte un String en su EstadoCliente correspondiente.
     * Sirve para recuperar el estado del cliente desde la base de datos como enum
     * (ejemplo: el texto "ACTIVO" se convierte en EstadoCliente.ACTIVO).
     */
    @TypeConverter
    fun toEstadoCliente(estado: String): EstadoCliente {
        return EstadoCliente.valueOf(estado)
    }
}
