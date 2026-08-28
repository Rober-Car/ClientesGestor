package com.roberto.gestorpro.cliente.model

/**
 * Cliente
 * -------
 * ✔ TIPO: data class
 * Ficha del cliente leída desde Firestore (clientes/{idCliente}).
 * No incluye observaciones (dato exclusivo del ADMIN).
 */
data class Cliente(
    val idCliente: Int,
    val negocioId: String,
    val firebaseUid: String?,
    val nombre: String,
    val apellidos: String,
    val dni: String,
    val telefono: String,
    val email: String?,
    val foto: String,
    val fechaNacimiento: Long,
    val fechaRegistro: Long,
    val fechaAlta: Long?,
    val fechaBaja: Long?,
    val estado: EstadoCliente,
    val tieneLlave: Boolean,
    val serviciosContratados: List<String>,
    val fechaInicioActual: Long?,
    val fechaFinActual: Long?
)
