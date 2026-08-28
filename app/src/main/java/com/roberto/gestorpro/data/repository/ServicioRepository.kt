package com.roberto.gestorpro.data.repository

import com.roberto.gestorpro.data.dao.ServicioDao
import com.roberto.gestorpro.data.entity.ServicioEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * ServicioRepository
 * ------------------
 * Capa de repositorio de servicios. Los ViewModels acceden a los datos a
 * través de esta clase, que delega en ServicioDao (Room).
 */
class ServicioRepository @Inject constructor(
    private val servicioDao: ServicioDao
) {
    suspend fun insertarServicio(servicio: ServicioEntity): Long {
        return servicioDao.insertarServicio(servicio)
    }

    suspend fun actualizarServicio(servicio: ServicioEntity) {
        servicioDao.actualizarServicio(servicio)
    }

    suspend fun eliminarServicio(servicio: ServicioEntity) {
        servicioDao.eliminarServicio(servicio)
    }

    suspend fun obtenerServicioPorId(idServicio: Int): ServicioEntity? {
        return servicioDao.obtenerServicioPorId(idServicio)
    }

    fun obtenerServiciosActivos(): Flow<List<ServicioEntity>> {
        return servicioDao.obtenerServiciosActivos()
    }

    fun obtenerServiciosInactivos(): Flow<List<ServicioEntity>> {
        return servicioDao.obtenerServiciosInactivos()
    }

    fun obtenerTodosLosServicios(): Flow<List<ServicioEntity>> {
        return servicioDao.obtenerTodosLosServicios()
    }
}
