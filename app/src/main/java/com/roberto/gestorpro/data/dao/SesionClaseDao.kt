package com.roberto.gestorpro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.roberto.gestorpro.data.entity.SesionClaseEntity
import com.roberto.gestorpro.model.SesionConClase
import kotlinx.coroutines.flow.Flow

@Dao
interface SesionClaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSesion(sesion: SesionClaseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSesiones(sesiones: List<SesionClaseEntity>)

    @Query("SELECT * FROM sesion_clase WHERE idClase = :idClase ORDER BY fecha ASC")
    fun obtenerSesionesPorClase(idClase: Int): Flow<List<SesionClaseEntity>>

    @Query("SELECT * FROM sesion_clase WHERE idClase = :idClase AND fecha >= :desde ORDER BY fecha ASC")
    fun obtenerSesionesPorClaseDesde(idClase: Int, desde: Long): Flow<List<SesionClaseEntity>>

    @Query("SELECT * FROM sesion_clase WHERE idSesion = :idSesion")
    suspend fun obtenerSesionPorId(idSesion: Int): SesionClaseEntity?

    @Query("UPDATE sesion_clase SET plazasDisponibles = plazasDisponibles - 1 WHERE idSesion = :idSesion AND plazasDisponibles > 0")
    suspend fun reservarPlaza(idSesion: Int): Int

    @Query("UPDATE sesion_clase SET plazasDisponibles = plazasDisponibles + 1 WHERE idSesion = :idSesion")
    suspend fun liberarPlaza(idSesion: Int)

    @Query("DELETE FROM sesion_clase WHERE idClase = :idClase")
    suspend fun eliminarSesionesPorClase(idClase: Int)

    @Query("""
        SELECT s.idSesion, s.idClase, c.nombre, c.horaInicio, c.duracionMinutos, c.capacidadMaxima, s.plazasDisponibles, s.fecha
        FROM sesion_clase s INNER JOIN clase c ON s.idClase = c.idClase
        WHERE s.fecha >= :desde AND c.activa = 1
        ORDER BY s.fecha ASC
    """)
    fun obtenerSesionesActivasConClase(desde: Long): Flow<List<SesionConClase>>

    @Query("DELETE FROM sesion_clase")
    suspend fun borrarTodasLasSesiones()
}
