package com.roberto.gestorpro.data.repository

import com.roberto.gestorpro.data.dao.SolicitudDao
import com.roberto.gestorpro.data.entity.SolicitudEntity
import com.roberto.gestorpro.model.EstadoSolicitud
import kotlinx.coroutines.flow.Flow

/**
 * SolicitudRepository.kt
 * ----------------------
 * ✔ TIPO: archivo de código fuente Kotlin (repositorio de datos)
 * Es el archivo que define la capa de repositorio de solicitudes.
 * Sirve para separar la interfaz de usuario del acceso directo a la base de datos.
 */

/**
 * SolicitudRepository
 * -------------------
 * ✔ TIPO: clase (repositorio de datos)
 * Es la capa que separa la interfaz de usuario del acceso directo a la base de datos.
 * Sirve para centralizar las operaciones con solicitudes usando SolicitudDao internamente;
 * Hilt ya la construye a través de AppModule.provideSolicitudRepository.
 */
class SolicitudRepository(
    /**
     * solicitudDao
     * ------------
     * ✔ TIPO: parámetro (param) → SolicitudDao
     * Es el DAO de solicitudes que recibe el repositorio.
     * Sirve para que el repositorio acceda a la base de datos a través del DAO.
     */
    private val solicitudDao: SolicitudDao
) {

    /**
     * insertarSolicitudRepo
     * ---------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → Long
     * Es la operación que inserta una solicitud en la base de datos a través del DAO.
     * Sirve para guardar una nueva SolicitudEntity devolviendo el id generado por Room.
     */
    suspend fun insertarSolicitudRepo(solicitud: SolicitudEntity): Long {
        return solicitudDao.insertarSolicitudDao(solicitud)
    }

    /**
     * actualizarSolicitudRepo
     * -----------------------
     * ✔ TIPO: método (fun) suspend de Kotlin
     * Es la operación que actualiza los datos de una solicitud ya existente.
     * Sirve para cambiar su estado (por ejemplo de PENDIENTE a ACEPTADA).
     */
    suspend fun actualizarSolicitudRepo(solicitud: SolicitudEntity) {
        solicitudDao.actualizarSolicitudDao(solicitud)
    }

    /**
     * eliminarSolicitudRepo
     * ---------------------
     * ✔ TIPO: método (fun) suspend de Kotlin
     * Es la operación que elimina una solicitud de la base de datos.
     * Sirve para borrar una SolicitudEntity a través del DAO.
     */
    suspend fun eliminarSolicitudRepo(solicitud: SolicitudEntity) {
        solicitudDao.eliminarSolicitudDao(solicitud)
    }

    /**
     * obtenerSolicitudesRepo
     * ----------------------
     * ✔ TIPO: método (fun) de Kotlin → Flow<List<SolicitudEntity>>
     * Es la operación que recupera todas las solicitudes guardadas.
     * Sirve para obtener la lista completa de forma reactiva desde la capa de repositorio.
     */
    fun obtenerSolicitudesRepo(): Flow<List<SolicitudEntity>> {
        return solicitudDao.obtenerSolicitudesDao()
    }

    /**
     * obtenerSolicitudesPorClienteRepo
     * --------------------------------
     * ✔ TIPO: método (fun) de Kotlin → Flow<List<SolicitudEntity>>
     * Es la operación que recupera las solicitudes de un cliente concreto.
     * Sirve para que cada cliente consulte el estado de sus propias peticiones.
     */
    fun obtenerSolicitudesPorClienteRepo(idCliente: Int): Flow<List<SolicitudEntity>> {
        return solicitudDao.obtenerSolicitudesPorClienteDao(idCliente)
    }

    /**
     * obtenerSolicitudPorIdRepo
     * -------------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → SolicitudEntity?
     * Es la operación que recupera una solicitud buscándola por su ID.
     * Sirve para obtener una SolicitudEntity concreta (o null si no existe).
     */
    suspend fun obtenerSolicitudPorIdRepo(idSolicitud: Int): SolicitudEntity? {
        return solicitudDao.obtenerSolicitudPorIdDao(idSolicitud)
    }

    /**
     * contarSolicitudesPorEstadoRepo
     * ------------------------------
     * ✔ TIPO: método (fun) de Kotlin → Flow<Int>
     * Es la operación que cuenta cuántas solicitudes hay en un estado dado.
     * Sirve para mostrar contadores reactivos (por ejemplo las pendientes de atender).
     */
    fun contarSolicitudesPorEstadoRepo(estado: EstadoSolicitud): Flow<Int> {
        return solicitudDao.contarSolicitudesPorEstadoDao(estado)
    }
}
