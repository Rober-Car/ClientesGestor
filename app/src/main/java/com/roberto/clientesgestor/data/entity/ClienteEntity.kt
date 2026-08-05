package com.roberto.clientesgestor.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.roberto.clientesgestor.model.EstadoCliente



/**
 * ClienteEntity
 * -------------
 * ✔ TIPO: data class (entidad de Room con anotación @Entity)
 * Es la clase que mapea un cliente a una tabla de la base de datos.
 * Sirve para que Room pueda guardar y recuperar clientes de forma persistente,
 * creando una fila en la tabla por cada instancia de esta clase.
 */

/**
 * @Entity
 * -------
 * ✔ TIPO: anotación (androidx.room.Entity)
 * Es la anotación que marca esta clase como tabla de la base de datos.
 * Sirve para que Room cree una tabla llamada "ClienteEntity" y una columna por cada propiedad.
 */
@Entity
data class ClienteEntity(

    /**
     * idUsuario
     * ---------
     * ✔ TIPO: propiedad (val) → Int
     * Es el identificador único del cliente en la base de datos.
     * Sirve como clave primaria (@PrimaryKey) de la tabla de clientes:
     * con autoGenerate = true Room asigna un valor automático e incremental a cada nuevo cliente.
     */
    @PrimaryKey(autoGenerate = true)

    val idUsuario: Int = 0,

    /**
     * nombre
     * ------
     * ✔ TIPO: propiedad (val) → String
     * Es el nombre del cliente.
     * Sirve para guardar el nombre del cliente en la base de datos.
     */
    val nombre: String,

    /**
     * apellidos
     * ---------
     * ✔ TIPO: propiedad (val) → String
     * Es el apellido o apellidos del cliente.
     * Sirve para guardar los apellidos del cliente en la base de datos.
     */
    val apellidos: String,

    /**
     * dni
     * ---
     * ✔ TIPO: propiedad (val) → String
     * Es el DNI del cliente.
     * Sirve para guardar el documento de identidad del cliente en la base de datos.
     */
    val dni: String,

    /**
     * telefono
     * --------
     * ✔ TIPO: propiedad (val) → String
     * Es el teléfono del cliente.
     * Sirve para guardar el número de contacto del cliente en la base de datos.
     */
    val telefono: String,

    /**
     * email
     * -----
     * ✔ TIPO: propiedad (val) → String?
     * Es el email del cliente, puede ser nulo si no se ha registrado.
     * Sirve para guardar el correo de contacto del cliente en la base de datos;
     * al ser nullable, Room permite guardar NULL en esa columna.
     */
    val email: String?,

    /**
     * estado
     * ------
     * ✔ TIPO: propiedad (val) → EstadoCliente
     * Es el estado del cliente (ACTIVO, MOROSO o BAJA).
     * Sirve para saber la situación del cliente en el gestor;
     * al ser un enum, Room lo guarda como texto gracias al conversor EstadoClienteConverter.
     */
    val estado: EstadoCliente,

    /**
     * tieneLlave
     * ----------
     * ✔ TIPO: propiedad (val) → Boolean
     * Es el indicador de si el cliente tiene llave de acceso al gimnasio.
     * Sirve para saber si el cliente dispone de llave física.
     */
    val tieneLlave: Boolean


)
