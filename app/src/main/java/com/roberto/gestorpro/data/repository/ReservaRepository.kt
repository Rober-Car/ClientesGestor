package com.roberto.gestorpro.data.repository

import androidx.room.withTransaction
import com.roberto.gestorpro.data.dao.ReservaDao
import com.roberto.gestorpro.data.dao.ServicioDao
import com.roberto.gestorpro.data.dao.SesionDao
import com.roberto.gestorpro.data.database.ClientesDatabase
import com.roberto.gestorpro.data.entity.ReservaEntity
import com.roberto.gestorpro.data.entity.SesionEntity
import com.roberto.gestorpro.model.ReservaConCliente
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * ResultadoReserva
 * ----------------
 * Resultado de una operación de reserva (crear/cancelar) en Room.
 */
data class ResultadoReserva(
    val exito: Boolean,
    val mensaje: String
)

/**
 * ReservaRepository
 * -----------------
 * Repositorio de reservas orientado al nuevo modelo Cliente -> Reserva ->
 * SesionEntity -> ServicioEntity (sin depender de SesionClaseEntity).
 *
 * Las operaciones críticas (crear, cancelar, regenerar programación y las
 * cascadas de borrado) se ejecutan dentro de `RoomDatabase.withTransaction`
 * para que sean ATÓMICAS: nunca queda una reserva sin su plaza descontada ni
 * una plaza descontada sin reserva.
 */
class ReservaRepository @Inject constructor(
    private val reservaDao: ReservaDao,
    private val sesionDao: SesionDao,
    private val servicioDao: ServicioDao,
    private val database: ClientesDatabase
) {

    fun obtenerReservasPorSesion(idSesion: Int): Flow<List<ReservaEntity>> {
        return reservaDao.obtenerReservasPorSesion(idSesion)
    }

    suspend fun obtenerReservasPorSesionSync(idSesion: Int): List<ReservaEntity> {
        return reservaDao.obtenerReservasPorSesionSync(idSesion)
    }

    fun obtenerReservasPorCliente(idCliente: Int): Flow<List<ReservaEntity>> {
        return reservaDao.obtenerReservasPorCliente(idCliente)
    }

    suspend fun obtenerReserva(idSesion: Int, idCliente: Int): ReservaEntity? {
        return reservaDao.obtenerReserva(idSesion, idCliente)
    }

    suspend fun obtenerReservasConCliente(idSesion: Int): List<ReservaConCliente> {
        return reservaDao.obtenerReservasConCliente(idSesion)
    }

    /**
     * crearReserva
     * ------------
     * Crea una reserva de forma atómica:
     * 1. la sesión existe.
     * 2. plazasDisponibles > 0.
     * 3. el servicio de la sesión existe y está activo.
     * 4. el cliente no tiene ya una reserva para esa sesión.
     * 5. inserta la reserva.
     * 6. reduce plazasDisponibles en 1 (si no quedan plazas, se deshace todo).
     */
    suspend fun crearReserva(reserva: ReservaEntity): ResultadoReserva {
        return try {
            database.withTransaction {
                val sesion = sesionDao.obtenerSesionPorId(reserva.idSesion)
                    ?: return@withTransaction ResultadoReserva(false, "La sesión no existe")

                if (sesion.plazasDisponibles <= 0) {
                    return@withTransaction ResultadoReserva(false, "No hay plazas disponibles")
                }

                val servicio = servicioDao.obtenerServicioPorId(sesion.idServicio)
                    ?: return@withTransaction ResultadoReserva(false, "El servicio no existe")

                if (!servicio.activo) {
                    return@withTransaction ResultadoReserva(false, "El servicio está inactivo")
                }

                val duplicada = reservaDao.obtenerReserva(reserva.idSesion, reserva.idCliente)
                if (duplicada != null) {
                    return@withTransaction ResultadoReserva(
                        false,
                        "Ya tienes una reserva para esta sesión"
                    )
                }

                reservaDao.insertarReserva(reserva)
                val filas = sesionDao.reservarPlaza(reserva.idSesion)
                if (filas == 0) {
                    // Carrera: ya no quedan plazas -> se deshace la reserva insertada.
                    throw IllegalStateException("No quedan plazas disponibles")
                }
                ResultadoReserva(true, "Reserva realizada")
            }
        } catch (e: Exception) {
            ResultadoReserva(false, e.message ?: "No se pudo realizar la reserva")
        }
    }

    /**
     * cancelarReserva
     * ---------------
     * Cancela una reserva de forma atómica:
     * 1. la reserva existe (del cliente en esa sesión).
     * 2. elimina la reserva.
     * 3. aumenta plazasDisponibles en 1, sin superar la capacidad.
     */
    suspend fun cancelarReserva(idSesion: Int, idCliente: Int): ResultadoReserva {
        return try {
            database.withTransaction {
                val reserva = reservaDao.obtenerReserva(idSesion, idCliente)
                    ?: return@withTransaction ResultadoReserva(false, "No existe la reserva")

                reservaDao.cancelarReserva(idSesion, idCliente)
                sesionDao.liberarPlaza(idSesion)
                ResultadoReserva(true, "Reserva cancelada")
            }
        } catch (e: Exception) {
            ResultadoReserva(false, e.message ?: "No se pudo cancelar la reserva")
        }
    }

    /**
     * regenerarProgramacion
     * ---------------------
     * Regenera la programación de un servicio de forma atómica:
     * 1. elimina las reservas de las sesiones futuras (fecha >= desde).
     * 2. elimina esas sesiones futuras.
     * 3. crea las nuevas sesiones.
     * Las sesiones pasadas y sus reservas se conservan.
     */
    suspend fun regenerarProgramacion(
        idServicio: Int,
        desde: Long,
        nuevas: List<SesionEntity>
    ) {
        database.withTransaction {
            reservaDao.eliminarReservasDeSesionesFuturasDelServicio(idServicio, desde)
            sesionDao.eliminarSesionesFuturasPorServicio(idServicio, desde)
            sesionDao.insertarSesiones(nuevas)
        }
    }

    /**
     * eliminarReservasYSesionesFuturasDelServicio
     * --------------------------------------------
     * Al dar de baja un servicio: elimina las reservas de sus sesiones futuras
     * y después esas sesiones futuras (atómico). Las pasadas se conservan.
     */
    suspend fun eliminarReservasYSesionesFuturasDelServicio(
        idServicio: Int,
        desde: Long
    ) {
        database.withTransaction {
            reservaDao.eliminarReservasDeSesionesFuturasDelServicio(idServicio, desde)
            sesionDao.eliminarSesionesFuturasPorServicio(idServicio, desde)
        }
    }

    /**
     * eliminarReservasYSesionesDelServicio
     * ------------------------------------
     * Al eliminar un servicio: elimina las reservas de todas sus sesiones y
     * después todas esas sesiones (atómico). Los movimientos no se tocan.
     */
    suspend fun eliminarReservasYSesionesDelServicio(idServicio: Int) {
        database.withTransaction {
            reservaDao.eliminarReservasDeSesionesDelServicio(idServicio)
            sesionDao.eliminarSesionesPorServicio(idServicio)
        }
    }

    /**
     * eliminarSesionConReservas
     * -------------------------
     * Al eliminar una sesión individual: elimina primero sus reservas y después
     * la sesión (atómico). No queda ninguna reserva apuntando a la sesión.
     */
    suspend fun eliminarSesionConReservas(idSesion: Int) {
        database.withTransaction {
            reservaDao.eliminarReservasPorSesion(idSesion)
            sesionDao.eliminarSesion(idSesion)
        }
    }
}
