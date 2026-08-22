package com.roberto.gestorpro.data.converter

import androidx.room.TypeConverter
import com.roberto.gestorpro.model.TipoSolicitud

/**
 * TipoSolicitudConverter.kt
 * -------------------------
 * ✔ TIPO: archivo de código fuente Kotlin (conversor de datos)
 * Es el archivo que define el conversor entre TipoSolicitud y String.
 * Sirve para que Room pueda guardar el enum TipoSolicitud como texto en la base de datos.
 */

/**
 * TipoSolicitudConverter
 * ----------------------
 * ✔ TIPO: clase (conversor de Room con anotación @TypeConverter)
 * Es la clase que convierte el tipo de solicitud entre enum y String.
 * Sirve para que Room pueda almacenar y recuperar TipoSolicitud de forma persistente,
 * igual que hace EstadoClienteConverter con el estado del cliente.
 */
class TipoSolicitudConverter {

    /**
     * fromTipoSolicitud
     * -----------------
     * ✔ TIPO: método (fun) → String
     * Es la función que convierte un TipoSolicitud en su nombre como String.
     * Sirve para guardar el tipo de solicitud como texto en la base de datos.
     */
    @TypeConverter
    fun fromTipoSolicitud(tipo: TipoSolicitud): String {
        return tipo.name
    }

    /**
     * toTipoSolicitud
     * ---------------
     * ✔ TIPO: método (fun) → TipoSolicitud
     * Es la función que convierte un String en su TipoSolicitud correspondiente.
     * Sirve para recuperar el tipo de solicitud desde la base de datos como enum.
     */
    @TypeConverter
    fun toTipoSolicitud(tipo: String): TipoSolicitud {
        return TipoSolicitud.valueOf(tipo)
    }
}
