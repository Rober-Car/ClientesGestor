package com.roberto.gestorpro.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.roberto.gestorpro.data.entity.ServicioEntity
import kotlinx.coroutines.flow.Flow

/**
 * ServicioDao
 * -----------
 * DAO de Room de la tabla "servicio".
 * Expone las operaciones CRUD de los servicios del negocio,
 * con consultas específicas para separar activos e inactivos.
 */
@Dao
interface ServicioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarServicio(servicio: ServicioEntity): Long

    @Update
    suspend fun actualizarServicio(servicio: ServicioEntity)

    @Delete
    suspend fun eliminarServicio(servicio: ServicioEntity)

    @Query("SELECT * FROM servicio WHERE idServicio = :idServicio")
    suspend fun obtenerServicioPorId(idServicio: Int): ServicioEntity?

    @Query("SELECT * FROM servicio WHERE activo = 1 ORDER BY nombre ASC")
    fun obtenerServiciosActivos(): Flow<List<ServicioEntity>>

    @Query("SELECT * FROM servicio WHERE activo = 0 ORDER BY nombre ASC")
    fun obtenerServiciosInactivos(): Flow<List<ServicioEntity>>

    @Query("SELECT * FROM servicio ORDER BY nombre ASC")
    fun obtenerTodosLosServicios(): Flow<List<ServicioEntity>>
}
