package com.roberto.clientesgestor.model

/**
 * EstadoMovimiento.kt
 * -------------------
 * ✔ TIPO: archivo de código fuente Kotlin (enum de estados)
 * Es el archivo que define los estados posibles de un movimiento (servicio).
 * Sirve para representar si un servicio prestado a un cliente está pendiente de pago o ya ha sido abonado.
 */

/**
 * EstadoMovimiento
 * ----------------
 * ✔ TIPO: enum (enumerado de Kotlin)
 * Es el enumerado que contiene los estados posibles de un movimiento.
 * Sirve para saber en qué situación está cada servicio contratado por un cliente.
 */
enum class EstadoMovimiento {

    /**
     * PENDIENTE
     * ---------
     * ✔ TIPO: constante (valor del enum EstadoMovimiento)
     * Es el estado de los movimientos cuyo servicio aún no ha sido abonado por el cliente.
     * Sirve para marcar los servicios pendientes de cobro en el gestor.
     */
    PENDIENTE,

    /**
     * PAGADO
     * ------
     * ✔ TIPO: constante (valor del enum EstadoMovimiento)
     * Es el estado de los movimientos cuyo servicio ya ha sido abonado por el cliente.
     * Sirve para marcar los servicios cobrados en el gestor.
     */
    PAGADO

}
