package com.roberto.gestorpro.data.firebase

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.roberto.gestorpro.model.ConfiguracionNotificaciones
import com.roberto.gestorpro.model.DestinatarioResuelto
import com.roberto.gestorpro.model.NotificacionAdmin
import com.roberto.gestorpro.model.ResolucionDestinatarios
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NotificacionRemotoRepository
 * ----------------------------
 * Repositorio remoto de las notificaciones del ADMIN.
 *
 * Fase D: crea el registro global `notificaciones/{id}`, resuelve los
 * destinatarios desde Firestore (nunca confía en Room) y crea los buzones
 * `notificaciones_por_destinatario/{clienteId}_{notificacionId}` para los
 * clientes vinculados (firebaseUid válido). También gestiona la configuración
 * de notificaciones preconfiguradas `configuracion_notificaciones/{negocioId}`.
 *
 * El envío FCM real (Cloud Functions) es Fase E: aquí solo se preparan los
 * documentos que el backend consumirá posteriormente.
 */
@Singleton
class NotificacionRemotoRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    companion object {
        private const val COLECCION_NOTIFICACIONES = "notificaciones"
        private const val COLECCION_BUZON = "notificaciones_por_destinatario"
        private const val COLECCION_CONFIG = "configuracion_notificaciones"
        private const val COLECCION_CLIENTES = "clientes"
        private const val MAX_ESCRITURAS_POR_BATCH = 500
        private const val TAG = "NotificacionRemotoRepository"

