package com.roberto.gestorpro.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reserva",
    indices = [
        Index(value = ["idSesion", "idCliente"], unique = true)
    ]
)
data class ReservaEntity(
    @PrimaryKey(autoGenerate = true)
    val idReserva: Int = 0,
    val negocioId: String,
    val idSesion: Int,
    val idCliente: Int,
    val fechaReserva: Long = System.currentTimeMillis()
)
