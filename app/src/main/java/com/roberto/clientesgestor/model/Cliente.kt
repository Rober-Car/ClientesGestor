package com.roberto.clientesgestor.model

/**
 * Cliente.kt
 * ----------
 * ✔ TIPO: archivo de código fuente Kotlin (modelo de datos)
 * Es el archivo que define el modelo de datos de un cliente para la interfaz.
 * Sirve para representar a un cliente con sus datos principales en la pantalla.
 */

/**
 * Cliente
 * -------
 * ✔ TIPO: data class (modelo de datos inmutable)
 * Es la clase que representa a un cliente del gestor.
 * Sirve para pasar los datos de un cliente entre el ViewModel y las pantallas,
 * de forma independiente de cómo se guarde en la base de datos (ClienteEntity).
 */
data class Cliente(

    /**
     * idCliente
     * ---------
     * ✔ TIPO: propiedad (val) → Int
     * Es el identificador único del cliente.
     * Sirve para saber de qué cliente se trata al operar con la base de datos.
     */
    val idCliente: Int,

    /**
     * nombre
     * ------
     * ✔ TIPO: propiedad (val) → String
     * Es el nombre completo del cliente (nombre y apellidos juntos).
     * Sirve para mostrarlo en las listas y en la interfaz de usuario.
     */
    val nombre: String,

    /**
     * telefono
     * --------
     * ✔ TIPO: propiedad (val) → String
     * Es el número de teléfono del cliente.
     * Sirve para mostrar el teléfono de contacto en la interfaz.
     */
    val telefono: String,

    /**
     * estado
     * ------
     * ✔ TIPO: propiedad (val) → EstadoCliente (enum)
     * Es el estado actual del cliente en el gestor.
     * Sirve para mostrar si el cliente está ACTIVO, MOROSO o de BAJA.
     */
    val estado: EstadoCliente
)
