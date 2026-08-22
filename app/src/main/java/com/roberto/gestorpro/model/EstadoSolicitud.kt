package com.roberto.gestorpro.model

/**
 * EstadoSolicitud.kt
 * ------------------
 * ✔ TIPO: archivo de código fuente Kotlin (enum de estados de solicitud)
 * Es el archivo que define los estados posibles de una solicitud de cliente.
 * Sirve para seguir el ciclo de vida de cada solicitud en la tabla "solicitud".
 */

/**
 * EstadoSolicitud
 * ---------------
 * ✔ TIPO: enum (enumerado de Kotlin)
 * Es el enumerado que contiene los estados posibles de una solicitud.
 * Sirve para saber si una solicitud está pendiente de revisar o ya fue resuelta;
 * Room lo guarda como texto a través de EstadoSolicitudConverter.
 */
enum class EstadoSolicitud {

    /**
     * PENDIENTE
     * ---------
     * ✔ TIPO: constante (valor del enum EstadoSolicitud)
     * Es el estado inicial de toda solicitud recién creada.
     * Sirve para que el administrador vea qué solicitudes aún no ha atendido.
     */
    PENDIENTE,

    /**
     * ACEPTADA
     * --------
     * ✔ TIPO: constante (valor del enum EstadoSolicitud)
     * Es el estado de las solicitudes aprobadas por el administrador.
     * Sirve para marcar la solicitud como resuelta favorablemente.
     */
    ACEPTADA,

    /**
     * RECHAZADA
     * ---------
     * ✔ TIPO: constante (valor del enum EstadoSolicitud)
     * Es el estado de las solicitudes denegadas por el administrador.
     * Sirve para marcar la solicitud como resuelta desfavorablemente.
     */
    RECHAZADA
}
