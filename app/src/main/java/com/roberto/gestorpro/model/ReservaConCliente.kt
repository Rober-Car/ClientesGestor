package com.roberto.gestorpro.model

/**
 * ReservaConCliente.kt
 * --------------------
 * ✔ TIPO: archivo de código fuente Kotlin (modelo de datos)
 * Es el archivo que define el dato conjunto de una reserva y los datos básicos del cliente.
 * Sirve como resultado de la consulta SQL de ReservaDao "obtenerReservasConCliente",
 * que une las tablas reserva y cliente para listar quién ha reservado cada sesión.
 */

/**
 * ReservaConCliente
 * -----------------
 * ✔ TIPO: data class (modelo de datos inmutable)
 * Es la clase que representa una reserva junto al nombre y teléfono del cliente que la hizo.
 * Sirve para pintar la lista de reservas de una sesión sin tener que volver a consultar
 * la tabla de clientes; los campos coinciden exactamente con las columnas del @Query.
 */
data class ReservaConCliente(
    val idReserva: Int,
    val idCliente: Int,
    val nombre: String,
    val apellidos: String,
    val telefono: String
)
