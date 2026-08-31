package com.roberto.gestorpro.cliente.model

/**
 * Sesion
 * ------
 * Sesión de un servicio leída desde Firestore (sesiones/{idSesion}).
 * El formato sigue el contrato remoto del Admin:
 *   - fecha = epoch millis de la medianoche del día (zona local);
 *   - hora  = "HH:mm" (formato 24h).
 * Una sesión pertenece directamente a un servicio (idServicio), sin entidad
 * Clase intermedia.
 *
 * horaDesdeReserva (opcional): hora local del día de la sesión (formato
 * "HH:mm") a partir de la cual el CLIENTE puede reservar. null = reservas
 * abiertas desde el inicio del día.
 */
data class Sesion(
    val idSesion: Int,
    val negocioId: String,
    val idServicio: Int,
    val fecha: Long,
    val hora: String,
    val duracionMinutos: Int,
    val capacidad: Int,
    val plazasDisponibles: Int,
    val horaDesdeReserva: String? = null
)
