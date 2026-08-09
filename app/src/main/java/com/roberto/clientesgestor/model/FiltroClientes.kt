package com.roberto.clientesgestor.model

/**
 * FiltroClientes.kt
 * -----------------
 * ✔ TIPO: archivo de código fuente Kotlin (modelo de datos)
 * Es el archivo que define los filtros disponibles para la lista de clientes.
 * Sirve para filtrar los clientes por su estado en la pantalla de gestión.
 */

/**
 * FiltroClientes
 * --------------
 * ✔ TIPO: enum class (enum de Kotlin)
 * Es el conjunto de valores fijos que representan los filtros de la lista de clientes.
 * Sirve para elegir entre ver todos los clientes o solo los de un estado concreto.
 */
enum class FiltroClientes {
    TODOS,
    ACTIVO,
    MOROSO,
    BAJA
}
