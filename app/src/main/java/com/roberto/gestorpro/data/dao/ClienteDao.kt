package com.roberto.gestorpro.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.roberto.gestorpro.data.entity.ClienteEntity
import com.roberto.gestorpro.model.EstadoCliente
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

    /**
     * insertarClienteDao
     * ------------------
     * ✔ TIPO: método (fun) de Room con anotación @Insert
     * Es la operación que inserta un cliente en la tabla de la base de datos.
     * Sirve para guardar un nuevo ClienteEntity cuando se le pasa como parámetro.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarClienteDao(cliente: ClienteEntity)

    /**
     * actualizarClienteDao
     * --------------------
     * ✔ TIPO: método (fun) de Room con anotación @Update
     * Es la operación que actualiza los datos de un cliente ya existente en la tabla.
     * Sirve para guardar los cambios de un ClienteEntity en la base de datos
     * buscando el cliente por su clave primaria.
     */
    @Update
    suspend fun actualizarClienteDao(cliente: ClienteEntity)

    /**
     * eliminarClienteDao
     * ------------------
     * ✔ TIPO: método (fun) de Room con anotación @Delete
     * Es la operación que elimina un cliente de la tabla de la base de datos.
     * Sirve para borrar un ClienteEntity de la base de datos
     * buscando el cliente por su clave primaria.
     */
    @Delete
    suspend fun eliminarClienteDao(cliente: ClienteEntity)

    /**
     * obtenerClientesDao
     * ------------------
     * ✔ TIPO: método (fun) de Room con anotación @Query → Flow<List<ClienteEntity>>
     * Es la operación que consulta todos los clientes guardados en la tabla.
     * Sirve para recuperar la lista completa de clientes con la consulta SQL
     * "SELECT * FROM cliente", devolviéndola de forma reactiva con Flow.
     */
    @Query("SELECT * FROM cliente")
    fun obtenerClientesDao(): Flow<List<ClienteEntity>>

    @Query("SELECT * FROM cliente")
    suspend fun obtenerTodosLosClientesSync(): List<ClienteEntity>

    /**
     * obtenerClientePorDniDao
     * -----------------------
     * ✔ TIPO: método (fun) de Room con anotación @Query → ClienteEntity?
     * Es la operación que consulta un cliente buscándolo por su DNI en la tabla.
     * Sirve para recuperar un ClienteEntity concreto (o null si no existe)
     * mediante la consulta SQL "SELECT * FROM cliente WHERE dni = :dni".
     */
    @Query("SELECT * FROM cliente WHERE dni = :dni")
    suspend fun obtenerClientePorDniDao(dni: String): ClienteEntity?

    /**
     * obtenerClientesPorEstadoDao
     * ---------------------------
     * ✔ TIPO: método (fun) de Room con anotación @Query → Flow<List<ClienteEntity>>
     * Es la operación que consulta los clientes filtrados por su estado.
     * Sirve para recuperar la lista de ClienteEntity con un EstadoCliente concreto
     * mediante la consulta SQL "SELECT * FROM cliente WHERE estado = :estado",
     * devolviéndola de forma reactiva con Flow.
     */
    @Query("SELECT * FROM cliente WHERE estado = :estado" )
    fun obtenerClientesPorEstadoDao(estado: EstadoCliente): Flow<List<ClienteEntity>>

    /**
     * obtenerClientePorIdDao
     * ----------------------
     * ✔ TIPO: método (fun) de Room con anotación @Query → ClienteEntity?
     * Es la operación que consulta un cliente buscándolo por su ID en la tabla.
     * Sirve para recuperar un ClienteEntity concreto (o null si no existe)
     * mediante la consulta SQL "SELECT * FROM cliente WHERE idCliente = :idCliente".
     */
    @Query("SELECT * FROM cliente WHERE idCliente = :idCliente")
    suspend fun obtenerClientePorIdDao(idCliente: Int): ClienteEntity?

    /**
     * obtenerIdsMorosos
     * -----------------
     * ✔ TIPO: método (fun) de Room con anotación @Query → Flow<List<Int>>
     * Es la operación que consulta los IDs de clientes que son morosos.
     * Un cliente es moroso si:
     *   - Está ACTIVO y tiene algún movimiento con fechaFin < ahora, o
     *   - Está de BAJA y tiene algún movimiento con estado PENDIENTE (impago).
     * Devuelve la lista de IDs de forma reactiva con Flow para que se actualice
     * automáticamente cuando cambien los movimientos o los estados de los clientes.
     */
    @Query("""
        SELECT DISTINCT m.idCliente FROM movimiento m
        INNER JOIN cliente c ON m.idCliente = c.idCliente
        WHERE (c.estado = 'ACTIVO' AND m.fechaFin < :ahora)
        OR (c.estado = 'BAJA' AND m.estado = 'PENDIENTE')
    """)
    fun obtenerIdsMorosos(ahora: Long): Flow<List<Int>>

    @Query("DELETE FROM cliente")
    suspend fun borrarTodosLosClientes()

}
