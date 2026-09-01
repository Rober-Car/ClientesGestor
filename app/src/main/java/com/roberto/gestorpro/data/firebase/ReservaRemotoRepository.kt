package com.roberto.gestorpro.data.firebase

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.roberto.gestorpro.model.EstadoCliente
import com.roberto.gestorpro.model.ReservaClienteDetalle
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
        private const val TAG = "ReservaRemotoRepository"

        /**
         * Máximo de reintentos de la cascada ante conflictos de transacción.
         * Cada reintento vuelve a consultar las reservas en FRESCO para capturar
         * reservas creadas entre consultas (máx. intentos = MAX_REINTENTOS + 1).
         */
        private const val MAX_REINTENTOS_CASCADA = 3

        /**
         * Máximo de reservas por sesión para poder eliminarlas de forma atómica:
         * 1 lectura de la sesión + N borrados de reserva + 1 borrado de sesión
         * no deben superar las 500 operaciones de la Transaction.
         */
        private const val MAX_RESERVAS_POR_SESION = 498

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
     * obtenerReservasDeSesionRemoto
     * -----------------------------
     * Obtiene las reservas de una sesión directamente desde Firestore
     * (reservas/{clienteId}_{sesionId}), que es la fuente de verdad de las
     * reservas creadas por appCliente. Consulta por sesionId + negocioId
     * (el índice compuesto correspondiente ya existe en producción) y
     * enriquece cada reserva con los datos del cliente (nombre, apellidos,
     * teléfono, foto y estado real) desde clientes/{idCliente}. Si la
     * operación falla, devuelve lista vacía para no romper la pantalla.
     */
    suspend fun obtenerReservasDeSesionRemoto(idSesion: Int): List<ReservaClienteDetalle> {
        val uid = auth.currentUser?.uid
            ?: return emptyList()
        val negocioId = uid

        return try {
            val reservas = db.collection(COLECCION_RESERVAS)
                .whereEqualTo("sesionId", idSesion)
                .whereEqualTo("negocioId", negocioId)
                .get()
                .esperar()
                .documents

            reservas.mapNotNull { documento ->
                val clienteId = documento.getLong("clienteId")?.toInt()
                    ?: return@mapNotNull null
                val cliente = db.collection(COLECCION_CLIENTES)
                    .document(clienteId.toString())
                    .get()
                    .esperar()
                if (!cliente.exists()) {
                    Log.w(TAG, "obtenerReservasDeSesionRemoto: cliente $clienteId no existe")
                    return@mapNotNull null
                }
                ReservaClienteDetalle(
                    idCliente = clienteId,
                    nombre = cliente.getString("nombre") ?: "",
                    apellidos = cliente.getString("apellidos") ?: "",
                    telefono = cliente.getString("telefono") ?: "",
                    foto = cliente.getString("foto") ?: "",
                    estado = estadoDe(cliente.getString("estado"))
                )
            }.sortedWith(compareBy({ it.nombre }, { it.apellidos }))
        } catch (e: Exception) {
            val codigo = (e as? FirebaseFirestoreException)?.code?.name ?: "NO_FIRESTORE_CODE"
            Log.e(
                TAG,
                "obtenerReservasDeSesionRemoto: idSesion=$idSesion falló. códigoFirebase=$codigo",
                e
            )
            emptyList()
        }
    }

    /**
     * estadoDe
     * --------
     * Convierte el valor remoto de clientes/{idCliente}.estado al enum
     * EstadoCliente. Ante un valor ausente o desconocido devuelve ACTIVO
     * (fail-closed, nunca lanza).
     */
    private fun estadoDe(valor: String?): EstadoCliente {
        if (valor == null) return EstadoCliente.ACTIVO
        return runCatching { EstadoCliente.valueOf(valor) }.getOrDefault(EstadoCliente.ACTIVO)
    }

    /**
     * eliminarSesionConReservasRemoto
     * -------------------------------
     * Elimina de forma ATÓMICA una sesión y TODAS sus reservas dentro de la
     * misma Transaction: lee primero la sesión (detección de conflictos con la
     * reserva concurrente de un CLIENTE) y borra sus reservas + la sesión.
     *
     * Ante un conflicto de transacción se REINTENTA con una consulta FRESCA de
     * reservas (máx. MAX_REINTENTOS_CASCADA reintentos) para capturar reservas
     * creadas entre consultas. La operación es idempotente: si la sesión ya no
     * existe y no conserva reservas, termina con éxito sin hacer nada. Si aún
     * conserva reservas, devuelve error para no ocultar datos huérfanos.
     *
     * Una sesión con más de MAX_RESERVAS_POR_SESION reservas no puede
     * eliminarse atómicamente sin superar el límite de 500 operaciones de la
     * Transaction; en ese caso se devuelve un error claro y NO se elimina nada.
     */
    suspend fun eliminarSesionConReservasRemoto(sesionId: Int): ResultadoAutenticacion {
        val negocioId = auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")
        val sesionRef = db.collection(COLECCION_SESIONES).document(sesionId.toString())

        var intentos = 0
        while (true) {
            intentos++

            val refsReservas = try {
                db.collection(COLECCION_RESERVAS)
                    .whereEqualTo("sesionId", sesionId)
                    .whereEqualTo("negocioId", negocioId)
                    .get()
                    .esperar()
                    .documents.map { it.reference }
            } catch (e: Exception) {
                return resultadoDeError(
                    "Query de reservas para la sesión $sesionId",
                    e
                )
            }

            if (refsReservas.size > MAX_RESERVAS_POR_SESION) {
                return ResultadoAutenticacion(
                    false,
                    "La sesión tiene demasiadas reservas (${refsReservas.size}) " +
                        "para eliminarse de forma atómica"
                )
            }

            try {
                db.runTransaction { transaction ->
                    val sesion = transaction.get(sesionRef)
                    if (!sesion.exists()) {
                        if (refsReservas.isNotEmpty()) {
                            throw SesionInexistenteConReservasException(refsReservas.size)
                        }
                        return@runTransaction
                    }
                    refsReservas.forEach { transaction.delete(it) }
                    transaction.delete(sesionRef)
                }.esperar()
                return ResultadoAutenticacion(true, "Sesión y reservas eliminadas")
            } catch (e: FirebaseFirestoreException) {
                if (e.code == FirebaseFirestoreException.Code.ABORTED && intentos <= MAX_REINTENTOS_CASCADA) {
                    continue
                }
                return resultadoDeError(
                    "Transaction de eliminación de la sesión $sesionId",
                    e
                )
            } catch (e: SesionInexistenteConReservasException) {
                return ResultadoAutenticacion(false, e.message ?: "La sesión no existe")
            } catch (e: Exception) {
                return resultadoDeError(
                    "Transaction de eliminación de la sesión $sesionId",
                    e
                )
            }
        }
    }

    /**
     * eliminarSesionesFuturasConReservasRemoto
     * -----------------------------------------
     * Elimina las sesiones futuras (fecha >= desde) de un servicio junto con
     * todas sus reservas, de una en una y de forma atómica. Se usa al dar de
     * baja un servicio o regenerar su programación. Las sesiones pasadas se
     * conservan.
     */
    suspend fun eliminarSesionesFuturasConReservasRemoto(
        idServicio: Int,
        desde: Long
    ): ResultadoAutenticacion {
        val negocioId = auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")
        Log.d("DIAG sesiones", "cascada eliminarSesionesFuturas: idServicio=$idServicio, desde=$desde, negocioId=$negocioId")
        return try {
            val idsFuturas = obtenerIdsSesionesFuturasDelServicio(
                idServicio,
                desde,
                negocioId
            )
            Log.d("DIAG sesiones", "cascada: ${idsFuturas.size} sesiones futuras encontradas")
            val resultado = eliminarSesionesConReservas(idsFuturas)
            Log.d("DIAG sesiones", "cascada resultado: exito=${resultado.exito}, mensaje=${resultado.mensaje}")
            resultado
        } catch (e: Exception) {
            val code = (e as? FirebaseFirestoreException)?.code
            Log.e("DIAG sesiones", "ERROR cascada: code=$code, message=${e.message}, full=${e.javaClass.simpleName}: $e")
            resultadoDeError(
                "Query de sesiones futuras del servicio $idServicio",
                e
            )
        }
    }

    /**
     * eliminarTodasLasSesionesConReservasRemoto
     * -----------------------------------------
     * Elimina TODAS las sesiones de un servicio junto con sus reservas, de una
     * en una y de forma atómica. Se usa al eliminar un servicio.
     */
    suspend fun eliminarTodasLasSesionesConReservasRemoto(
        idServicio: Int
    ): ResultadoAutenticacion {
        val negocioId = auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")
        return try {
            val ids = obtenerIdsSesionesDelServicio(idServicio, negocioId)
            eliminarSesionesConReservas(ids)
        } catch (e: Exception) {
            resultadoDeError(
                "Query de sesiones del servicio $idServicio",
                e
            )
        }
    }

    /**
     * eliminarSesionesConReservas
     * ---------------------------
     * Elimina las sesiones indicadas con sus reservas, de una en una. Cada
     * sesión se borra en su propia Transaction (nunca separada de sus reservas)
     * y dentro del límite de 500 operaciones. Si una sesión falla, se detiene
     * y se devuelve ese error para que el reintento exterior converja.
     */
    private suspend fun eliminarSesionesConReservas(sesionIds: List<Int>): ResultadoAutenticacion {
        if (sesionIds.isEmpty()) {
            return ResultadoAutenticacion(true, "No hay sesiones que eliminar")
        }
        for (id in sesionIds) {
            val resultado = eliminarSesionConReservasRemoto(id)
            if (!resultado.exito) return resultado
        }
        return ResultadoAutenticacion(true, "Sesiones y reservas eliminadas")
    }

    /**
     * obtenerIdsSesionesDelServicio
     * -----------------------------
     * Consulta las sesiones de un servicio y su negocio con filtros de igualdad
     * y devuelve sus ids.
     */
    private suspend fun obtenerIdsSesionesDelServicio(
        idServicio: Int,
        negocioId: String
    ): List<Int> {
        val snapshots = db.collection(COLECCION_SESIONES)
            .whereEqualTo("idServicio", idServicio)
            .whereEqualTo("negocioId", negocioId)
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
        desde: Long,
        negocioId: String
    ): List<Int> {
        Log.d("DIAG sesiones", "obtenerIdsSesionesFuturasDelServicio: idServicio=$idServicio, negocioId=$negocioId, desde=$desde")
        val snapshots = db.collection(COLECCION_SESIONES)
            .whereEqualTo("idServicio", idServicio)
            .whereEqualTo("negocioId", negocioId)
            .get()
            .esperar()
        Log.d("DIAG sesiones", "query resultado: ${snapshots.size()} documentos")
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

    /** Registra el código real de Firestore sin cambiar el mensaje de la UI. */
    private fun resultadoDeError(operacion: String, e: Exception): ResultadoAutenticacion {
        val codigo = (e as? FirebaseFirestoreException)?.code?.name ?: "NO_FIRESTORE_CODE"
        Log.e(TAG, "$operacion falló. códigoFirebase=$codigo", e)
        return ResultadoAutenticacion(false, mensajeDe(e))
    }
}

/**
 * ReservaException
 * ----------------
 * Excepción interna para errores de negocio de reservas (mensajes amigables).
 */
private class ReservaException(message: String) : Exception(message)

private class SesionInexistenteConReservasException(cantidadReservas: Int) :
    Exception("La sesión no existe y conserva $cantidadReservas reserva(s)")
