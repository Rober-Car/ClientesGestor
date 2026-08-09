package com.roberto.clientesgestor.model

/**
 * EstadoCliente.kt
 * ----------------
 * ✔ TIPO: archivo de código fuente Kotlin (modelo de datos)
 * Es el archivo del paquete `model` que define el estado de un cliente.
 * Sirve para representar de forma tipada y segura la situación de cada cliente en toda la aplicación.
 */

/**
 * EstadoCliente
 * -------------
 * ✔ TIPO: enum class (enum de Kotlin)
 * Es el conjunto de valores fijos que representan el estado de un cliente.
 * Sirve para garantizar que un cliente solo puede estar en ACTIVO, MOROSO o BAJA.
 */
enum class EstadoCliente {
    ACTIVO,
    MOROSO,
    BAJA
}
