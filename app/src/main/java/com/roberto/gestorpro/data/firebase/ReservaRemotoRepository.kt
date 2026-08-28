package com.roberto.gestorpro.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ReservaRemotoRepository
 * -----------------------
 * Repositorio de reservas en Firestore orientado al nuevo modelo
 * Cliente -> Reserva -> SesionEntity -> ServicioEntity.
 *
 * El documentId es DETERMINISTA: reservas/{clienteId}_{sesionId}, lo que
 * garantiza una única reserva activa por cliente + sesión.
 *
 * - La creación y la cancelación son ATÓMICAS (Firestore Transaction): la
 *   reserva y el ajuste de plazasDisponibles de la sesión van siempre juntos.
 * - Las cascadas de borrado de reservas se agrupan en WriteBatch.
 *
 * No toca movimientos en ningún caso.
 */
@Singleton
class ReservaRemotoRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    companion object {
        private const val COLECCION_RESERVAS = "reservas"
        private const val COLECCION_SESIONES = "sesiones"
        private const val COLECCION_SERVICIOS = "servicios"
        private const val COLECCION_CLIENTES = "clientes"

        /** Máximo de valores permitidos por cláusula `in` de Firestore. */
        private const val MAX_IN_QUERY = 10

        /**
         * documentId de una reserva: {clienteId}_{sesionId}.
         */
        fun reservaId(clienteId: Int, sesionId: Int): String =
            "${clienteId}_${sesionId}"
    }

    /**
     * crearReservaRemota
     * ------------------
     * Crea una reserva de forma ATÓMICA dentro de una Transaction:
     * comprueba cliente, sesión, servicio (activo y del negocio), servicio
     * contratado, plazas disponibles y que no exista ya la reserva; crea la
     * reserva y decrementa plazasDisponibles.
     */
    suspend fun crearReservaRemota(
        clienteId: Int,
        sesionId: Int
    ): ResultadoAutenticacion {
        val uid = auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")

        return try {
            db.runTransaction { transaction ->
                val clienteRef = db.collection(COLECCION_CLIENTES)
                    .document(clienteId.toString())
                val sesionRef = db.collection(COLECCION_SESIONES)
                    .document(sesionId.toString())
                val reservaRef = db.collection(COLECCION_RESERVAS)
                    .document(reservaId(clienteId, sesionId))

                val cliente = transaction.get(clienteRef)
                if (!cliente.exists()) throw ReservaException("El cliente no existe")
                val negocioId = cliente.getString("negocioId")
                    ?: throw ReservaException("El cliente no tiene negocio")
                val serviciosContratados = cliente.get("serviciosContratados") as? List<*>
                    ?: emptyList<Any>()

                val sesion = transaction.get(sesionRef)
                if (!sesion.exists()) throw ReservaException("La sesión no existe")
                if (sesion.getString("negocioId") != negocioId) {
                    throw ReservaException("La sesión no pertenece a tu negocio")
                }
                val idServicio = sesion.getLong("idServicio")?.toInt()
                    ?: throw ReservaException("La sesión no tiene servicio")
                val plazas = sesion.getLong("plazasDisponibles")?.toInt() ?: 0
                if (plazas <= 0) throw ReservaException("No hay plazas disponibles")

                val servicioRef = db.collection(COLECCION_SERVICIOS)
                    .document(idServicio.toString())
                val servicio = transaction.get(servicioRef)
                if (!servicio.exists()) throw ReservaException("El servicio no existe")
                if (servicio.getString("negocioId") != negocioId) {
                    throw ReservaException("El servicio no pertenece a tu negocio")
                }
                if (servicio.getBoolean("activo") != true) {
                    throw ReservaException("El servicio está inactivo")
                }
                if (idServicio !in serviciosContratados.filterIsInstance<Number>().map { it.toInt() }) {
                    throw ReservaException("No tienes contratado este servicio")
                }

                if (transaction.get(reservaRef).exists()) {
                    throw ReservaException("Ya tienes una reserva para esta sesión")
                }

                transaction.set(
                    reservaRef,
                    mapOf(
                        "idReserva" to reservaId(clienteId, sesionId),
                        "negocioId" to negocioId,
                        "sesionId" to sesionId,
                        "clienteId" to clienteId,
                        "fechaReserva" to Timestamp.now()
                    )
                )
                transaction.update(
                    sesionRef,
                    mapOf("plazasDisponibles" to (plazas - 1))
                )
            }.esperar()
            ResultadoAutenticacion(true, "Reserva realizada")
        } catch (e: ReservaException) {
            ResultadoAutenticacion(false, e.message ?: "No se pudo realizar la reserva")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * cancelarReservaRemota
     * ---------------------
     * Cancela una reserva de forma ATÓMICA dentro de una Transaction:
     * comprueba que la reserva existe y que la sesión sigue existiendo, elimina
     * la reserva e incrementa plazasDisponibles (sin superar la capacidad).
     */
    suspend fun cancelarReservaRemota(
        clienteId: Int,
        sesionId: Int
    ): ResultadoAutenticacion {
        val uid = auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")

        return try {
            db.runTransaction { transaction ->
                val reservaRef = db.collection(COLECCION_RESERVAS)
                    .document(reservaId(clienteId, sesionId))
                val sesionRef = db.collection(COLECCION_SESIONES)
                    .document(sesionId.toString())

                if (!transaction.get(reservaRef).exists()) {
                    throw ReservaException("No existe la reserva")
                }
                val sesion = transaction.get(sesionRef)
                if (!sesion.exists()) throw ReservaException("La sesión no existe")

                val plazas = sesion.getLong("plazasDisponibles")?.toInt() ?: 0
                val capacidad = sesion.getLong("capacidad")?.toInt() ?: plazas
                if (plazas >= capacidad) {
                    throw ReservaException("La sesión ya está completa")
                }

                transaction.delete(reservaRef)
                transaction.update(
                    sesionRef,
                    mapOf("plazasDisponibles" to (plazas + 1))
                )
            }.esperar()
            ResultadoAutenticacion(true, "Reserva cancelada")
        } catch (e: ReservaException) {
            ResultadoAutenticacion(false, e.message ?: "No se pudo cancelar la reserva")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * eliminarReservasDeSesionRemoto
     * ------------------------------
     * Elimina todas las reservas de una sesión concreta.
     */
    suspend fun eliminarReservasDeSesionRemoto(sesionId: Int): ResultadoAutenticacion {
        return try {
            val reservas = db.collection(COLECCION_RESERVAS)
                .whereEqualTo("sesionId", sesionId)
                .get()
                .esperar()
            borrarDocumentos(reservas.documents.map { it.reference })
            ResultadoAutenticacion(true, "Reservas eliminadas")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * eliminarReservasDeSesionesFuturasDelServicioRemoto
     * --------------------------------------------------
     * Elimina las reservas de las sesiones futuras (fecha >= desde) de un
     * servicio. Se usa al dar de baja un servicio o regenerar su programación.
     */
    suspend fun eliminarReservasDeSesionesFuturasDelServicioRemoto(
        idServicio: Int,
        desde: Long
    ): ResultadoAutenticacion {
        return try {
            val idsFuturas = obtenerIdsSesionesFuturasDelServicio(idServicio, desde)
            eliminarReservasDeSesionesRemoto(idsFuturas)
            ResultadoAutenticacion(true, "Reservas eliminadas")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * eliminarTodasLasReservasDelServicioRemoto
     * -----------------------------------------
     * Elimina todas las reservas de todas las sesiones de un servicio.
     * Se usa al eliminar un servicio.
     */
    suspend fun eliminarTodasLasReservasDelServicioRemoto(
        idServicio: Int
    ): ResultadoAutenticacion {
        return try {
            val ids = obtenerIdsSesionesDelServicio(idServicio)
            eliminarReservasDeSesionesRemoto(ids)
            ResultadoAutenticacion(true, "Reservas eliminadas")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * eliminarReservasDeSesionesRemoto
     * --------------------------------
     * Borra las reservas de las sesiones indicadas agrupando las consultas con
     * `in` (máx. 10 valores) y los borrados en WriteBatch.
     */
    private suspend fun eliminarReservasDeSesionesRemoto(sesionIds: List<Int>) {
        if (sesionIds.isEmpty()) return
        sesionIds.chunked(MAX_IN_QUERY).forEach { chunk ->
            val reservas = db.collection(COLECCION_RESERVAS)
                .whereIn("sesionId", chunk)
                .get()
                .esperar()
            borrarDocumentos(reservas.documents.map { it.reference })
        }
    }

    /**
     * borrarDocumentos
     * ----------------
     * Borra los documentos indicados en un único WriteBatch.
     */
    private suspend fun borrarDocumentos(referencias: List<com.google.firebase.firestore.DocumentReference>) {
        if (referencias.isEmpty()) return
        val batch = db.batch()
        referencias.forEach { batch.delete(it) }
        batch.commit().esperar()
    }

    /**
     * obtenerIdsSesionesDelServicio
     * -----------------------------
     * Consulta las sesiones de un servicio (igualdad sobre idServicio, sin
     * índices compuestos) y devuelve sus ids.
     */
    private suspend fun obtenerIdsSesionesDelServicio(idServicio: Int): List<Int> {
        val snapshots = db.collection(COLECCION_SESIONES)
            .whereEqualTo("idServicio", idServicio)
            .get()
            .esperar()
        return snapshots.documents.mapNotNull { it.getLong("idSesion")?.toInt() }
    }

    /**
     * obtenerIdsSesionesFuturasDelServicio
     * ------------------------------------
     * Sesiones de un servicio con fecha >= desde.
     */
    private suspend fun obtenerIdsSesionesFuturasDelServicio(
        idServicio: Int,
        desde: Long
    ): List<Int> {
        val snapshots = db.collection(COLECCION_SESIONES)
            .whereEqualTo("idServicio", idServicio)
            .get()
            .esperar()
        return snapshots.documents.mapNotNull { documento ->
            val id = documento.getLong("idSesion")?.toInt()
            val fecha = documento.getLong("fecha")
            if (id != null && fecha != null && fecha >= desde) id else null
        }
    }

    /**
     * mensajeDe
     * ---------
     * Traduce los errores típicos de Firestore a mensajes en español.
     */
    private fun mensajeDe(e: Exception): String {
        return when {
            e.message?.contains("permission", ignoreCase = true) == true ->
                "No tienes permisos para esta operación"
            else -> e.message ?: "Error inesperado. Inténtalo de nuevo"
        }
    }
}

/**
 * ReservaException
 * ----------------
 * Excepción interna para errores de negocio de reservas (mensajes amigables).
 */
private class ReservaException(message: String) : Exception(message)
