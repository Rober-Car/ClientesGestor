package com.roberto.clientesgestor.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.roberto.clientesgestor.model.EstadoMovimiento

/**
 * MovimientoEntity.kt
 * -------------------
 * ✔ TIPO: archivo de código fuente Kotlin (entidad Room)
 * Es el archivo que define la entidad de la tabla de movimientos (servicios) en la base de datos.
 * Sirve para que Room pueda almacenar y recuperar los servicios contratados por cada cliente.
 */

/**
 * @Entity(tableName = "movimiento")
 * ---------------------------------
 * ✔ TIPO: anotación (androidx.room.Entity)
 * Es la anotación que marca esta data class como una tabla de la base de datos Room.
 * Sirve para que Room genere automáticamente la tabla "movimiento" con sus columnas.
 */

/**
 * MovimientoEntity
 * ----------------
 * ✔ TIPO: data class (entidad Room)
 * Es la entidad que representa un servicio o movimiento asociado a un cliente.
 * Sirve para almacenar los datos de cada servicio contratado: fechas, precio, estado y observaciones.
 */
@Entity(tableName = "movimiento")
data class MovimientoEntity(

    /**
     * idMovimiento
     * ------------
     * ✔ TIPO: propiedad (val) → Int (clave primaria autogenerada)
     * Es el identificador único del movimiento en la base de datos.
     * Sirve para identificar cada servicio de forma unívoca, generándose automáticamente al insertar.
     */
    @PrimaryKey(autoGenerate = true)
    val idMovimiento: Int = 0,

    /**
     * idCliente
     * ---------
     * ✔ TIPO: propiedad (val) → Int
     * Es el identificador del cliente al que pertenece este movimiento.
     * Sirve para relacionar cada servicio con el cliente que lo contrató.
     */
    val idCliente: Int,

    /**
     * servicio
     * --------
     * ✔ TIPO: propiedad (val) → String
     * Es el nombre o descripción del servicio prestado al cliente.
     * Sirve para identificar qué tipo de servicio se realizó (ej. instalación, mantenimiento, etc.).
     */
    val servicio: String,

    /**
     * fechaInicio
     * -----------
     * ✔ TIPO: propiedad (val) → Long
     * Es la fecha de inicio del servicio en formato de milisegundos (timestamp).
     * Sirve para registrar cuándo comenzó el servicio prestado al cliente.
     */
    val fechaInicio: Long,

    /**
     * fechaFin
     * --------
     * ✔ TIPO: propiedad (val) → Long
     * Es la fecha de fin del servicio en formato de milisegundos (timestamp).
     * Sirve para registrar cuándo finalizó el servicio prestado al cliente.
     */
    val fechaFin: Long,

    /**
     * precio
     * ------
     * ✔ TIPO: propiedad (val) → Double
     * Es el precio acordado para el servicio prestado.
     * Sirve para registrar el coste del servicio que el cliente deberá abonar.
     */
    val precio: Double,

    /**
     * estado
     * ------
     * ✔ TIPO: propiedad (val) → EstadoMovimiento
     * Es el estado actual del movimiento (PENDIENTE o PAGADO).
     * Sirve para saber si el servicio ya ha sido abonado o sigue pendiente de cobro.
     */
    val estado: EstadoMovimiento,

    /**
     * fechaPago
     * ---------
     * ✔ TIPO: propiedad (val) → Long? (nullable, por defecto null)
     * Es la fecha en que se realizó el pago del servicio en formato de milisegundos.
     * Sirve para registrar cuándo abonó el cliente; es null si el pago aún no se ha realizado.
     */
    val fechaPago: Long? = null,

    /**
     * observaciones
     * -------------
     * ✔ TIPO: propiedad (val) → String? (nullable, por defecto null)
     * Son las notas o comentarios adicionales sobre el servicio.
     * Sirve para anotar detalles extra del movimiento que no encajan en los demás campos.
     */
    val observaciones: String? = null

)
