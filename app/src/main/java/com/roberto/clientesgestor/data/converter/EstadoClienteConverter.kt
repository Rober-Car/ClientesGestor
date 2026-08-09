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

    @TypeConverter
    fun fromEstadoCliente(estado: EstadoCliente): String {
        return estado.name
    }

    @TypeConverter
    fun toEstadoCliente(estado: String): EstadoCliente {
        return EstadoCliente.valueOf(estado)
    }
}
