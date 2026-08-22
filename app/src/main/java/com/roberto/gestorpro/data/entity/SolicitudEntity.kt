package com.roberto.gestorpro.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.roberto.gestorpro.data.converter.StringListConverter
import com.roberto.gestorpro.model.EstadoSolicitud
import com.roberto.gestorpro.model.TipoSolicitud

/**
 * SolicitudEntity.kt
 * ------------------
 * ✔ TIPO: archivo de código fuente Kotlin (entidad de datos)
 * Es el archivo que define la entidad de la tabla de solicitudes de la base de datos.
 * Sirve para que Room cree la tabla "solicitud" donde se guardan las peticiones
 * de los clientes (por ejemplo pedir plaza en una clase o pedir la baja).
 */

/**
 * @Entity(tableName = "solicitud", ...)
 * --------------------------------------
 * ✔ TIPO: anotación (androidx.room.Entity)
 * Es la anotación que marca esta clase como tabla de la base de datos.
 * Sirve para crear la tabla "solicitud" con un índice sobre idCliente
 * para acelerar las búsquedas de solicitudes por cliente.
 */
@Entity(
    tableName = "solicitud",
    indices = [Index(value = ["idCliente"])]
)

/**
 * SolicitudEntity
 * ---------------
 * ✔ TIPO: data class (entidad de Room con anotación @Entity)
 * Es la clase que mapea una solicitud a una fila de la base de datos.
 * Sirve para registrar qué pidió cada cliente, cuándo y en qué estado está.
 */
@TypeConverters(StringListConverter::class)
data class SolicitudEntity(

    /**
     * idSolicitud
     * -----------
     * ✔ TIPO: propiedad (val) → Int (clave primaria)
     * Es el identificador único de la solicitud en la base de datos.
     * Sirve como clave primaria (@PrimaryKey) con valor autogenerado por Room.
     */
    @PrimaryKey(autoGenerate = true)
    val idSolicitud: Int = 0,

    /**
     * negocioId
     * ---------
     * ✔ TIPO: propiedad (val) → String
     * Es el identificador del negocio al que pertenece la solicitud.
     * Sirve para preparar la app a varios negocios; hoy se guarda vacío
     * igual que en las sesiones de clase.
     */
    val negocioId: String = "",

    /**
     * idCliente
     * ---------
     * ✔ TIPO: propiedad (val) → Int
     * Es el identificador del cliente que hace la solicitud.
     * Sirve para relacionar la solicitud con su fila en la tabla cliente.
     */
    val idCliente: Int,

    /**
     * tipo
     * ----
     * ✔ TIPO: propiedad (val) → TipoSolicitud
     * Es el tipo de petición (CLASE o BAJA).
     * Sirve para clasificar la solicitud; Room lo guarda como texto con TipoSolicitudConverter.
     */
    val tipo: TipoSolicitud,

    /**
     * estado
     * ------
     * ✔ TIPO: propiedad (val) → EstadoSolicitud
     * Es el estado de tramitación (PENDIENTE, ACEPTADA o RECHAZADA).
     * Sirve para que el administrador sepa qué solicitudes aún debe atender;
     * Room lo guarda como texto con EstadoSolicitudConverter.
     */
    val estado: EstadoSolicitud = EstadoSolicitud.PENDIENTE,

    /**
     * detalle
     * -------
     * ✔ TIPO: propiedad (val) → List<String>
     * Es la lista de textos con los detalles de la solicitud
     * (por ejemplo los nombres de las clases pedidas).
     * Sirve para guardar información variable sin más columnas;
     * Room la almacena como texto separado por comas con StringListConverter.
     */
    val detalle: List<String> = emptyList(),

    /**
     * fechaCreacion
     * -------------
     * ✔ TIPO: propiedad (val) → Long
     * Es el momento de creación de la solicitud en milisegundos.
     * Sirve para ordenar las solicitudes y saber su antigüedad.
     */
    val fechaCreacion: Long = System.currentTimeMillis()
)
