package com.roberto.gestorpro.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.roberto.gestorpro.data.entity.MovimientoEntity
import com.roberto.gestorpro.model.EstadoMovimiento
import kotlinx.coroutines.flow.Flow

/**
 * MovimientoDao.kt
 * -----------------
 * ✔ TIPO: archivo de código fuente Kotlin (acceso a datos)
 * Es el archivo que define las operaciones de lectura y escritura sobre la tabla de movimientos.
 * Sirve para que la aplicación inserte, consulte, actualice y elimine servicios en la base de datos Room.
 */

/**
 * @Dao
 * ----
 * ✔ TIPO: anotación (androidx.room.Dao)
 * Es la anotación que marca esta interfaz como objeto de acceso a datos (DAO).
 * Sirve para que Room genere automáticamente la implementación de los métodos de esta interfaz.
 */

/**
 * MovimientoDao
 * -------------
 * ✔ TIPO: interfaz (DAO de Room)
 * Es la interfaz que expone las operaciones de la base de datos sobre los movimientos (servicios).
 * Sirve para insertar, actualizar, eliminar y consultar movimientos sin escribir consultas SQL a mano.
 */
@Dao
interface MovimientoDao {

    /**
     * insertarMovimiento
     * ------------------
     * ✔ TIPO: método (fun) de Room con anotación @Insert
     * Es la operación que inserta un movimiento en la tabla de la base de datos.
     * Sirve para guardar un nuevo MovimientoEntity cuando se le pasa como parámetro.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMovimiento(movimiento: MovimientoEntity)

    /**
     * actualizarMovimiento
     * --------------------
     * ✔ TIPO: método (fun) de Room con anotación @Update
     * Es la operación que actualiza los datos de un movimiento ya existente en la tabla.
     * Sirve para guardar los cambios de un MovimientoEntity en la base de datos
     * buscando el movimiento por su clave primaria.
     */
    @Update
    suspend fun actualizarMovimiento(movimiento: MovimientoEntity)

    /**
     * eliminarMovimiento
     * ------------------
     * ✔ TIPO: método (fun) de Room con anotación @Delete
     * Es la operación que elimina un movimiento de la tabla de la base de datos.
     * Sirve para borrar un MovimientoEntity de la base de datos
     * buscando el movimiento por su clave primaria.
     */
    @Delete
    suspend fun eliminarMovimiento(movimiento: MovimientoEntity)

    /**
     * obtenerMovimientosPorCliente
     * ----------------------------
     * ✔ TIPO: método (fun) de Room con anotación @Query → Flow<List<MovimientoEntity>>
     * Es la operación que consulta todos los movimientos asociados a un cliente concreto.
     * Sirve para recuperar la lista de servicios de un cliente ordenados por fecha de inicio descendente
     * mediante la consulta SQL "SELECT * FROM movimiento WHERE idCliente = :idCliente ORDER BY fechaInicio DESC",
     * devolviéndola de forma reactiva con Flow.
     */
    @Query("""
        SELECT * FROM movimiento
        WHERE idCliente = :idCliente
        ORDER BY fechaInicio DESC
    """)
    fun obtenerMovimientosPorCliente(idCliente: Int): Flow<List<MovimientoEntity>>

    /**
     * obtenerMovimientoPorId
     * ----------------------
     * ✔ TIPO: método (fun) de Room con anotación @Query → MovimientoEntity?
     * Es la operación que consulta un movimiento buscándolo por su ID en la tabla.
     * Sirve para recuperar un MovimientoEntity concreto (o null si no existe)
     * mediante la consulta SQL "SELECT * FROM movimiento WHERE idMovimiento = :idMovimiento".
     */
    @Query("""
        SELECT * FROM movimiento
        WHERE idMovimiento = :idMovimiento
    """)
    suspend fun obtenerMovimientoPorId(idMovimiento: Int): MovimientoEntity?

    /**
     * obtenerMovimientosPorEstado
     * ---------------------------
     * ✔ TIPO: método (fun) de Room con anotación @Query → Flow<List<MovimientoEntity>>
     * Es la operación que consulta los movimientos filtrados por su estado.
     * Sirve para recuperar la lista de MovimientoEntity con un EstadoMovimiento concreto
     * (PENDIENTE o PAGADO) mediante la consulta SQL
     * "SELECT * FROM movimiento WHERE estado = :estado ORDER BY fechaInicio DESC",
     * devolviéndola de forma reactiva con Flow.
     */
    @Query("""
        SELECT * FROM movimiento
        WHERE estado = :estado
        ORDER BY fechaInicio DESC
    """)
    fun obtenerMovimientosPorEstado(
        estado: EstadoMovimiento
    ): Flow<List<MovimientoEntity>>

    /**
     * obtenerTodosLosMovimientos
     * --------------------------
     * ✔ TIPO: método (fun) de Room con anotación @Query → Flow<List<MovimientoEntity>>
     * Es la operación que consulta todos los movimientos guardados en la tabla.
     * Sirve para recuperar la lista completa de servicios de todos los clientes
     * mediante la consulta SQL "SELECT * FROM movimiento ORDER BY fechaInicio DESC",
     * devolviéndola de forma reactiva con Flow.
     */
    @Query("""
        SELECT * FROM movimiento
        ORDER BY fechaInicio DESC
    """)
    fun obtenerTodosLosMovimientos(): Flow<List<MovimientoEntity>>

    @Query("SELECT * FROM movimiento")
    suspend fun obtenerTodosLosMovimientosSync(): List<MovimientoEntity>

    @Query("DELETE FROM movimiento")
    suspend fun borrarTodosLosMovimientos()

    @Query("SELECT COUNT(*) FROM movimiento")
    suspend fun contarTodos(): Int
}
