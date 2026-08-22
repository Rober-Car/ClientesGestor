package com.roberto.gestorpro.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.roberto.gestorpro.data.entity.SolicitudEntity
import com.roberto.gestorpro.model.EstadoSolicitud
import kotlinx.coroutines.flow.Flow

/**
 * SolicitudDao.kt
 * ---------------
 * ✔ TIPO: archivo de código fuente Kotlin (acceso a datos)
 * Es el archivo que define las operaciones de lectura y escritura sobre la tabla de solicitudes.
 * Sirve para que la aplicación inserte y consulte solicitudes en la base de datos Room.
 */

/**
 * SolicitudDao
 * ------------
 * ✔ TIPO: interfaz (DAO de Room)
 * Es la interfaz que expone las operaciones de la base de datos sobre las solicitudes.
 * Sirve para insertar, actualizar, eliminar y consultar solicitudes sin escribir SQL suelto.
 */
@Dao
interface SolicitudDao {

    /**
     * insertarSolicitudDao
     * --------------------
     * ✔ TIPO: método (fun) de Room con anotación @Insert
     * Es la operación que inserta una solicitud en la tabla.
     * Sirve para guardar una nueva SolicitudEntity; devuelve el id generado por Room.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSolicitudDao(solicitud: SolicitudEntity): Long

    /**
     * actualizarSolicitudDao
     * ----------------------
     * ✔ TIPO: método (fun) de Room con anotación @Update
     * Es la operación que actualiza una solicitud ya existente buscándola por clave primaria.
     * Sirve para cambiar por ejemplo el estado de PENDIENTE a ACEPTADA o RECHAZADA.
     */
    @Update
    suspend fun actualizarSolicitudDao(solicitud: SolicitudEntity)

    /**
     * eliminarSolicitudDao
     * --------------------
     * ✔ TIPO: método (fun) de Room con anotación @Delete
     * Es la operación que elimina una solicitud de la tabla.
     * Sirve para borrar la fila buscando el objeto por su clave primaria.
     */
    @Delete
    suspend fun eliminarSolicitudDao(solicitud: SolicitudEntity)

    /**
     * obtenerSolicitudesDao
     * ---------------------
     * ✔ TIPO: método (fun) de Room con anotación @Query → Flow<List<SolicitudEntity>>
     * Es la operación que consulta todas las solicitudes ordenadas de más nueva a más antigua.
     * Sirve para listar las peticiones del negocio de forma reactiva con Flow.
     */
    @Query("SELECT * FROM solicitud ORDER BY fechaCreacion DESC")
    fun obtenerSolicitudesDao(): Flow<List<SolicitudEntity>>

    /**
     * obtenerSolicitudesPorClienteDao
     * -------------------------------
     * ✔ TIPO: método (fun) de Room con anotación @Query → Flow<List<SolicitudEntity>>
     * Es la operación que consulta las solicitudes de un cliente concreto.
     * Sirve para mostrar al cliente el estado de sus propias peticiones de forma reactiva.
     */
    @Query("SELECT * FROM solicitud WHERE idCliente = :idCliente ORDER BY fechaCreacion DESC")
    fun obtenerSolicitudesPorClienteDao(idCliente: Int): Flow<List<SolicitudEntity>>

    /**
     * obtenerSolicitudPorIdDao
     * ------------------------
     * ✔ TIPO: método (fun) de Room con anotación @Query → SolicitudEntity?
     * Es la operación que recupera una solicitud concreta por su identificador.
     * Sirve para cargar una solicitud puntual (o null si no existe).
     */
    @Query("SELECT * FROM solicitud WHERE idSolicitud = :idSolicitud")
    suspend fun obtenerSolicitudPorIdDao(idSolicitud: Int): SolicitudEntity?

    /**
     * obtenerSolicitudesPendientesDao
     * -------------------------------
     * ✔ TIPO: método (fun) de Room con anotación @Query → Flow<Int>
     * Es la operación que cuenta cuántas solicitudes están pendientes de atender.
     * Sirve para mostrar un contador reactivo de solicitudes sin resolver.
     */
    @Query("SELECT COUNT(*) FROM solicitud WHERE estado = :estado")
    fun contarSolicitudesPorEstadoDao(estado: EstadoSolicitud): Flow<Int>

    /**
     * borrarTodasLasSolicitudes
     * -------------------------
     * ✔ TIPO: método (fun) de Room con anotación @Query
     * Es la operación que vacía la tabla de solicitudes.
     * Sirve para la opción de borrar todos los datos de la app (DatosScreen).
     */
    @Query("DELETE FROM solicitud")
    suspend fun borrarTodasLasSolicitudes()
}
