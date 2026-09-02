package com.roberto.gestorpro.model

/**
 * Cliente.kt
 * ----------
 * ✔ TIPO: archivo de código fuente Kotlin (modelo de datos)
 * Es el archivo que define el modelo de datos de un cliente para la interfaz.
 * Sirve para representar a un cliente con sus datos principales en la pantalla.
 */

/**
 * Cliente
 * -------
 * ✔ TIPO: data class (modelo de datos inmutable)
 * Es la clase que representa a un cliente del gestor.
 * Sirve para pasar los datos de un cliente entre el ViewModel y las pantallas,
 * de forma independiente de cómo se guarde en la base de datos (ClienteEntity).
 */
data class Cliente(
    val idCliente: Int,
    val nombre: String,
    val telefono: String,
    val email: String?,
    val dni: String,
    val foto: String,
    val fechaNacimiento: Long,
    val estado: EstadoCliente,
    val observaciones: String?,
    val serviciosContratados: List<Int> = emptyList(),
    val moroso: Boolean = false,
    val fechaEntradaMorosidad: Long? = null
)
