package com.roberto.gestorpro.cliente.model

/**
 * EstadoSolicitud
 * ---------------
 * Estado de una solicitud de baja (mismos nombres que el contrato remoto).
 * Es independiente del estado del CLIENTE.
 */
enum class EstadoSolicitud {
    PENDIENTE,
    ACEPTADA,
    RECHAZADA
}
