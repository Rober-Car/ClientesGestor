package com.roberto.gestorpro.data.repository

import com.roberto.gestorpro.data.dao.ClaseDao
import com.roberto.gestorpro.data.entity.ClaseEntity
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ClaseRepository @Inject constructor(
    private val claseDao: ClaseDao
) {
    suspend fun insertarClase(clase: ClaseEntity): Long {
        return claseDao.insertarClase(clase)
    }

    suspend fun actualizarClase(clase: ClaseEntity) {
        claseDao.actualizarClase(clase)
    }

    suspend fun eliminarClase(clase: ClaseEntity) {
        claseDao.eliminarClase(clase)
    }

    fun obtenerTodasLasClases(): Flow<List<ClaseEntity>> {
        return claseDao.obtenerTodasLasClases()
    }

    fun obtenerClasesActivas(): Flow<List<ClaseEntity>> {
        return claseDao.obtenerClasesActivas()
    }

    suspend fun obtenerClasePorId(idClase: Int): ClaseEntity? {
        return claseDao.obtenerClasePorId(idClase)
    }
}
