package com.roberto.clientesgestor.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.roberto.clientesgestor.data.entity.GastoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {

    @Insert
    suspend fun insertarGasto(gasto: GastoEntity)

    @Update
    suspend fun actualizarGasto(gasto: GastoEntity)

    @Delete
    suspend fun eliminarGasto(gasto: GastoEntity)

    @Query("SELECT * FROM gasto ORDER BY fecha DESC")
    fun obtenerTodosLosGastos(): Flow<List<GastoEntity>>

    @Query("SELECT * FROM gasto")
    suspend fun obtenerTodosLosGastosSync(): List<GastoEntity>

    @Query("SELECT * FROM gasto WHERE idGasto = :idGasto")
    suspend fun obtenerGastoPorId(idGasto: Int): GastoEntity?
}
