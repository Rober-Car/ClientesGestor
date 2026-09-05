package com.roberto.gestorpro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.roberto.gestorpro.data.entity.ServicioDesactivacionPendienteEntity

/**
 * ServicioDesactivacionPendienteDao
 * ---------------------------------
 * Acceso a la tabla `servicio_desactivacion_pendiente`: servicios desactivados
 * localmente cuya cascada remota (eliminar sesiones futuras + reservas y dejar
 * el servicio inactivo) aún no se ha confirmado en Firestore.
 */
@Dao
interface ServicioDesactivacionPendienteDao {

    /**
     * registrarPendiente
     * ------------------
     * Registra (o conserva) la desactivación pendiente con su frontera ORIGINAL.
     * REPLACE: si el mismo servicio ya estaba pendiente, se mantiene un único
     * registro (no se duplican reintentos).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrarPendiente(pendiente: ServicioDesactivacionPendienteEntity)

    /**
     * eliminarPendiente
     * -----------------
     * Elimina el registro pendiente una vez que la cascada remota converge.
     */
    @Query("DELETE FROM servicio_desactivacion_pendiente WHERE idServicio = :idServicio")
    suspend fun eliminarPendiente(idServicio: Int)

    /**
     * obtenerPendiente
     * ----------------
     * Devuelve el pendiente de un servicio (si existe) con su frontera original.
     */
    @Query("SELECT * FROM servicio_desactivacion_pendiente WHERE idServicio = :idServicio")
    suspend fun obtenerPendiente(idServicio: Int): ServicioDesactivacionPendienteEntity?

    /**
     * obtenerPendientesSync
     * ---------------------
     * Todas las desactivaciones pendientes (para reintentar al arrancar la app
     * o al entrar a gestionar actividades).
     */
    @Query("SELECT * FROM servicio_desactivacion_pendiente")
    suspend fun obtenerPendientesSync(): List<ServicioDesactivacionPendienteEntity>
}
