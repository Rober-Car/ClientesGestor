package com.roberto.clientesgestor.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.roberto.clientesgestor.model.Cliente
import com.roberto.clientesgestor.model.EstadoCliente

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

    /**
     * nombre
     * ------
     * ✔ TIPO: propiedad (val) → String
     * Es el nombre propio del cliente.
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
     * Es el documento nacional de identidad del cliente.
     * Sirve para identificar de forma única a cada cliente (índice único de la tabla).
     */
    val dni: String,

    /**
     * password
     * --------
     * ✔ TIPO: propiedad (val) → String
     * Es la contraseña de acceso del cliente.
     * Sirve para que el cliente pueda autenticarse en la aplicación.
     */
    val password: String,

    /**
     * telefono
     * --------
     * ✔ TIPO: propiedad (val) → String
     * Es el número de teléfono de contacto del cliente.
     * Sirve para guardar el teléfono del cliente y poder contactar con él.
     */
    val telefono: String,

    /**
     * email
     * -----
     * ✔ TIPO: propiedad (val) → String? (opcional)
     * Es el correo electrónico de contacto del cliente.
     * Sirve para guardar el email del cliente; puede estar vacío (null) si no lo ha dado.
     */
    val email: String? = null,

    /**
     * foto
     * ----
     * ✔ TIPO: propiedad (val) → String
     * Es la ruta o referencia de la foto de perfil del cliente.
     * Sirve para mostrar la imagen del cliente en su perfil.
     */
    val foto: String,

    /**
     * fechaNacimiento
     * ---------------
     * ✔ TIPO: propiedad (val) → Long (timestamp)
     * Es la fecha de nacimiento del cliente.
     * Sirve para guardar la fecha de nacimiento como milisegundos desde 1970 y calcular su edad.
     */
    val fechaNacimiento: Long,

    /**
     * fechaRegistro
     * -------------
     * ✔ TIPO: propiedad (val) → Long (timestamp)
     * Es la fecha y hora en que se registró el cliente en la aplicación.
     * Sirve para guardar el momento del registro; por defecto se usa la hora actual.
     */
    val fechaRegistro: Long = System.currentTimeMillis(),

    /**
     * fechaAlta
     * ---------
     * ✔ TIPO: propiedad (val) → Long? (opcional, timestamp)
     * Es la fecha en que el cliente se dio de alta en el servicio.
     * Sirve para guardar cuándo empezó el alta; puede estar vacía (null) si aún no se ha dado de alta.
     */
    val fechaAlta: Long? = null,

    /**
     * fechaBaja
     * ---------
     * ✔ TIPO: propiedad (val) → Long? (opcional, timestamp)
     * Es la fecha en que el cliente se dio de baja del servicio.
     * Sirve para guardar cuándo terminó el alta; puede estar vacía (null) si sigue activo.
     */
    val fechaBaja: Long? = null,

    /**
     * estado
     * ------
     * ✔ TIPO: propiedad (val) → EstadoCliente (enum)
     * Es el estado actual del cliente en el gestor.
     * Sirve para saber si el cliente está ACTIVO, MOROSO o de BAJA.
     */
    val estado: EstadoCliente,

    /**
     * tieneLlave
     * ----------
     * ✔ TIPO: propiedad (val) → Boolean
     * Es el indicador de si el cliente tiene la llave de las instalaciones.
     * Sirve para controlar quién dispone de llave de acceso en el gestor.
     */
    val tieneLlave: Boolean,

    /**
     * observaciones
     * -------------
     * ✔ TIPO: propiedad (val) → String? (opcional)
     * Es un campo libre para anotaciones o notas sobre el cliente.
     * Sirve para guardar observaciones adicionales; puede estar vacío (null) si no hay notas.
     */
    val observaciones: String? = null,

    /**
     * firebaseUid
     * -----------
     * ✔ TIPO: propiedad (val) → String? (opcional)
     * Es el identificador único de Firebase del cliente.
     * Sirve para relacionar el cliente con su cuenta de autenticación de Firebase; puede ser null.
     */
    val firebaseUid: String? = null
)

/**
 * toCliente
 * ---------
 * ✔ TIPO: función de extensión (extension function) → Cliente
 * Es la función que convierte una ClienteEntity en un Cliente del modelo de UI.
 * Sirve para transformar los datos de la base de datos en el modelo ligero que usa la interfaz,
 * juntando nombre y apellidos en un único campo "nombre".
 */
fun ClienteEntity.toCliente(): Cliente {
    return Cliente(
        idCliente = idCliente,
        nombre = "$nombre $apellidos",
        telefono = telefono,
        estado = estado
    )
}
