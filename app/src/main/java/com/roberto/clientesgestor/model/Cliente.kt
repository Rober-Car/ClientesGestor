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
     * nombre
     * ------
     * ✔ TIPO: propiedad (val) → String
     * Es el nombre del cliente.
     * Sirve para identificar a cada cliente en la aplicación.
     */
    val nombre: String,

    /**
     * telefono
     * --------
     * ✔ TIPO: propiedad (val) → String
     * Es el teléfono del cliente.
     * Sirve para guardar el número de contacto de cada cliente.
     */
    val telefono: String,

    /**
     * estado
     * ------
     * ✔ TIPO: propiedad (val) → Estado
     * Es el estado del cliente (ACTIVO, MOROSO o BAJA).
     * Sirve para saber la situación de cada cliente en el gestor.
     */
    val estado: Estado
)
