package com.roberto.gestorpro.data.repository

import com.roberto.gestorpro.data.dao.ReservaDao
import com.roberto.gestorpro.data.entity.ReservaEntity
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ReservaRepository @Inject constructor(
    private val reservaDao: ReservaDao
) {
    suspend fun insertarReserva(reserva: ReservaEntity) {
        reservaDao.insertarReserva(reserva)
    }

    fun obtenerReservasPorSesion(idSesion: Int): Flow<List<ReservaEntity>> {
        return reservaDao.obtenerReservasPorSesion(idSesion)
    }

    suspend fun obtenerReservasPorSesionSync(idSesion: Int): List<ReservaEntity> {
        return reservaDao.obtenerReservasPorSesionSync(idSesion)
    }

    suspend fun obtenerReserva(idSesion: Int, idCliente: Int): ReservaEntity? {
        return reservaDao.obtenerReserva(idSesion, idCliente)
    }

    suspend fun cancelarReserva(idSesion: Int, idCliente: Int) {
        reservaDao.cancelarReserva(idSesion, idCliente)
    }
}
