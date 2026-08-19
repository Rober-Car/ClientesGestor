package com.roberto.clientesgestor.data.repository

import com.roberto.clientesgestor.data.dao.GastoDao
import com.roberto.clientesgestor.data.entity.GastoEntity
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GastoRepository @Inject constructor(
    private val gastoDao: GastoDao
) {

    suspend fun insertarGasto(gasto: GastoEntity) {
        gastoDao.insertarGasto(gasto)
    }

    suspend fun actualizarGasto(gasto: GastoEntity) {
        gastoDao.actualizarGasto(gasto)
    }

    suspend fun eliminarGasto(gasto: GastoEntity) {
        gastoDao.eliminarGasto(gasto)
    }

    fun obtenerTodosLosGastos(): Flow<List<GastoEntity>> {
        return gastoDao.obtenerTodosLosGastos()
    }

    suspend fun obtenerGastoPorId(idGasto: Int): GastoEntity? {
        return gastoDao.obtenerGastoPorId(idGasto)
    }
}
