package com.roberto.gestorpro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.roberto.gestorpro.data.entity.ReservaEntity
import com.roberto.gestorpro.model.ReservaConCliente
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarReserva(reserva: ReservaEntity)

    @Query("SELECT * FROM reserva WHERE idSesion = :idSesion")
    fun obtenerReservasPorSesion(idSesion: Int): Flow<List<ReservaEntity>>

    @Query("SELECT * FROM reserva WHERE idSesion = :idSesion")
    suspend fun obtenerReservasPorSesionSync(idSesion: Int): List<ReservaEntity>

    @Query("SELECT * FROM reserva WHERE idCliente = :idCliente ORDER BY fechaReserva DESC")
    fun obtenerReservasPorCliente(idCliente: Int): Flow<List<ReservaEntity>>

    @Query("SELECT * FROM reserva WHERE idCliente = :idCliente")
    suspend fun obtenerReservasPorClienteSync(idCliente: Int): List<ReservaEntity>

    @Query("SELECT * FROM reserva WHERE idSesion = :idSesion AND idCliente = :idCliente")
    suspend fun obtenerReserva(idSesion: Int, idCliente: Int): ReservaEntity?

    @Query("DELETE FROM reserva WHERE idSesion = :idSesion AND idCliente = :idCliente")
    suspend fun cancelarReserva(idSesion: Int, idCliente: Int)

    @Query("DELETE FROM reserva WHERE idSesion = :idSesion")
    suspend fun eliminarReservasPorSesion(idSesion: Int)

    @Query("""
        SELECT r.idReserva, r.idCliente, c.nombre, c.apellidos, c.telefono
        FROM reserva r INNER JOIN cliente c ON r.idCliente = c.idCliente
        WHERE r.idSesion = :idSesion
        ORDER BY c.nombre ASC
    """)
    suspend fun obtenerReservasConCliente(idSesion: Int): List<ReservaConCliente>

    /**
     * eliminarReservasDeSesionesFuturasDelServicio
     * --------------------------------------------
     * Borra las reservas de las sesiones futuras (fecha >= desde) de un servicio.
     * Sirve al ADMIN para dar de baja un servicio o regenerar su programación
     * sin dejar reservas huérfanas de sesiones que se van a eliminar.
     */
    @Query("""
        DELETE FROM reserva
        WHERE idSesion IN (
            SELECT idSesion FROM sesion
            WHERE idServicio = :idServicio AND fecha >= :desde
        )
    """)
    suspend fun eliminarReservasDeSesionesFuturasDelServicio(idServicio: Int, desde: Long)

    /**
     * eliminarReservasDeSesionesDelServicio
     * -------------------------------------
     * Borra todas las reservas de todas las sesiones de un servicio.
     * Sirve al ADMIN para eliminar un servicio sin dejar reservas huérfanas.
     */
    @Query("""
        DELETE FROM reserva
        WHERE idSesion IN (
            SELECT idSesion FROM sesion
            WHERE idServicio = :idServicio
        )
    """)
    suspend fun eliminarReservasDeSesionesDelServicio(idServicio: Int)

    @Query("DELETE FROM reserva")
    suspend fun borrarTodasLasReservas()
}
