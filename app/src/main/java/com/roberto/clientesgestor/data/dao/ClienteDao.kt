package com.roberto.clientesgestor.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.roberto.clientesgestor.data.entity.ClienteEntity
import com.roberto.clientesgestor.model.EstadoCliente
import kotlinx.coroutines.flow.Flow

/**
 * ClienteDao.kt
 * -------------
 * ✔ TIPO: archivo de código fuente Kotlin (acceso a datos)
 * Es el archivo que define las operaciones de lectura y escritura sobre la tabla de clientes.
 * Sirve para que la aplicación inserte y consulte clientes en la base de datos Room.
 */

/**
 * @Dao
 * ----
 * ✔ TIPO: anotación (androidx.room.Dao)
 * Es la anotación que marca esta interfaz como objeto de acceso a datos (DAO).
 * Sirve para que Room genere automáticamente la implementación de los métodos de esta interfaz.
 */

/**
 * ClienteDao
 * ----------
 * ✔ TIPO: interfaz (DAO de Room)
 * Es la interfaz que expone las operaciones de la base de datos sobre los clientes.
 * Sirve para insertar y obtener clientes sin escribir consultas SQL a mano.
 */
@Dao
interface ClienteDao {

    @Insert
    suspend fun insertarClienteDao(cliente: ClienteEntity)

    @Update
    suspend fun actualizarClienteDao(cliente: ClienteEntity)

    @Delete
    suspend fun eliminarClienteDao(cliente: ClienteEntity)

    @Query("SELECT * FROM cliente")
    fun obtenerClientesDao(): Flow<List<ClienteEntity>>

    @Query("SELECT * FROM cliente WHERE dni = :dni")
    suspend fun obtenerClientePorDniDao(dni: String): ClienteEntity?

    @Query("SELECT * FROM cliente WHERE estado = :estado" )
    fun obtenerClientesPorEstadoDao(estado: EstadoCliente): Flow<List<ClienteEntity>>

}
