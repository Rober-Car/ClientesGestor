package com.roberto.gestorpro.model

/**
 * FiltroClientes.kt
 * -----------------
 * ✔ TIPO: archivo de código fuente Kotlin (enum de filtros)
 * Es el archivo que define los filtros disponibles para la lista de clientes.
 * Sirve para representar los modos de búsqueda y filtrado de la pantalla de clientes.
 */

/**
 * FiltroClientes
 * --------------
 * ✔ TIPO: enum (enumerado de Kotlin)
 * Es el enumerado que contiene las opciones de filtrado de clientes.
 * Sirve para que el usuario pueda ver todos los clientes o solo los de un estado.
 */
enum class FiltroClientes {

    /**
     * TODOS
     * -----
     * ✔ TIPO: constante (valor del enum FiltroClientes)
     * Es el filtro que muestra todos los clientes sin distinguir su estado.
     * Sirve como opción por defecto de la lista de clientes.
     */
    TODOS,

    /**
     * ACTIVO
     * ------
     * ✔ TIPO: constante (valor del enum FiltroClientes)
     * Es el filtro que muestra solo los clientes con estado ACTIVO.
     * Sirve para consultar únicamente los clientes activos del gestor.
     */
    ACTIVO,

    /**
     * MOROSO
     * ------
     * ✔ TIPO: constante (valor del enum FiltroClientes)
     * Es el filtro que muestra solo los clientes con estado MOROSO.
     * Sirve para consultar únicamente los clientes morosos del gestor.
     */
    MOROSO,

    /**
     * BAJA
     * ----
     * ✔ TIPO: constante (valor del enum FiltroClientes)
     * Es el filtro que muestra solo los clientes con estado BAJA.
     * Sirve para consultar únicamente los clientes dados de baja del gestor.
     */
    BAJA
}
