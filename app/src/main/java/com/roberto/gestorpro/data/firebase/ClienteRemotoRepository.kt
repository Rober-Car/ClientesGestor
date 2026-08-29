package com.roberto.gestorpro.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.roberto.gestorpro.data.entity.ClienteEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ClienteRemotoRepository
 * -----------------------
 * ✔ TIPO: clase @Singleton inyectada por Hilt (data/firebase)
 * Es el repositorio que replica a Firestore las fichas de cliente creadas y
 * gestionadas por el ADMIN en Room (mismo idCliente en ambas bases).
 * Sirve para mantener el espejo remoto que permite la vinculacion de clientes
 * sin duplicidades:
 *   - clientes/{idCliente}        -> ficha publica del cliente (sin observaciones)
 *   - indices_clientes/{negocio}_{dni} -> unicidad y localizacion por negocio+DNI
 *   - clientes_privados/{idCliente}    -> datos exclusivos del ADMIN (observaciones)
 */
@Singleton
class ClienteRemotoRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    companion object {
        private const val COLECCION_CLIENTES = "clientes"
        private const val COLECCION_INDICES = "indices_clientes"
        private const val COLECCION_PRIVADOS = "clientes_privados"

        /**
         * negocioIdDelAdmin
         * -----------------
         * El negocioId del ADMIN es su propio UID, igual que en NegocioRepository.
         */
        fun negocioIdDelAdmin(uid: String): String = uid

