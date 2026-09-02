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
     * ✔ TIPO: método (fun) de Room con anotación @Insert → Long
     * Es la operación que inserta un cliente en la tabla de la base de datos.
     * Sirve para guardar un nuevo ClienteEntity cuando se le pasa como parámetro
     * y devolver el rowId generado por Room para conocer el id del cliente creado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarClienteDao(cliente: ClienteEntity): Long

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
     * IDs de clientes con el indicador `moroso = true` persistido (única
     * fuente: la lógica MovimientoMorosidad recalcula y guarda el flag).
     */
    @Query("SELECT idCliente FROM cliente WHERE moroso = 1")
    fun obtenerIdsMorosos(): Flow<List<Int>>

    /**
     * actualizarMorosidadDao
     * ----------------------
     * Persiste el resultado del recálculo de morosidad (flag + fecha de
     * entrada) sin tocar el resto de la ficha del cliente.
     */
    @Query(
        "UPDATE cliente SET moroso = :moroso, " +
            "fechaEntradaMorosidad = :fechaEntradaMorosidad " +
            "WHERE idCliente = :idCliente"
    )
    suspend fun actualizarMorosidadDao(
        idCliente: Int,
        moroso: Boolean,
        fechaEntradaMorosidad: Long?
    )

    @Query("DELETE FROM cliente")
    suspend fun borrarTodosLosClientes()

}
