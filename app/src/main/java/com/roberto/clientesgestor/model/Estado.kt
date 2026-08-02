package com.roberto.clientesgestor.model

/**
 * Estado.kt
 * ---------
 * ✔ TIPO: archivo de código fuente Kotlin (modelo de datos)
 *
 * ¿Qué es?
 * El archivo del paquete `model` que define el estado de un cliente.
 *
 * ¿Qué hace?
 * - Declara el enum Estado con los valores posibles de un cliente.
 *
 * ¿Para qué sirve?
 * Para representar de forma tipada y segura la situación de cada cliente
 * (activo, moroso o de baja) en toda la aplicación.
 */

/**
 * Estado
 * ------
 * ✔ TIPO: enum class (enum de Kotlin)
 *
 * ¿Qué es?
 * Un conjunto de valores fijos que representan el estado de un cliente.
 *
 * ¿Qué hace?
 * - Define las tres constantes posibles: ACTIVO, MOROSO y BAJA.
 *
 * ¿Para qué sirve?
 * Para garantizar que un cliente solo puede estar en uno de estos estados,
 * evitando valores inválidos.
 */
enum class Estado {

    /**
     * ACTIVO
     * ------
     * ✔ TIPO: constante de enum
     *
     * ¿Qué es?
     * El estado de un cliente que está dado de alta y al corriente.
     *
     * ¿Para qué sirve?
     * Para identificar a los clientes activos del gestor.
     */
    ACTIVO,

    /**
     * MOROSO
     * ------
     * ✔ TIPO: constante de enum
     *
     * ¿Qué es?
     * El estado de un cliente que debe pagos pendientes.
     *
     * ¿Para qué sirve?
     * Para identificar a los clientes con cuotas o pagos atrasados.
     */
    MOROSO,

    /**
     * BAJA
     * ----
     * ✔ TIPO: constante de enum
     *
     * ¿Qué es?
     * El estado de un cliente que ya no está dado de alta.
     *
     * ¿Para qué sirve?
     * Para identificar a los clientes dados de baja en el gestor.
     */
    BAJA
}
