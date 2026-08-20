package com.roberto.gestorpro.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clase")
data class ClaseEntity(
    @PrimaryKey(autoGenerate = true)
    val idClase: Int = 0,
    val nombre: String,
    val diasSemana: String,
    val horaInicio: String,
    val duracionMinutos: Int,
    val capacidadMaxima: Int,
    val reservaDesdeHorasAntes: Int,
    val fechaInicio: Long,
    val fechaFin: Long,
    val activa: Boolean = true
)