        const val TIPO_MANUAL = "MANUAL"
        const val TIPO_PROGRAMADA = "PROGRAMADA"
        const val ORIGEN_MANUAL = "MANUAL"
        const val ESTADO_PENDIENTE = "PENDIENTE"
        const val ESTADO_ENVIADA = "ENVIADA"
        const val ESTADO_PROGRAMADA = "PROGRAMADA"
        const val ESTADO_CANCELADA = "CANCELADA"
        const val ESTADO_ERROR = "ERROR"
    }

    /**
     * negocioIdActual
     * ---------------
     * Devuelve el negocioId del ADMIN autenticado (su propio UID) o null si
     * no hay sesión válida.
     */
    fun negocioIdActual(): String? = auth.currentUser?.uid?.takeIf { it.isNotBlank() }

    /**
     * obtenerNotificaciones
     * ---------------------
     * Consulta `notificaciones` filtrando por negocioId (la query exige el
     * filtro porque la regla de list no funciona como post-filtro) y ordena
     * en memoria de más reciente a más antigua usando fechaEnvio y, si no
     * existe (programadas no enviadas), fechaProgramada o fechaCreacion.
     */
    suspend fun obtenerNotificaciones(negocioId: String): List<NotificacionAdmin> {
        val snapshots = db.collection(COLECCION_NOTIFICACIONES)
            .whereEqualTo("negocioId", negocioId)
            .get()
            .esperar()

        return snapshots.documents
            .mapNotNull { documento ->
                val datos = documento.data ?: return@mapNotNull null
                val titulo = datos["titulo"] as? String ?: return@mapNotNull null
                val mensaje = datos["mensaje"] as? String ?: return@mapNotNull null
                val fechaCreacion = fechaEnMilisegundos(datos["fechaCreacion"])
                    ?: return@mapNotNull null
                NotificacionAdmin(
                    id = documento.id,
                    titulo = titulo,
                    mensaje = mensaje,
                    tipo = datos["tipo"] as? String ?: TIPO_MANUAL,
                    origen = datos["origen"] as? String ?: ORIGEN_MANUAL,
                    modoDestino = datos["modoDestino"] as? String ?: "TODOS",
                    idsClientes = (datos["idsClientes"] as? List<*>)
                        ?.mapNotNull { enteroDe(it) } ?: emptyList(),
                    clienteId = enteroDe(datos["clienteId"]),
                    fechaCreacion = fechaCreacion,
                    fechaEnvio = fechaEnMilisegundos(datos["fechaEnvio"]),
                    programada = (datos["programada"] as? Boolean) ?: false,
                    fechaProgramada = fechaEnMilisegundos(datos["fechaProgramada"]),
                    estado = datos["estado"] as? String ?: ESTADO_PENDIENTE
                )
            }
            .sortedByDescending { it.fechaEnvio ?: it.fechaProgramada ?: it.fechaCreacion }
    }

    /**
     * resolverDestinatarios
     * ---------------------
     * Comprueba qué clientes del objetivo tienen firebaseUid válido en
     * Firestore (pueden recibir buzón) y cuáles quedan fuera por no estar
     * vinculados. Usado por la UI para mostrar "Se enviará a X de Y clientes
     * vinculados" antes de confirmar.
     */
    suspend fun resolverDestinatarios(
        negocioId: String,
        modoDestino: String,
        idsSeleccionados: List<Int>
    ): ResolucionDestinatarios {
        val clientes = obtenerClientesDelNegocio(negocioId)
        val idsObjetivo = when (modoDestino) {
            "INDIVIDUAL", "GRUPO" -> idsSeleccionados.toSet()
            else -> null
        }
        val objetivo = if (idsObjetivo == null) {
            clientes
        } else {
            clientes.filter { it.first in idsObjetivo }
        }
        val destinatarios = objetivo.mapNotNull { (idCliente, firebaseUid) ->
            firebaseUid?.let { DestinatarioResuelto(idCliente, it) }
        }
        return ResolucionDestinatarios(
            destinatarios = destinatarios,
            omitidos = objetivo.size - destinatarios.size,
            totalObjetivo = objetivo.size
        )
    }

    /**
     * crearNotificacion
     * -----------------
     * Prepara los documentos de una notificación del ADMIN.
     *
     *  - Inmediata (programada = false):
     *      notificaciones/{id} en PENDIENTE + buzones de los destinatarios
     *      (lotes de <= 500 escrituras). El estado final (ENVIADA/ERROR) y el
     *      push FCM real los resuelve la Cloud Function (Fase E): la app NO
     *      marca ENVIADA ni asume que el push se ha enviado.
     *  - Programada (programada = true):
     *      notificaciones/{id} en PROGRAMADA con fechaProgramada. NO crea
     *      buzones: los generará Cloud Functions cuando llegue la fecha.
     *
     * `destinatarios` deben ser los ya resueltos (firebaseUid válido) para
     * una notificación inmediata; para programadas puede ir vacía y se usan
     * `idsObjetivo` como destinatarios previstos.
     */
    suspend fun crearNotificacion(
        negocioId: String,
        titulo: String,
        mensaje: String,
        modoDestino: String,
        clienteId: Int?,
        destinatarios: List<DestinatarioResuelto>,
        idsObjetivo: List<Int>,
        programada: Boolean,
        fechaProgramada: Long?,
        tipo: String = TIPO_MANUAL,
        origen: String = ORIGEN_MANUAL,
        notificacionId: String? = null
    ): ResultadoAutenticacion {
        val uid = auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")

        val idNotificacion = notificacionId ?: generarIdNotificacion()
        val tipoFinal = if (programada && tipo == TIPO_MANUAL) TIPO_PROGRAMADA else tipo
        val idsParaRegistro = if (programada) {
            idsObjetivo.distinct()
        } else {
            destinatarios.map { it.idCliente }.distinct()
        }

        val docPrincipal = mapaDeNotificacion(
            negocioId = negocioId,
            titulo = titulo,
            mensaje = mensaje,
            tipo = tipoFinal,
            origen = origen,
            modoDestino = modoDestino,
            clienteId = clienteId,
            idsClientes = idsParaRegistro,
            programada = programada,
            fechaProgramada = fechaProgramada,
            estado = if (programada) ESTADO_PROGRAMADA else ESTADO_PENDIENTE
        )

        return try {
            db.collection(COLECCION_NOTIFICACIONES)
                .document(idNotificacion)
                .set(docPrincipal)
                .esperar()

            if (!programada) {
                if (destinatarios.isEmpty()) {
                    // Sin destinatarios vinculados: no se puede enviar.
                    // Se deja el registro en ERROR para no arrastrar un PENDIENTE eterno.
                    db.collection(COLECCION_NOTIFICACIONES)
                        .document(idNotificacion)
                        .update("estado", ESTADO_ERROR)
                        .esperar()
                    return ResultadoAutenticacion(
                        false,
                        "No hay clientes vinculados para recibir la notificación"
                    )
                }
                crearBuzones(
                    negocioId = negocioId,
                    notificacionId = idNotificacion,
                    titulo = titulo,
                    mensaje = mensaje,
                    tipo = tipoFinal,
                    origen = origen,
                    destinatarios = destinatarios
                )
                // NOTA (Fase E): el envío inmediato se deja en PENDIENTE a
                // propósito. La Cloud Function onDocumentCreated reclamará el
                // documento (PENDIENTE -> ENVIADA) y hará el push FCM real.
                // La app NO asume que el push se ha enviado correctamente.
            }

            Log.i(
                TAG,
                "Notificación creada: $idNotificacion " +
                    "programada=$programada destinatarios=${destinatarios.size}"
            )
            ResultadoAutenticacion(
                true,
                if (programada) "Notificación programada" else "Notificación creada"
            )
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Error creando notificación $idNotificacion código=${e.code}", e)
            ResultadoAutenticacion(false, mensajeDe(e))
        } catch (e: Exception) {
            Log.e(TAG, "Error creando notificación $idNotificacion", e)
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * existeNotificacionFinalizada
     * ----------------------------
     * Indica si ya existe un documento de notificación con ese id y su estado
     * ya no es PENDIENTE (p. ej. ya ENVIADA por Cloud Functions). Sirve para
     * no sobrescribir una notificación automática que el backend ya procesó.
     */
    suspend fun existeNotificacionFinalizada(notificacionId: String): Boolean {
        return try {
            val documento = db.collection(COLECCION_NOTIFICACIONES)
                .document(notificacionId)
                .get()
                .esperar()
            documento.exists() && documento.getString("estado") != ESTADO_PENDIENTE
        } catch (e: Exception) {
            false
        }
    }

    /**
     * cancelarNotificacion
     * --------------------
     * Pone el estado de una notificación programada a CANCELADA. Las Rules
     * solo permiten cambiar estado/fechaEnvio/idsClientes, por lo que el
     * contenido nunca se puede editar.
     */
    suspend fun cancelarNotificacion(notificacionId: String): ResultadoAutenticacion {
        return try {
            db.collection(COLECCION_NOTIFICACIONES)
                .document(notificacionId)
                .update("estado", ESTADO_CANCELADA)
                .esperar()
            ResultadoAutenticacion(true, "Notificación cancelada")
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Error cancelando notificación $notificacionId código=${e.code}", e)
            ResultadoAutenticacion(false, mensajeDe(e))
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelando notificación $notificacionId", e)
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * obtenerConfiguracion
     * --------------------
     * Lee configuracion_notificaciones/{negocioId}. Devuelve null si el
     * documento no existe todavía.
     */
    suspend fun obtenerConfiguracion(negocioId: String): ConfiguracionNotificaciones? {
        return try {
            val documento = db.collection(COLECCION_CONFIG)
                .document(negocioId)
                .get()
                .esperar()
            if (!documento.exists()) return null
            val morosidad = documento.get("morosidad") as? Map<*, *>
            val bajaConfirmada = documento.get("bajaConfirmada") as? Map<*, *>
            ConfiguracionNotificaciones(
                morosidadActiva = (morosidad?.get("activa") as? Boolean) ?: false,
                recordatorioHoras = enteroDe(morosidad?.get("recordatorioHoras")) ?: 0,
                bajaConfirmadaActiva = (bajaConfirmada?.get("activa") as? Boolean) ?: false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error leyendo configuración de notificaciones", e)
            null
        }
    }

    /**
     * guardarConfiguracion
     * --------------------
     * Crea o actualiza configuracion_notificaciones/{negocioId}. Para el
     * recordatorio de morosidad: 0 = desactivado, 24 = activado (cada 24h).
     */
    suspend fun guardarConfiguracion(
        negocioId: String,
        config: ConfiguracionNotificaciones
    ): ResultadoAutenticacion {
        return try {
            val datos = mapOf(
                "morosidad" to mapOf(
                    "activa" to config.morosidadActiva,
                    "recordatorioHoras" to config.recordatorioHoras
                ),
                "bajaConfirmada" to mapOf(
                    "activa" to config.bajaConfirmadaActiva
                )
            )
            val referencia = db.collection(COLECCION_CONFIG).document(negocioId)
            val existente = referencia.get().esperar()
            if (existente.exists()) {
                referencia.update(datos).esperar()
            } else {
                referencia.set(mapOf("negocioId" to negocioId) + datos).esperar()
            }
            ResultadoAutenticacion(true, "Configuración guardada")
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Error guardando configuración de notificaciones código=${e.code}", e)
            ResultadoAutenticacion(false, mensajeDe(e))
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando configuración de notificaciones", e)
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    // ------------------------------------------------------------------------
    // Helpers privados
    // ------------------------------------------------------------------------

    /**
     * crearBuzones
     * ------------
     * Crea notificaciones_por_destinatario/{clienteId}_{notificacionId} para
     * cada destinatario respetando el límite de 500 escrituras por WriteBatch
     * (chunking). El documentId determinista lo exigen las Rules.
     */
    private suspend fun crearBuzones(
        negocioId: String,
        notificacionId: String,
        titulo: String,
        mensaje: String,
        tipo: String,
        origen: String,
        destinatarios: List<DestinatarioResuelto>
    ) {
        destinatarios.chunked(MAX_ESCRITURAS_POR_BATCH).forEach { lote ->
            val batch = db.batch()
            lote.forEach { destino ->
                batch.set(
                    db.collection(COLECCION_BUZON)
                        .document("${destino.idCliente}_$notificacionId"),
                    mapOf(
                        "negocioId" to negocioId,
                        "notificacionId" to notificacionId,
                        "clienteId" to destino.idCliente,
                        "firebaseUid" to destino.firebaseUid,
                        "titulo" to titulo,
                        "mensaje" to mensaje,
                        "tipo" to tipo,
                        "origen" to origen,
                        "fechaEnvio" to Timestamp.now(),
                        "leida" to false
                    )
                )
            }
            batch.commit().esperar()
        }
    }

    /**
     * obtenerClientesDelNegocio
     * -------------------------
     * Consulta `clientes` del negocio (query con negocioId, requerida por la
     * regla de list) y devuelve (idCliente, firebaseUid). La fuente de verdad
     * del vínculo es Firestore, no Room.
     */
    private suspend fun obtenerClientesDelNegocio(negocioId: String): List<Pair<Int, String?>> {
        val snapshots = db.collection(COLECCION_CLIENTES)
            .whereEqualTo("negocioId", negocioId)
            .get()
            .esperar()
        return snapshots.documents.mapNotNull { documento ->
            val idCliente = enteroDe(documento.get("idCliente")) ?: return@mapNotNull null
            idCliente to documento.getString("firebaseUid")
        }
    }

    /**
     * mapaDeNotificacion
     * ------------------
     * Construye el documento de notificaciones/{id} con exactamente las
     * claves permitidas por el hasOnly de las Rules.
     */
    private fun mapaDeNotificacion(
        negocioId: String,
        titulo: String,
        mensaje: String,
        tipo: String,
        origen: String,
        modoDestino: String,
        clienteId: Int?,
        idsClientes: List<Int>,
        programada: Boolean,
        fechaProgramada: Long?,
        estado: String
    ): Map<String, Any?> {
        val mapa = mutableMapOf<String, Any?>(
            "negocioId" to negocioId,
            "titulo" to titulo,
            "mensaje" to mensaje,
            "tipo" to tipo,
            "origen" to origen,
            "modoDestino" to modoDestino,
            "idsClientes" to idsClientes,
            "fechaCreacion" to Timestamp.now(),
            "programada" to programada,
            "estado" to estado
        )
        if (clienteId != null) {
            mapa["clienteId"] = clienteId
        }
        if (fechaProgramada != null) {
            mapa["fechaProgramada"] = Timestamp(java.util.Date(fechaProgramada))
        }
        return mapa
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

    private fun generarIdNotificacion(): String =
        "n_${System.currentTimeMillis()}_${(1000..9999).random()}"

    private fun mensajeDe(e: Exception): String {
        return when {
            e.message?.contains("permission", ignoreCase = true) == true ->
                "No tienes permisos para gestionar notificaciones"
            else -> e.message ?: "Error inesperado al gestionar la notificación"
        }
    }
}
