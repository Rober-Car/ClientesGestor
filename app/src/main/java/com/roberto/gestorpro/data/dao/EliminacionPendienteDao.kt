package com.roberto.gestorpro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.roberto.gestorpro.data.entity.EliminacionPendienteEntity
import kotlinx.coroutines.flow.Flow

/**
 * EliminacionPendienteDao
 * -----------------------
 * Acceso a la tabla `eliminacion_pendiente`: movimientos borrados en Room cuyo
 * `delete` remoto en Firestore aún no se ha confirmado.
 */
@Dao
interface EliminacionPendienteDao {

    /**
     * registrarPendiente
     * ------------------
     * Registra (o conserva) la eliminación pendiente. REPLACE: si la misma
     * eliminación ya estaba registrada, se mantiene sin duplicar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrarPendiente(pendiente: EliminacionPendienteEntity)

    /**
     * eliminarPendiente
     * -----------------
     * Elimina el registro pendiente una vez confirmado el borrado remoto.
     */
    @Query("DELETE FROM eliminacion_pendiente WHERE idMovimiento = :idMovimiento")
    suspend fun eliminarPendiente(idMovimiento: Int)

    /**
     * obtenerPendientesDeClienteSync
     * ------------------------------
     * Eliminaciones pendientes de un cliente concreto (para reconciliar su
     * economía o reintentar al abrir su perfil).
     */
    @Query("SELECT * FROM eliminacion_pendiente WHERE idCliente = :idCliente")
    suspend fun obtenerPendientesDeClienteSync(idCliente: Int): List<EliminacionPendienteEntity>

    /**
     * obtenerPendientesSync
     * ---------------------
     * Todas las eliminaciones pendientes (para reintentar al reiniciar la app).
     */
    @Query("SELECT * FROM eliminacion_pendiente")
    suspend fun obtenerPendientesSync(): List<EliminacionPendienteEntity>

    /**
     * obtenerPendientes
     * -----------------
     * Versión reactiva (por si una pantalla necesita observarla).
     */
    @Query("SELECT * FROM eliminacion_pendiente")
    fun obtenerPendientes(): Flow<List<EliminacionPendienteEntity>>
}
