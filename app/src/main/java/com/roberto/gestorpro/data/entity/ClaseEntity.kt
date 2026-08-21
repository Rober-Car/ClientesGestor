package com.roberto.gestorpro.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clase")
data class ClaseEntity(
    @PrimaryKey(autoGenerate = true)
    val idClase: Int = 0,
    val negocioId: String,
    val nombre: String,
    val diasSemana: String,
    val horaInicio: String,
    val duracionMinutos: Int,
    val capacidadMaxima: Int,
    val horaAperturaReservas: String,
    val fechaInicio: Long,
    val fechaFin: Long,
    val activa: Boolean = true
)
