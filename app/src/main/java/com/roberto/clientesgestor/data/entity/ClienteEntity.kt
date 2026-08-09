package com.roberto.clientesgestor.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.roberto.clientesgestor.model.Cliente
import com.roberto.clientesgestor.model.EstadoCliente

@Entity(
    tableName = "cliente",
    indices = [Index(value = ["dni"], unique = true)]
)
data class ClienteEntity(
    @PrimaryKey(autoGenerate = true)
    val idCliente: Int = 0,
    val nombre: String,
    val apellidos: String,
    val dni: String,
    val password: String,
    val telefono: String,
    val email: String? = null,
    val foto: String,
    val fechaNacimiento: Long,
    val fechaRegistro: Long = System.currentTimeMillis(),
    val fechaAlta: Long? = null,
    val fechaBaja: Long? = null,
    val estado: EstadoCliente,
    val tieneLlave: Boolean,
    val observaciones: String? = null,
    val firebaseUid: String? = null
)

fun ClienteEntity.toCliente(): Cliente {
    return Cliente(
        idCliente = idCliente,
        nombre = "$nombre $apellidos",
        telefono = telefono,
        estado = estado
    )
}
