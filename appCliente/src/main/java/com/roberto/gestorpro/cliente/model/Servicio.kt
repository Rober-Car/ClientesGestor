package com.roberto.gestorpro.cliente.model

/**
 * Servicio
 * --------
 * Servicio del gimnasio leído desde Firestore (servicios/{idServicio}).
 * El CLIENTE solo puede leer servicios ACTIVOS de su propio negocio; un
 * servicio inactivo o eliminado no es legible y se omite de la pantalla.
 */
data class Servicio(
    val idServicio: Int,
    val negocioId: String,
    val nombre: String,
    val descripcion: String,
    val activo: Boolean
)
