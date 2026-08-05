package com.roberto.clientesgestor.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ClienteEntity.kt
 * ----------------
 * ✔ TIPO: archivo de código fuente Kotlin (entidad de base de datos)
 * Es el archivo que define la entidad Room para guardar clientes.
 * Sirve para representar la tabla de clientes en la base de datos local.
 */

/**
 * ClienteEntity
 * -------------
 * ✔ TIPO: data class (entidad de Room con anotación @Entity)
 * Es la clase que mapea un cliente a una tabla de la base de datos.
 * Sirve para que Room pueda guardar y recuperar clientes de forma persistente.
 */
@Entity
data class ClienteEntity(

    /**
     * idCliente
     * ---------
     * ✔ TIPO: propiedad (val) → Int
     * Es el identificador único del cliente en la base de datos.
     * Sirve como clave primaria (@PrimaryKey) de la tabla de clientes.
     */
    @PrimaryKey
    val idCliente: Int

)
