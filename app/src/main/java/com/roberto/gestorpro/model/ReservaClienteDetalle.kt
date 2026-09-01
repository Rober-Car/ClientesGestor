package com.roberto.gestorpro.model

/**
 * ReservaClienteDetalle
 * ---------------------
 * Datos del cliente reservado en una sesión, obtenidos desde Firestore
 * (clientes/{idCliente}) para la pantalla "Reservas de la sesión".
 * Permite pintar cada cliente con el mismo componente ClienteItem de la
 * lista de Clientes (foto, nombre y teléfono, con el color según el estado
 * real del cliente).
 */
data class ReservaClienteDetalle(
    val idCliente: Int,
    val nombre: String,
    val apellidos: String,
    val telefono: String,
    val foto: String,
    val estado: EstadoCliente
)
