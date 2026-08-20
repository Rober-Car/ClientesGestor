package com.roberto.gestorpro.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reserva")
data class ReservaEntity(
    @PrimaryKey(autoGenerate = true)
    val idReserva: Int = 0,
    val idSesion: Int,
    val idCliente: Int,
    val fechaReserva: Long = System.currentTimeMillis()
)
