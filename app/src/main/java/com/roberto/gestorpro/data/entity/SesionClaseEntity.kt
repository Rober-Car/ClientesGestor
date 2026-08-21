package com.roberto.gestorpro.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sesion_clase")
data class SesionClaseEntity(
    @PrimaryKey(autoGenerate = true)
    val idSesion: Int = 0,
    val negocioId: String,
    val idClase: Int,
    val servicio: String,
    val fecha: Long,
    val plazasDisponibles: Int
)
