package com.roberto.gestorpro.model

/**
 * SesionConClase.kt
 * -----------------
 * ✔ TIPO: archivo de código fuente Kotlin (modelo de datos)
 * Es el archivo que define el dato conjunto de una sesión programada y su clase padre.
 * Sirve como resultado de la consulta SQL de SesionClaseDao "obtenerSesionesActivasConClase",
 * que une las tablas sesion_clase y clase para mostrar la agenda de sesiones próximas.
 */

/**
 * SesionConClase
 * --------------
 * ✔ TIPO: data class (modelo de datos inmutable)
 * Es la clase que representa una sesión con los datos visibles de la clase a la que pertenece.
 * Sirve para pintar tarjetas de sesiones (nombre, hora, plazas) sin consultas extra;
 * los campos coinciden exactamente con las columnas seleccionadas en el @Query.
 */
data class SesionConClase(
    val idSesion: Int,
    val idClase: Int,
    val nombre: String,
    val horaInicio: String,
    val duracionMinutos: Int,
    val capacidadMaxima: Int,
    val plazasDisponibles: Int,
    val fecha: Long
)
