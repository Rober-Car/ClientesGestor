package com.roberto.clientesgestor.model

/**
 * Cliente.kt
 * ----------
 * ✔ TIPO: archivo de código fuente Kotlin (modelo de datos)
 * Es el archivo que define el modelo de datos de un cliente.
 * Sirve para representar la información de cada cliente en toda la aplicación.
 */


data class Cliente(


    val nombre: String,


    val telefono: String,


    val estado: Estado
)
