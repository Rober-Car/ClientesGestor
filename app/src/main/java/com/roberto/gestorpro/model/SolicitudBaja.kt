package com.roberto.gestorpro.model

/**
 * SolicitudBaja
 * -------------
 * Solicitud de baja del CLIENTE leída desde Firestore (solicitudes/{idSolicitud}).
 * La solicitud es independiente del estado del cliente: un cliente ACTIVO con
 * una solicitud PENDIENTE sigue ACTIVO hasta que el ADMIN la acepta (BAJA) o la
 * rechaza (permanece ACTIVO).
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