        /**
         * indiceId
         * --------
         * Documento de indice negocio+DNI: {negocioId}_{dni}. DNI normalizado
         * en mayusculas para que coincida con el documentId de Firestore.
         */
        fun indiceId(negocioId: String, dni: String): String =
            "${negocioId}_${dni.trim().uppercase()}"
    }

    /**
     * existeClienteRemoto
     * -------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → Boolean
     * Indica si la ficha ya existe en Firestore. Sirve para decidir en la UI
     * si se puede gestionar el enlace o si la ficha no esta sincronizada.
     */
    suspend fun existeClienteRemoto(idCliente: Int): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        if (uid.isBlank()) return false

        return try {
            db.collection(COLECCION_CLIENTES)
                .document(idCliente.toString())
                .get()
                .esperar()
                .exists()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * crearClienteRemoto
     * ------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → ResultadoAutenticacion
     * Replica el alta completa de la ficha con el mismo idCliente de Room.
     * En un unico Batch crea:
     *   - clientes/{idCliente} (firebaseUid = null, sin observaciones);
     *   - indices_clientes/{negocio}_{dni} (unicidad negocio+DNI);
     *   - clientes_privados/{idCliente} (observaciones, solo ADMIN).
     * Las Rules exigen que el indice nazca en el mismo Batch que la ficha.
     */
    suspend fun crearClienteRemoto(entidad: ClienteEntity): ResultadoAutenticacion {
        val uid = auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")
        if (entidad.dni.isBlank()) {
            return ResultadoAutenticacion(false, "La ficha necesita un DNI para crear el índice")
        }

        val negocioId = negocioIdDelAdmin(uid)
        val idIndice = indiceId(negocioId, entidad.dni)

        return try {
            val batch = db.batch()
            batch.set(
                db.collection(COLECCION_CLIENTES).document(entidad.idCliente.toString()),
                mapaDeAlta(entidad, negocioId)
            )
            batch.set(
                db.collection(COLECCION_INDICES).document(idIndice),
                mapOf(
                    "negocioId" to negocioId,
                    "dni" to entidad.dni.trim().uppercase(),
                    "clienteId" to entidad.idCliente
                )
            )
            batch.set(
                db.collection(COLECCION_PRIVADOS).document(entidad.idCliente.toString()),
                mapOf(
                    "negocioId" to negocioId,
                    "observaciones" to entidad.observaciones
                )
            )
            batch.commit().esperar()
            ResultadoAutenticacion(true, "Ficha sincronizada")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * actualizarClienteRemoto
     * -----------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → ResultadoAutenticacion
     * Replica la edicion de la ficha. En un unico Batch:
     *   - actualiza clientes/{idCliente} con los campos de gestion;
     *   - actualiza clientes_privados/{idCliente} (observaciones);
     *   - si cambia el DNI, borra el indice viejo y crea el nuevo (atomico).
     * Nunca toca firebaseUid ni identificadores.
     */
    suspend fun actualizarClienteRemoto(entidad: ClienteEntity, dniAnterior: String? = null): ResultadoAutenticacion {
        val uid = auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")

        val negocioId = negocioIdDelAdmin(uid)
        val dniNuevo = entidad.dni.trim().uppercase()
        val dniViejo = dniAnterior?.trim()?.uppercase()

        return try {
            val batch = db.batch()
            batch.update(
                db.collection(COLECCION_CLIENTES).document(entidad.idCliente.toString()),
                mapaDeEdicion(entidad)
            )
            batch.update(
                db.collection(COLECCION_PRIVADOS).document(entidad.idCliente.toString()),
                mapOf("observaciones" to entidad.observaciones)
            )
            if (dniViejo != null && dniViejo != dniNuevo) {
                batch.delete(
                    db.collection(COLECCION_INDICES).document(indiceId(negocioId, dniViejo))
                )
            }
            if (dniViejo == null || dniViejo != dniNuevo) {
                batch.set(
                    db.collection(COLECCION_INDICES).document(indiceId(negocioId, dniNuevo)),
                    mapOf(
                        "negocioId" to negocioId,
                        "dni" to dniNuevo,
                        "clienteId" to entidad.idCliente
                    )
                )
            }
            batch.commit().esperar()
            ResultadoAutenticacion(true, "Cambios sincronizados")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * actualizarServiciosContratadosRemoto
     * -------------------------------------
     * Replica SOLO la lista de servicios contratados de la ficha
     * (clientes/{idCliente}.serviciosContratados) con un update de un único
     * campo. No toca el resto de campos ni el indice negocio+DNI, por lo que
     * las Rules de indices_clientes (update: false) no se ven afectadas.
     */
    suspend fun actualizarServiciosContratadosRemoto(
        idCliente: Int,
        idsServicios: List<Int>
    ): ResultadoAutenticacion {
        val uid = auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")
        if (uid.isBlank()) {
            return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")
        }

        return try {
            db.collection(COLECCION_CLIENTES)
                .document(idCliente.toString())
                .update("serviciosContratados", idsServicios)
                .esperar()
            ResultadoAutenticacion(true, "Servicios sincronizados")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * mapaDeAlta
     * ----------
     * ✔ TIPO: método (fun) privado de Kotlin → Map<String, Any?>
     * Construye el documento publico del alta segun el contrato acordado:
     * estados con el nombre exacto del enum Room y fechas como Timestamp.
     * observaciones NO va aqui (es dato privado del ADMIN).
     */
    private fun mapaDeAlta(entidad: ClienteEntity, negocioId: String): Map<String, Any?> {
        return mapOf(
            "idCliente" to entidad.idCliente,
            "negocioId" to negocioId,
            "firebaseUid" to null,
            "nombre" to entidad.nombre,
            "apellidos" to entidad.apellidos,
            "dni" to entidad.dni.trim().uppercase(),
            "telefono" to entidad.telefono,
            "email" to entidad.email,
            "foto" to entidad.foto,
            "fechaNacimiento" to timestampDe(entidad.fechaNacimiento),
            "fechaRegistro" to timestampDe(entidad.fechaRegistro),
            "fechaAlta" to entidad.fechaAlta?.let { timestampDe(it) },
            "fechaBaja" to entidad.fechaBaja?.let { timestampDe(it) },
            "estado" to entidad.estado.name,
            "tieneLlave" to entidad.tieneLlave,
            "serviciosContratados" to entidad.serviciosContratados,
            "fechaInicioActual" to null,
            "fechaFinActual" to null
        )
    }

    /**
     * mapaDeEdicion
     * -------------
     * ✔ TIPO: método (fun) privado de Kotlin → Map<String, Any?>
     * Construye el update de edicion limitado a los campos de gestion
     * autorizados por las Security Rules.
     */
    private fun mapaDeEdicion(entidad: ClienteEntity): Map<String, Any?> {
        return mapOf(
            "nombre" to entidad.nombre,
            "apellidos" to entidad.apellidos,
            "dni" to entidad.dni.trim().uppercase(),
            "telefono" to entidad.telefono,
            "email" to entidad.email,
            "foto" to entidad.foto,
            "fechaNacimiento" to timestampDe(entidad.fechaNacimiento),
            "serviciosContratados" to entidad.serviciosContratados,
            "estado" to entidad.estado.name,
            "fechaAlta" to entidad.fechaAlta?.let { timestampDe(it) },
            "fechaBaja" to entidad.fechaBaja?.let { timestampDe(it) },
            "tieneLlave" to entidad.tieneLlave
        )
    }

    /**
     * timestampDe
     * -----------
     * ✔ TIPO: método (fun) privado de Kotlin → Timestamp
     * Convierte milisegundos de Room en Timestamp de Firestore.
     */
    private fun timestampDe(millis: Long): Timestamp = Timestamp(java.util.Date(millis))

    /**
     * mensajeDe
     * ---------
     * ✔ TIPO: método (fun) privado de Kotlin → String
     * Traduce los errores típicos de Firestore a mensajes en español.
     */
    private fun mensajeDe(e: Exception): String {
        return when {
            e.message?.contains("permission", ignoreCase = true) == true ->
                "No tienes permisos para sincronizar esta ficha"
            else -> e.message ?: "Error inesperado durante la sincronización"
        }
    }
}
