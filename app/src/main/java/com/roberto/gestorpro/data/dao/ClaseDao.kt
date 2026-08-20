package com.roberto.gestorpro.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.roberto.gestorpro.data.entity.ClaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarClase(clase: ClaseEntity): Long

    @Update
    suspend fun actualizarClase(clase: ClaseEntity)

    @Delete
    suspend fun eliminarClase(clase: ClaseEntity)

    @Query("SELECT * FROM clase ORDER BY nombre ASC")
    fun obtenerTodasLasClases(): Flow<List<ClaseEntity>>

    @Query("SELECT * FROM clase WHERE activa = 1 ORDER BY nombre ASC")
    fun obtenerClasesActivas(): Flow<List<ClaseEntity>>

    @Query("SELECT * FROM clase WHERE idClase = :idClase")
    suspend fun obtenerClasePorId(idClase: Int): ClaseEntity?

    @Query("DELETE FROM clase")
    suspend fun borrarTodasLasClases()
}
