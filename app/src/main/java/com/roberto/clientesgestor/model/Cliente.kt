package com.roberto.clientesgestor.model

/**
 * Cliente.kt
 * ----------
 * ✔ TIPO: archivo de código fuente Kotlin (modelo de datos)
 * Es el archivo que define el modelo de datos de un cliente.
 * Sirve para representar la información de cada cliente en toda la aplicación.
 */


/**
 * Cliente
 * -------
 * ✔ TIPO: data class (clase de datos de Kotlin)
 * Es la clase que agrupa los datos de un cliente.
 * Sirve para representar a cada cliente con su nombre, teléfono y estado.
 */
data class Cliente(

    /**
     * idCliente
     * ---------
     * ✔ TIPO: propiedad (val) → Int
     * Es el identificador único del cliente.
     * Sirve para distinguir a cada cliente dentro de la aplicación.
     */
    val idCliente: Int,

    /**
     * nombre
     * ------
     * ✔ TIPO: propiedad (val) → String
     * Es el nombre del cliente.
     * Sirve para mostrar el nombre del cliente en la interfaz.
     */
    val nombre: String,

    /**
     * telefono
     * --------
     * ✔ TIPO: propiedad (val) → String
     * Es el número de teléfono de contacto del cliente.
     * Sirve para mostrar el teléfono del cliente en la interfaz.
     */
    val telefono: String,

    /**
     * estado
     * ------
     * ✔ TIPO: propiedad (val) → EstadoCliente (enum)
     * Es el estado actual del cliente.
     * Sirve para mostrar y filtrar clientes por su estado en la interfaz.
     */
    val estado: EstadoCliente
)
