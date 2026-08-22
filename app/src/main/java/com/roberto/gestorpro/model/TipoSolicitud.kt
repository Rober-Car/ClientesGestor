package com.roberto.gestorpro.model

/**
 * TipoSolicitud.kt
 * ----------------
 * ✔ TIPO: archivo de código fuente Kotlin (enum de tipos de solicitud)
 * Es el archivo que define los tipos de solicitud que puede hacer un cliente.
 * Sirve para clasificar cada solicitud guardada en la tabla "solicitud" de la base de datos.
 */

/**
 * TipoSolicitud
 * -------------
 * ✔ TIPO: enum (enumerado de Kotlin)
 * Es el enumerado que contiene los tipos posibles de solicitud.
 * Sirve para saber qué quiere el cliente cuando envía una solicitud al negocio;
 * Room lo guarda como texto a través de TipoSolicitudConverter.
 */
enum class TipoSolicitud {

    /**
     * CLASE
     * ------
     * ✔ TIPO: constante (valor del enum TipoSolicitud)
     * Es el tipo de solicitud para pedir plaza en una clase o sesión.
     * Sirve para que el administrador sepa que el cliente quiere apuntarse.
     */
    CLASE,

    /**
     * BAJA
     * ----
     * ✔ TIPO: constante (valor del enum TipoSolicitud)
     * Es el tipo de solicitud para pedir la baja de un servicio contratado.
     * Sirve para que el administrador sepa que el cliente quiere darse de baja.
     */
    BAJA
}
