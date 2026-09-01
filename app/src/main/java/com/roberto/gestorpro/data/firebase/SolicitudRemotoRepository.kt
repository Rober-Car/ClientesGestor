package com.roberto.gestorpro.data.firebase

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.roberto.gestorpro.model.EstadoSolicitud
import com.roberto.gestorpro.model.SolicitudBaja
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SolicitudRemotoRepository
 * -------------------------
 * Repositorio remoto de las solicitudes de baja del ADMIN.
 *
 * El CLIENTE crea su solicitud (PENDIENTE) en Firestore; el ADMIN las consulta
 * por negocio y las resuelve. Al ACEPTAR, la solicitud pasa a ACEPTADA y el
 * cliente a BAJA de forma ATÓMICA (misma Transaction Firestore) para evitar
 * estados inconsistentes. Al RECHAZAR, solo cambia la solicitud.
 */
@Singleton
class SolicitudRemotoRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    companion object {
        private const val COLECCION_SOLICITUDES = "solicitudes"
        private const val COLECCION_CLIENTES = "clientes"
        private const val TAG = "SolicitudRemotoRepository"

        const val ESTADO_PENDIENTE = "PENDIENTE"
        const val ESTADO_ACEPTADA = "ACEPTADA"
        const val ESTADO_RECHAZADA = "RECHAZADA"
        const val ESTADO_CLIENTE_BAJA = "BAJA"
    }

    /**
     * negocioIdActual
     * ---------------
     * El negocioId del ADMIN es su propio UID.
     */
    fun negocioIdActual(): String? = auth.currentUser?.uid?.takeIf { it.isNotBlank() }

    /**
     * obtenerSolicitudes
     * ------------------
     * Consulta las solicitudes del negocio filtrando por negocioId (la query
     * exige el filtro porque la regla de list no funciona como post-filtro).
     */
    suspend fun obtenerSolicitudes(negocioId: String): List<SolicitudBaja> {
        val snapshots = db.collection(COLECCION_SOLICITUDES)
            .whereEqualTo("negocioId", negocioId)
            .get()
            .esperar()

        return snapshots.documents
            .mapNotNull { documento ->
                val datos = documento.data ?: return@mapNotNull null
                val idSolicitud = datos["idSolicitud"] as? String ?: return@mapNotNull null
                val idCliente = enteroDe(datos["idCliente"]) ?: return@mapNotNull null
                val fechaSolicitud = fechaEnMilisegundos(datos["fechaSolicitud"])
                    ?: return@mapNotNull null
                SolicitudBaja(
                    idSolicitud = idSolicitud,
                    negocioId = datos["negocioId"] as? String ?: negocioId,
                    idCliente = idCliente,
                    firebaseUid = datos["firebaseUid"] as? String,
                    fechaSolicitud = fechaSolicitud,
                    estado = when (datos["estado"]) {
                        ESTADO_ACEPTADA -> EstadoSolicitud.ACEPTADA
                        ESTADO_RECHAZADA -> EstadoSolicitud.RECHAZADA
                        else -> EstadoSolicitud.PENDIENTE
                    },
                    fechaResolucion = fechaEnMilisegundos(datos["fechaResolucion"]),
                    resueltaPor = datos["resueltaPor"] as? String,
                    motivo = datos["motivo"] as? String
                )
            }
            .sortedByDescending { it.fechaSolicitud }
    }

    /**
     * aceptarBaja
     * -----------
     * Acepta la solicitud de forma ATÓMICA: en la misma Transaction marca la
     * solicitud ACEPTADA (fechaResolucion + resueltaPor) y pone al cliente en
     * BAJA (estado + fechaBaja = fechaBajaMillis). Devuelve false (sin cambios)
     * si la solicitud ya no estaba PENDIENTE.
     */
    suspend fun aceptarBaja(
        solicitud: SolicitudBaja,
        fechaBajaMillis: Long
    ): ResultadoAutenticacion {
        val uid = auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")

        return try {
            val aceptado = db.runTransaction { tx ->
                val solicitudRef =
                    db.collection(COLECCION_SOLICITUDES).document(solicitud.idSolicitud)
                val clienteRef =
                    db.collection(COLECCION_CLIENTES).document(solicitud.idCliente.toString())

                val snapSolicitud = tx.get(solicitudRef)
                if (!snapSolicitud.exists() ||
                    snapSolicitud.getString("estado") != ESTADO_PENDIENTE
                ) {
                    return@runTransaction false
                }

                tx.update(
                    solicitudRef,
                    mapOf(
                        "estado" to ESTADO_ACEPTADA,
                        "fechaResolucion" to Timestamp(Date(fechaBajaMillis)),
                        "resueltaPor" to uid
                    )
                )
                tx.update(
                    clienteRef,
                    mapOf(
                        "estado" to ESTADO_CLIENTE_BAJA,
                        "fechaBaja" to Timestamp(Date(fechaBajaMillis))
                    )
                )
                true
            }.esperar()

            if (aceptado) {
                Log.i(TAG, "Solicitud aceptada y cliente dado de baja: ${solicitud.idSolicitud}")
                ResultadoAutenticacion(true, "Baja aceptada")
            } else {
                ResultadoAutenticacion(false, "La solicitud ya fue gestionada")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error aceptando solicitud ${solicitud.idSolicitud}", e)
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * rechazarSolicitud
     * -----------------
     * Marca la solicitud RECHAZADA (fechaResolucion + resueltaPor). El cliente
     * permanece como estaba (ACTIVO). Las Rules exigen que la solicitud siga
     * PENDIENTE para poder modificarla.
     */
    suspend fun rechazarSolicitud(solicitud: SolicitudBaja): ResultadoAutenticacion {
        val uid = auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")
        val ahora = System.currentTimeMillis()

        return try {
            db.collection(COLECCION_SOLICITUDES)
                .document(solicitud.idSolicitud)
                .update(
                    mapOf(
                        "estado" to ESTADO_RECHAZADA,
                        "fechaResolucion" to Timestamp(Date(ahora)),
                        "resueltaPor" to uid
                    )
                )
                .esperar()
            Log.i(TAG, "Solicitud rechazada: ${solicitud.idSolicitud}")
            ResultadoAutenticacion(true, "Solicitud rechazada")
        } catch (e: Exception) {
            Log.e(TAG, "Error rechazando solicitud ${solicitud.idSolicitud}", e)
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    private fun fechaEnMilisegundos(valor: Any?): Long? = when (valor) {
        is Timestamp -> valor.toDate().time
        is Number -> valor.toLong()
        else -> null
    }

    private fun enteroDe(valor: Any?): Int? = when (valor) {
        is Int -> valor
        is Long -> valor.toInt()
        is Number -> valor.toInt()
        else -> null
    }

    private fun mensajeDe(e: Exception): String {
        return when {
            e.message?.contains("permission", ignoreCase = true) == true ->
                "No tienes permisos para gestionar esta solicitud"
            else -> e.message ?: "Error inesperado al gestionar la solicitud"
        }
    }
}
