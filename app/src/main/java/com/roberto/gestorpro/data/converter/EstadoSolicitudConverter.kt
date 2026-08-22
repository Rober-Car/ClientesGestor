package com.roberto.gestorpro.data.converter

import androidx.room.TypeConverter
import com.roberto.gestorpro.model.EstadoSolicitud

/**
 * EstadoSolicitudConverter.kt
 * ---------------------------
 * ✔ TIPO: archivo de código fuente Kotlin (conversor de datos)
 * Es el archivo que define el conversor entre EstadoSolicitud y String.
 * Sirve para que Room pueda guardar el enum EstadoSolicitud como texto en la base de datos.
 */

/**
 * EstadoSolicitudConverter
 * ------------------------
 * ✔ TIPO: clase (conversor de Room con anotación @TypeConverter)
 * Es la clase que convierte el estado de la solicitud entre enum y String.
 * Sirve para que Room pueda almacenar y recuperar EstadoSolicitud de forma persistente,
 * siguiendo el mismo patrón que los conversores de EstadoCliente y TipoSolicitud.
 */
class EstadoSolicitudConverter {

    /**
     * fromEstadoSolicitud
     * -------------------
     * ✔ TIPO: método (fun) → String
     * Es la función que convierte un EstadoSolicitud en su nombre como String.
     * Sirve para guardar el estado de la solicitud como texto en la base de datos.
     */
    @TypeConverter
    fun fromEstadoSolicitud(estado: EstadoSolicitud): String {
        return estado.name
    }

    /**
     * toEstadoSolicitud
     * -----------------
     * ✔ TIPO: método (fun) → EstadoSolicitud
     * Es la función que convierte un String en su EstadoSolicitud correspondiente.
     * Sirve para recuperar el estado de la solicitud desde la base de datos como enum.
     */
    @TypeConverter
    fun toEstadoSolicitud(estado: String): EstadoSolicitud {
        return EstadoSolicitud.valueOf(estado)
    }
}
