package com.roberto.gestorpro.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.roberto.gestorpro.model.Cliente
import com.roberto.gestorpro.model.EstadoCliente

/**
 * ClienteEntity.kt
 * ----------------
 * ✔ TIPO: archivo de código fuente Kotlin (entidad de datos)
 * Es el archivo que define la entidad de la tabla de clientes de la base de datos.
 * Sirve para que Room cree la tabla de clientes a partir de esta clase y la mapee al modelo de UI.
 */

/**
 * @Entity(tableName = "cliente", indices = [Index(value = ["dni"], unique = true)])
 * ---------------------------------------------------------------------------------
 * ✔ TIPO: anotación (androidx.room.Entity)
 * Es la anotación que marca esta clase como tabla de la base de datos.
 * Sirve para que Room cree una tabla llamada "cliente" con una columna por cada propiedad,
 * añadiendo además un índice único sobre la columna "dni" para evitar DNI duplicados.
 */
@Entity(
    tableName = "cliente",
    indices = [Index(value = ["dni"], unique = true)]
)

/**
 * ClienteEntity
 * -------------
 * ✔ TIPO: data class (entidad de Room con anotación @Entity)
 * Es la clase que mapea un cliente a una tabla de la base de datos.
 * Sirve para que Room pueda guardar y recuperar clientes de forma persistente,
 * creando una fila en la tabla por cada instancia de esta clase.
 */
data class ClienteEntity(

    /**
     * idCliente
     * ---------
     * ✔ TIPO: propiedad (val) → Int (clave primaria)
     * Es el identificador único del cliente en la base de datos.
     * Sirve como clave primaria (@PrimaryKey) de la tabla de clientes:
     * con autoGenerate = true Room asigna un valor automático e incremental a cada nuevo cliente.
     */
    @PrimaryKey(autoGenerate = true)
    val idCliente: Int = 0,
    val nombre: String,
    val apellidos: String,
    val dni: String,
    val telefono: String,
    val email: String? = null,
    val foto: String,
    val fechaNacimiento: Long,
    val fechaRegistro: Long = System.currentTimeMillis(),
    val fechaAlta: Long? = null,
    val fechaBaja: Long? = null,
    val estado: EstadoCliente,
    val observaciones: String? = null,
    val negocioId: String? = null,
    val serviciosContratados: List<Int>,
    val firebaseUid: String? = null,

    /**
     * moroso: indicador INDEPENDIENTE del estado administrativo (ACTIVO/BAJA).
     * false por defecto; la lógica pura de MovimientoMorosidad lo recalcula.
     */
    val moroso: Boolean = false,

    /**
     * fechaEntradaMorosidad: fechaFin del periodo que provocó la entrada en
     * morosidad (null si el cliente no es moroso). NO se reinicia en cada
     * recálculo mientras siga moroso.
     */
    val fechaEntradaMorosidad: Long? = null
)


//significa:
//
//Crear una función que pertenece a ClienteEntity y que convierte un ClienteEntity en un Cliente.

fun ClienteEntity.toCliente(): Cliente {
    return Cliente(
        idCliente = idCliente,
        nombre = "$nombre $apellidos",
        telefono = telefono,
        email = email,
        dni = dni,
        foto = foto,
        fechaNacimiento = fechaNacimiento,
        estado = estado,
        observaciones = observaciones,
        serviciosContratados = serviciosContratados,
        moroso = moroso,
        fechaEntradaMorosidad = fechaEntradaMorosidad
    )
}
