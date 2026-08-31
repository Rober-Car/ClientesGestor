package com.roberto.gestorpro.cliente.data.firebase

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.roberto.gestorpro.cliente.model.Reserva
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ReservaRepository
 * -----------------
 * Gestiona las reservas del CLIENTE en Firestore. Crear y cancelar mantienen
 * la reserva y las plazas de la sesión dentro de la misma Transaction.
 */
@Singleton
class ReservaRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    companion object {
        private const val COLECCION_CLIENTES = "clientes"
        private const val COLECCION_SERVICIOS = "servicios"
        private const val COLECCION_SESIONES = "sesiones"
        private const val COLECCION_RESERVAS = "reservas"

        /** Identificador remoto determinista de cliente + sesión. */
        fun reservaId(clienteId: Int, sesionId: Int): String =
            "${clienteId}_${sesionId}"

        /**
         * aperturaAlcanzada
         * -----------------
         * Indica si la hora de apertura de reservas ya ha llegado. Comparación
         * en instante absoluto: fecha (epoch millis de la medianoche local del
         * día de la sesión) + offset de horaDesdeReserva frente al instante
         * actual. Si horaDesdeReserva es null, la apertura es el inicio del día.
         * Mismo criterio que el resto del proyecto (ZoneId.systemDefault ya
         * quedó aplicado en el valor de `fecha` generado por el Admin).
         */
        fun aperturaAlcanzada(fecha: Long, horaDesdeReserva: String?): Boolean {
            val apertura = horaDesdeReserva?.let { hora ->
                val partes = hora.split(":")
                val h = partes.getOrNull(0)?.toIntOrNull() ?: return true
                val m = partes.getOrNull(1)?.toIntOrNull() ?: return true
                fecha + (h * 3_600_000L + m * 60_000L)
            } ?: return true
            return System.currentTimeMillis() >= apertura
        }
    }

    /**
     * Crea una reserva validando cliente, negocio, servicio, autorización,
     * duplicado y plazas antes de escribir.
     */
    suspend fun crearReserva(
        clienteId: Int,
        sesionId: Int,
        negocioId: String
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

                // Todas las lecturas se completan antes de cualquier escritura.
                val cliente = transaction.get(clienteRef)
                val sesion = transaction.get(sesionRef)
                val reserva = transaction.get(reservaRef)
                val idServicio = sesion.getLong("idServicio")?.toInt()
                val servicio = idServicio?.let {
                    transaction.get(
                        db.collection(COLECCION_SERVICIOS).document(it.toString())
                    )
                }

                if (!cliente.exists()) throw ReservaException("El cliente no existe")
                if (cliente.getString("negocioId") != negocioId) {
                    throw ReservaException("El cliente no pertenece a tu negocio")
                }
                if (cliente.getString("firebaseUid") != uid) {
                    throw ReservaException("El cliente no corresponde a esta cuenta")
                }
                if (!sesion.exists()) throw ReservaException("La sesión no existe")
                if (sesion.getString("negocioId") != negocioId) {
                    throw ReservaException("La sesión no pertenece a tu negocio")
                }
                if (idServicio == null) throw ReservaException("La sesión no tiene servicio")
                if (servicio == null || !servicio.exists()) {
                    throw ReservaException("El servicio no existe")
                }
                if (servicio.getString("negocioId") != negocioId) {
                    throw ReservaException("El servicio no pertenece a tu negocio")
                }
                if (servicio.getBoolean("activo") != true) {
                    throw ReservaException("El servicio está inactivo")
                }

                val contratados = (cliente.get("serviciosContratados") as? List<*>)
                    ?.mapNotNull { (it as? Number)?.toInt() }
                    ?: emptyList()
                if (idServicio !in contratados) {
                    throw ReservaException("No tienes contratado este servicio")
                }
                if (reserva.exists()) {
                    throw ReservaException("Ya tienes una reserva para esta sesión")
                }

                val plazas = sesion.getLong("plazasDisponibles")?.toInt()
                    ?: throw ReservaException("La sesión no tiene plazas disponibles")
                if (plazas <= 0) throw ReservaException("No hay plazas disponibles")

                // La apertura de reservas: antes de horaDesdeReserva no se puede
                // reservar (null = abierta desde el inicio del día).
                val fechaSesion = sesion.getLong("fecha")
                    ?: throw ReservaException("La sesión no tiene fecha")
                val horaDesdeReserva = sesion.getString("horaDesdeReserva")
                if (!aperturaAlcanzada(fechaSesion, horaDesdeReserva)) {
                    throw ReservaException("Las reservas abren a las $horaDesdeReserva")
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
                    mapOf("plazasDisponibles" to plazas - 1)
                )
            }.esperar()
            ResultadoAutenticacion(true, "Reserva realizada")
        } catch (e: ReservaException) {
            ResultadoAutenticacion(false, e.message ?: "No se pudo realizar la reserva")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /** Cancela una reserva y devuelve su plaza dentro de una Transaction. */
    suspend fun cancelarReserva(
        clienteId: Int,
        sesionId: Int,
        negocioId: String
    ): ResultadoAutenticacion {
        auth.currentUser
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")

        return try {
            db.runTransaction { transaction ->
                val reservaRef = db.collection(COLECCION_RESERVAS)
                    .document(reservaId(clienteId, sesionId))
                val sesionRef = db.collection(COLECCION_SESIONES)
                    .document(sesionId.toString())

                // Las dos lecturas preceden a la eliminación y actualización.
                val reserva = transaction.get(reservaRef)
                val sesion = transaction.get(sesionRef)

                if (!reserva.exists()) throw ReservaException("No existe la reserva")
                if (reserva.getString("negocioId") != negocioId ||
                    reserva.getLong("clienteId")?.toInt() != clienteId
                ) {
                    throw ReservaException("La reserva no corresponde a esta cuenta")
                }
                if (!sesion.exists()) throw ReservaException("La sesión no existe")
                if (sesion.getString("negocioId") != negocioId) {
                    throw ReservaException("La sesión no pertenece a tu negocio")
                }

                val plazas = sesion.getLong("plazasDisponibles")?.toInt()
                    ?: throw ReservaException("La sesión no tiene plazas disponibles")
                val capacidad = sesion.getLong("capacidad")?.toInt()
                    ?: throw ReservaException("La sesión no tiene capacidad válida")
                if (plazas >= capacidad) {
                    throw ReservaException("La sesión ya está completa")
                }

                transaction.delete(reservaRef)
                transaction.update(
                    sesionRef,
                    mapOf("plazasDisponibles" to plazas + 1)
                )
            }.esperar()
            ResultadoAutenticacion(true, "Reserva cancelada")
        } catch (e: ReservaException) {
            ResultadoAutenticacion(false, e.message ?: "No se pudo cancelar la reserva")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /** Obtiene todas las reservas propias usando los filtros exigidos por Rules. */
    suspend fun obtenerReservasCliente(
        clienteId: Int,
        negocioId: String
    ): List<Reserva> {
        return db.collection(COLECCION_RESERVAS)
            .whereEqualTo("clienteId", clienteId)
            .whereEqualTo("negocioId", negocioId)
            .get()
            .esperar()
            .documents.mapNotNull { documento ->
                val datos = documento.data ?: return@mapNotNull null
                val sesionId = (datos["sesionId"] as? Number)?.toInt()
                    ?: return@mapNotNull null
                val cliente = (datos["clienteId"] as? Number)?.toInt()
                    ?: return@mapNotNull null
                Reserva(
                    idReserva = datos["idReserva"] as? String ?: documento.id,
                    negocioId = datos["negocioId"] as? String ?: negocioId,
                    sesionId = sesionId,
                    clienteId = cliente,
                    fechaReserva = fechaEnMilisegundos(datos["fechaReserva"]) ?: 0L
                )
            }
    }

    private fun fechaEnMilisegundos(valor: Any?): Long? = when (valor) {
        is Timestamp -> valor.toDate().time
        is Number -> valor.toLong()
        else -> null
    }

    private fun mensajeDe(e: Exception): String = when (e) {
        is FirebaseNetworkException ->
            "No hay conexión con el servidor. Comprueba tu conexión a Internet"
        is FirebaseFirestoreException -> when (e.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                "No tienes permisos para esta operación"
            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
                "No hay conexión con el servidor. Comprueba tu conexión a Internet"
            FirebaseFirestoreException.Code.ABORTED ->
                "La sesión ha cambiado. Comprueba las plazas e inténtalo de nuevo"
            else -> e.message ?: "Error inesperado. Inténtalo de nuevo"
        }
        else -> e.message ?: "Error inesperado. Inténtalo de nuevo"
    }
}

private class ReservaException(message: String) : Exception(message)
