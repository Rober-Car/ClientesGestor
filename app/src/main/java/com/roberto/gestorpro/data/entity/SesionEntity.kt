package com.roberto.gestorpro.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * SesionEntity
 * ------------
 * Entidad Room de la tabla "sesion".
 * Representa una sesión concreta de un servicio: fecha, hora, duración, capacidad y plazas.
 * Una sesión pertenece directamente a un servicio (idServicio), sin entidad Clase intermedia.
 */
@Entity(tableName = "sesion")
data class SesionEntity(
    @PrimaryKey(autoGenerate = true)
    val idSesion: Int = 0,
    val negocioId: String,
    val idServicio: Int,
    val fecha: Long,
    val hora: String,
    val duracionMinutos: Int,
    val capacidad: Int,
    val plazasDisponibles: Int
)
