package com.roberto.clientesgestor.model

/**
 * Estado.kt
 * ---------
 * ✔ TIPO: archivo de código fuente Kotlin (modelo de datos)
 * Es el archivo del paquete `model` que define el estado de un cliente.
 * Sirve para representar de forma tipada y segura la situación de cada cliente en toda la aplicación.
 */

/**
 * Estado
 * ------
 * ✔ TIPO: enum class (enum de Kotlin)
 * Es el conjunto de valores fijos que representan el estado de un cliente.
 * Sirve para garantizar que un cliente solo puede estar en ACTIVO, MOROSO o BAJA.
 */
enum class Estado {

    /**
     * ACTIVO
     * ------
     * ✔ TIPO: constante de enum
     * Es el estado de un cliente dado de alta y al corriente.
     * Sirve para identificar a los clientes activos del gestor.
     */
    ACTIVO,

    /**
     * MOROSO
     * ------
     * ✔ TIPO: constante de enum
     * Es el estado de un cliente con pagos pendientes.
     * Sirve para identificar a los clientes con cuotas o pagos atrasados.
     */
    MOROSO,

    /**
     * BAJA
     * ----
     * ✔ TIPO: constante de enum
     * Es el estado de un cliente que ya no está dado de alta.
     * Sirve para identificar a los clientes dados de baja.
     */
    BAJA
}
