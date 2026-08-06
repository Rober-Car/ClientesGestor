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


    val idCliente: Int,


    val nombre: String,


    val telefono: String,


    val estado: EstadoCliente
)
