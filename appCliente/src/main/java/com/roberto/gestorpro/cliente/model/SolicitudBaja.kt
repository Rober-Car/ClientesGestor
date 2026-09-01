package com.roberto.gestorpro.cliente.model

/**
 * SolicitudBaja
 * -------------
 * Solicitud de baja del CLIENTE leída desde Firestore (solicitudes/{idSolicitud}).
 * Mientras la solicitud está PENDIENTE el cliente permanece ACTIVO; solo pasa a
 * BAJA cuando el ADMIN la acepta.
 */
data class SolicitudBaja(
    val idSolicitud: String,
    val negocioId: String,
    val idCliente: Int,
    val firebaseUid: String?,
    val fechaSolicitud: Long,
    val estado: EstadoSolicitud,
    val fechaResolucion: Long? = null,
    val resueltaPor: String? = null,
    val motivo: String? = null
)
