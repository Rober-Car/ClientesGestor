package com.roberto.clientesgestor.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gasto")
data class GastoEntity(
    @PrimaryKey(autoGenerate = true)
    val idGasto: Int = 0,
    val concepto: String,
    val importe: Double,
    val fecha: Long,
    val observaciones: String? = null
)
