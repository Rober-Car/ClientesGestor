package com.roberto.gestorpro.data.repository

import com.roberto.gestorpro.data.dao.SesionClaseDao
import com.roberto.gestorpro.data.entity.SesionClaseEntity
import com.roberto.gestorpro.model.SesionConClase
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class SesionClaseRepository @Inject constructor(
    private val sesionClaseDao: SesionClaseDao
) {
    suspend fun insertarSesion(sesion: SesionClaseEntity): Long {
        return sesionClaseDao.insertarSesion(sesion)
    }

    suspend fun insertarSesiones(sesiones: List<SesionClaseEntity>) {
        sesionClaseDao.insertarSesiones(sesiones)
    }

    fun obtenerSesionesPorClase(idClase: Int): Flow<List<SesionClaseEntity>> {
        return sesionClaseDao.obtenerSesionesPorClase(idClase)
    }

    fun obtenerSesionesPorClaseDesde(idClase: Int, desde: Long): Flow<List<SesionClaseEntity>> {
        return sesionClaseDao.obtenerSesionesPorClaseDesde(idClase, desde)
    }

    suspend fun obtenerSesionPorId(idSesion: Int): SesionClaseEntity? {
        return sesionClaseDao.obtenerSesionPorId(idSesion)
    }

    suspend fun reservarPlaza(idSesion: Int): Int {
        return sesionClaseDao.reservarPlaza(idSesion)
    }

    suspend fun liberarPlaza(idSesion: Int) {
        sesionClaseDao.liberarPlaza(idSesion)
    }

    suspend fun eliminarSesionesPorClase(idClase: Int) {
        sesionClaseDao.eliminarSesionesPorClase(idClase)
    }

    fun obtenerSesionesActivasConClase(desde: Long, hasta: Long): Flow<List<SesionConClase>> {
        return sesionClaseDao.obtenerSesionesActivasConClase(desde, hasta)
    }
}
