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
 * Sirve para mantener el espejo remoto que permite la vinculación de clientes
 * sin duplicidades: Room sigue siendo la fuente de verdad local y Firestore
 * la copia visible para el CLIENTE.
 */
@Singleton
class ClienteRemotoRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    companion object {
        private const val COLECCION_CLIENTES = "clientes"

        /**
         * negocioIdDelAdmin
         * -----------------
         * El negocioId del ADMIN es su propio UID, igual que en NegocioRepository.
         */
        fun negocioIdDelAdmin(uid: String): String = uid
    }

    /**
     * existeClienteRemoto
     * -------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → Boolean
     * Indica si la ficha ya existe en Firestore. Sirve para bloquear la
     * generación de enlaces mientras la réplica no haya tenido éxito.
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
     * firebaseUid nace null: solo lo escribirá el CLIENTE al reclamarla.
     */
    suspend fun crearClienteRemoto(entidad: ClienteEntity): ResultadoAutenticacion {
        val uid = auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")

        return try {
            db.collection(COLECCION_CLIENTES)
                .document(entidad.idCliente.toString())
                .set(mapaDeAlta(entidad, negocioIdDelAdmin(uid)))
                .esperar()
            ResultadoAutenticacion(true, "Ficha sincronizada")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * actualizarClienteRemoto
     * -----------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → ResultadoAutenticacion
     * Replica la edición sobre los campos de gestión que permiten las Rules
     * (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento,
     * observaciones, serviciosContratados, estado, fechas y llave). Nunca
     * toca firebaseUid, codigoVinculacion ni identificadores.
     */
    suspend fun actualizarClienteRemoto(entidad: ClienteEntity): ResultadoAutenticacion {
        auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")

        return try {
            db.collection(COLECCION_CLIENTES)
                .document(entidad.idCliente.toString())
                .update(mapaDeEdicion(entidad))
                .esperar()
            ResultadoAutenticacion(true, "Cambios sincronizados")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * mapaDeAlta
     * ----------
     * ✔ TIPO: método (fun) privado de Kotlin → Map<String, Any?>
     * Construye el documento completo del alta según el contrato acordado:
     * estados con el nombre exacto del enum Room y fechas como Timestamp.
     */
    private fun mapaDeAlta(entidad: ClienteEntity, negocioId: String): Map<String, Any?> {
        return mapOf(
            "idCliente" to entidad.idCliente,
            "negocioId" to negocioId,
            "firebaseUid" to null,
            "codigoVinculacion" to null,
            "nombre" to entidad.nombre,
            "apellidos" to entidad.apellidos,
            "dni" to entidad.dni,
            "telefono" to entidad.telefono,
            "email" to entidad.email,
            "foto" to entidad.foto,
            "fechaNacimiento" to timestampDe(entidad.fechaNacimiento),
            "fechaRegistro" to timestampDe(entidad.fechaRegistro),
            "fechaAlta" to entidad.fechaAlta?.let { timestampDe(it) },
            "fechaBaja" to entidad.fechaBaja?.let { timestampDe(it) },
            "estado" to entidad.estado.name,
            "tieneLlave" to entidad.tieneLlave,
            "observaciones" to entidad.observaciones,
            "serviciosContratados" to entidad.serviciosContratados,
            "fechaInicioActual" to null,
            "fechaFinActual" to null
        )
    }

    /**
     * mapaDeEdicion
     * -------------
     * ✔ TIPO: método (fun) privado de Kotlin → Map<String, Any?>
     * Construye el update de edición limitado a los campos de gestión
     * autorizados por las Security Rules.
     */
    private fun mapaDeEdicion(entidad: ClienteEntity): Map<String, Any?> {
        return mapOf(
            "nombre" to entidad.nombre,
            "apellidos" to entidad.apellidos,
            "dni" to entidad.dni,
            "telefono" to entidad.telefono,
            "email" to entidad.email,
            "foto" to entidad.foto,
            "fechaNacimiento" to timestampDe(entidad.fechaNacimiento),
            "observaciones" to entidad.observaciones,
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
