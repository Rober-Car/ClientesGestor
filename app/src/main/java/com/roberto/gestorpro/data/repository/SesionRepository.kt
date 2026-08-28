package com.roberto.gestorpro.data.repository

import com.roberto.gestorpro.data.dao.SesionDao
import com.roberto.gestorpro.data.entity.SesionEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * SesionRepository
 * ----------------
 * Capa de repositorio de sesiones. Los ViewModels acceden a los datos a
 * través de esta clase, que delega en SesionDao (Room).
 */
class SesionRepository @Inject constructor(
    private val sesionDao: SesionDao
) {
    suspend fun insertarSesion(sesion: SesionEntity): Long {
        return sesionDao.insertarSesion(sesion)
    }

    suspend fun insertarSesiones(sesiones: List<SesionEntity>) {
        sesionDao.insertarSesiones(sesiones)
    }

    suspend fun actualizarSesion(sesion: SesionEntity) {
        sesionDao.actualizarSesion(sesion)
    }

    fun obtenerSesionesPorServicio(idServicio: Int): Flow<List<SesionEntity>> {
        return sesionDao.obtenerSesionesPorServicio(idServicio)
    }

    fun obtenerSesionesFuturasPorServicio(idServicio: Int, desde: Long): Flow<List<SesionEntity>> {
        return sesionDao.obtenerSesionesFuturasPorServicio(idServicio, desde)
    }

    suspend fun obtenerSesionPorId(idSesion: Int): SesionEntity? {
        return sesionDao.obtenerSesionPorId(idSesion)
    }

    suspend fun eliminarSesionesFuturasPorServicio(idServicio: Int, desde: Long) {
        sesionDao.eliminarSesionesFuturasPorServicio(idServicio, desde)
    }

    suspend fun eliminarSesionesPorServicio(idServicio: Int) {
        sesionDao.eliminarSesionesPorServicio(idServicio)
    }

    suspend fun eliminarSesion(idSesion: Int) {
        sesionDao.eliminarSesion(idSesion)
    }

    suspend fun reservarPlaza(idSesion: Int): Int {
        return sesionDao.reservarPlaza(idSesion)
    }

    suspend fun liberarPlaza(idSesion: Int) {
        sesionDao.liberarPlaza(idSesion)
    }
}
