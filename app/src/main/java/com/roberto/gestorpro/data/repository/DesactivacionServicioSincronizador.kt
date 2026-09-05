package com.roberto.gestorpro.data.repository

import android.util.Log
import com.roberto.gestorpro.data.dao.ServicioDesactivacionPendienteDao
import com.roberto.gestorpro.data.entity.ServicioDesactivacionPendienteEntity
import com.roberto.gestorpro.data.firebase.ReservaRemotoRepository
import com.roberto.gestorpro.data.firebase.ResultadoAutenticacion
import com.roberto.gestorpro.data.firebase.ServicioRemotoRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DesactivacionServicioSincronizador
 * ----------------------------------
 * Responsable de garantizar que la DESACTIVACIÓN (baja) de un servicio/actividad
 * converge también en Firestore de forma DURABLE e IDEMPOTENTE.
 *
 * Regla de negocio (sin cambios): al dar de baja un servicio se eliminan sus
 * sesiones FUTURAS y las reservas asociadas (las pasadas se conservan) y el
 * servicio queda `activo = false`. Este coordinador persiste la operación en la
 * tabla `servicio_desactivacion_pendiente` con la FRONTERA ORIGINAL de la baja
 * (`desde`), para que un reintento posterior no redefina qué sesiones debían
 * eliminarse.
 *
 * Es idempotente: si una sesión/reserva ya no existe, la cascada remota la da
 * por hecha; si ya se eliminó parte, continúa hasta converger. Cuando el estado
 * remoto coincide con la baja (sesiones futuras/reservas eliminadas y servicio
 * inactivo), se elimina el registro pendiente.
 */
@Singleton
class DesactivacionServicioSincronizador @Inject constructor(
    private val dao: ServicioDesactivacionPendienteDao,
    private val reservaRemotoRepository: ReservaRemotoRepository,
    private val servicioRemotoRepository: ServicioRemotoRepository
) {

    companion object {
        private const val TAG = "DesactivacionServicioSincronizador"
    }

    /**
     * Persiste (o conserva) el pendiente de desactivación con su frontera
     * ORIGINAL. No se recalcula la frontera en los reintentos.
     */
    suspend fun registrarPendiente(idServicio: Int, desde: Long) {
        dao.registrarPendiente(
            ServicioDesactivacionPendienteEntity(idServicio = idServicio, desde = desde)
        )
    }

    suspend fun eliminarPendiente(idServicio: Int) {
        dao.eliminarPendiente(idServicio)
    }

    suspend fun obtenerPendiente(idServicio: Int): ServicioDesactivacionPendienteEntity? {
        return dao.obtenerPendiente(idServicio)
    }

    /**
     * Converge en Firestore la desactivación de un servicio respecto a la
     * frontera `desde` (la de la baja ORIGINAL):
     *  1. elimina reservas + sesiones futuras (fecha >= desde);
     *  2. deja el servicio activo = false;
     *  3. si todo converge, elimina el pendiente durable.
     * Ante un fallo en cualquier paso, registra (o conserva) el pendiente y
     * devuelve el error. Es idempotente: repeticiones no generan efectos
     * incorrectos ni errores por sesiones/reservas ya eliminadas.
     */
    suspend fun convergerDesactivacion(
        idServicio: Int,
        desde: Long
    ): ResultadoAutenticacion {
        val cascada = reservaRemotoRepository
            .eliminarSesionesFuturasConReservasRemoto(idServicio, desde)
        if (!cascada.exito) {
            registrarPendiente(idServicio, desde)
            return cascada
        }

        val desactivar = servicioRemotoRepository.desactivarServicioRemoto(idServicio)
        if (!desactivar.exito) {
            registrarPendiente(idServicio, desde)
            return desactivar
        }

        eliminarPendiente(idServicio)
        return ResultadoAutenticacion(true, "Desactivación sincronizada en la nube")
    }

    /**
     * Reintenta TODAS las desactivaciones pendientes (arranque / entrada en
     * Actividades). Best-effort: un fallo deja el pendiente para el siguiente
     * reintento sin abortar el resto.
     */
    suspend fun reintentarPendientes() {
        val pendientes = dao.obtenerPendientesSync()
        if (pendientes.isEmpty()) return
        for (pendiente in pendientes) {
            try {
                convergerDesactivacion(pendiente.idServicio, pendiente.desde)
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Reintento de desactivación fallido: idServicio=${pendiente.idServicio}",
                    e
                )
            }
        }
    }
}
