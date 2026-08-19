package com.roberto.clientesgestor.model

/**
 * EstadoCliente.kt
 * ----------------
 * ✔ TIPO: archivo de código fuente Kotlin (enum de estados)
 * Es el archivo que define los estados posibles de un cliente.
 * Sirve para representar la situación de cada cliente dentro del gestor.
 */

/**
 * EstadoCliente
 * -------------
 * ✔ TIPO: enum (enumerado de Kotlin)
 * Es el enumerado que contiene los estados posibles de un cliente.
 * Sirve para saber en qué situación está cada cliente y poder filtrarlos.
 */
enum class EstadoCliente {

    /**
     * ACTIVO
     * ------
     * ✔ TIPO: constante (valor del enum EstadoCliente)
     * Es el estado de los clientes que están dados de alta y al día.
     * Sirve para marcar a los clientes activos del gestor.
     */
    ACTIVO,

    /**
     * MOROSO
     * ------
     * ✔ TIPO: constante (valor del enum EstadoCliente)
     * Es el estado de los clientes que tienen pagos pendientes o deudas.
     * Sirve para marcar a los clientes morosos del gestor.
     */
    MOROSO,

    /**
     * BAJA
     * ----
     * ✔ TIPO: constante (valor del enum EstadoCliente)
     * Es el estado de los clientes que se han dado de baja del servicio.
     * Sirve para marcar a los clientes que ya no forman parte del gestor.
     */
    BAJA,


    /**
     * REGISTRADO
     * ----------
     * ✔ TIPO: constante (valor del enum EstadoCliente)
     * Es el estado de los clientes que acaban de ser dados de alta en el sistema.
     * Sirve para marcar a los clientes que han sido registrados pero que aún no tienen actividad.
     */
    REGISTRADO
}
