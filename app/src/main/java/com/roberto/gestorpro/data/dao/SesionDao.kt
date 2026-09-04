package com.roberto.gestorpro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.roberto.gestorpro.data.entity.SesionEntity
import kotlinx.coroutines.flow.Flow

/**
 * SesionDao
 * ---------
 * DAO de Room de la tabla "sesion".
 * Expone las operaciones de inserción, consulta, eliminación y gestión de
 * plazas de las sesiones de un servicio. Una sesión pertenece directamente
 * a un servicio (idServicio), sin entidad Clase intermedia.
 */
@Dao
interface SesionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSesion(sesion: SesionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSesiones(sesiones: List<SesionEntity>)

    @Update
    suspend fun actualizarSesion(sesion: SesionEntity)

    @Query("SELECT * FROM sesion WHERE idServicio = :idServicio ORDER BY fecha ASC, hora ASC")
    fun obtenerSesionesPorServicio(idServicio: Int): Flow<List<SesionEntity>>

    @Query("SELECT * FROM sesion WHERE idServicio = :idServicio AND fecha >= :desde ORDER BY fecha ASC, hora ASC")
    fun obtenerSesionesFuturasPorServicio(idServicio: Int, desde: Long): Flow<List<SesionEntity>>

    @Query("SELECT * FROM sesion WHERE idServicio = :idServicio AND fecha >= :desde ORDER BY fecha ASC, hora ASC")
    suspend fun obtenerSesionesFuturasPorServicioSync(idServicio: Int, desde: Long): List<SesionEntity>

    @Query("SELECT * FROM sesion WHERE idSesion = :idSesion")
    suspend fun obtenerSesionPorId(idSesion: Int): SesionEntity?

    @Query("SELECT * FROM sesion WHERE fecha >= :inicio AND fecha < :fin ORDER BY idServicio ASC, fecha ASC, hora ASC")
    suspend fun obtenerSesionesEntre(inicio: Long, fin: Long): List<SesionEntity>

    @Query("DELETE FROM sesion WHERE idServicio = :idServicio AND fecha >= :desde")
    suspend fun eliminarSesionesFuturasPorServicio(idServicio: Int, desde: Long)

    @Query("DELETE FROM sesion WHERE idServicio = :idServicio")
    suspend fun eliminarSesionesPorServicio(idServicio: Int)

    @Query("DELETE FROM sesion WHERE idSesion = :idSesion")
    suspend fun eliminarSesion(idSesion: Int)

    @Query("UPDATE sesion SET plazasDisponibles = plazasDisponibles - 1 WHERE idSesion = :idSesion AND plazasDisponibles > 0")
    suspend fun reservarPlaza(idSesion: Int): Int

    @Query("UPDATE sesion SET plazasDisponibles = plazasDisponibles + 1 WHERE idSesion = :idSesion AND plazasDisponibles < capacidad")
    suspend fun liberarPlaza(idSesion: Int)

    @Query("SELECT COUNT(*) FROM sesion")
    suspend fun contarTodos(): Int
}
