package com.roberto.gestorpro.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.roberto.gestorpro.data.entity.SesionEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SesionRemotoRepository
 * ----------------------
 * Repositorio que replica las sesiones del ADMIN a Firestore en
 * sesiones/{idSesion}, con el mismo idSesion de Room.
 *
 * El negocioId remoto es el UID del ADMIN autenticado (convención
 * ADMIN UID == negocioId), aunque en Room el campo negocioId siga vacío.
 *
 * La creación comprueba la existencia previa del documento para no
 * sobrescribir una sesión de otro negocio (colisión de idSesion Int).
 *
 * Las operaciones de borrado de sesiones están preparadas para la futura
 * cascada de reservas, pero en esta fase solo eliminan documentos de sesiones.
 */
@Singleton
class SesionRemotoRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    companion object {
        private const val COLECCION_SESIONES = "sesiones"

        /**
         * El negocioId del ADMIN es su propio UID, igual que en los demás
         * repositorios remotos del proyecto.
         */
        fun negocioIdDeAdmin(uid: String): String = uid
    }

    /**
     * crearSesionRemoto
     * -----------------
     * Crea sesiones/{idSesion} con el negocioId real del ADMIN autenticado.
     * Antes comprueba (con lectura y Transaction) que el id no pertenece ya a
     * otro negocio; si hay colisión, NO sobrescribe y devuelve error.
     */
    suspend fun crearSesionRemoto(sesion: SesionEntity): ResultadoAutenticacion {
        val uid = auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")

        val negocioId = negocioIdDeAdmin(uid)
        val referencia = db.collection(COLECCION_SESIONES)
            .document(sesion.idSesion.toString())

        val existente = try {
            referencia.get().esperar()
        } catch (e: Exception) {
            if (e.message?.contains("permission", ignoreCase = true) == true) {
                return ResultadoAutenticacion(
                    false,
                    "El idSesion ya está en uso por otro negocio"
                )
            }
            return ResultadoAutenticacion(false, mensajeDe(e))
        }

        if (existente.exists()) {
            return if (existente.getString("negocioId") == negocioId) {
                ResultadoAutenticacion(true, "Sesión ya sincronizada")
            } else {
                ResultadoAutenticacion(
                    false,
                    "El idSesion ya está en uso por otro negocio"
                )
            }
        }

        return try {
            db.runTransaction { transaction ->
                val enTransaccion = transaction.get(referencia)
                if (enTransaccion.exists()) {
                    if (enTransaccion.getString("negocioId") != negocioId) {
                        throw ColisionSesionException()
                    }
                } else {
                    transaction.set(referencia, mapaDeSesion(sesion, negocioId))
                }
            }.esperar()
            ResultadoAutenticacion(true, "Sesión sincronizada")
        } catch (e: ColisionSesionException) {
            ResultadoAutenticacion(false, "El idSesion ya está en uso por otro negocio")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * actualizarSesionRemoto
     * ----------------------
     * Actualiza en Firestore solo los campos editables de la sesión
     * (fecha, hora, duracionMinutos, capacidad, plazasDisponibles).
     * idSesion, negocioId e idServicio no cambian.
     */
    suspend fun actualizarSesionRemoto(sesion: SesionEntity): ResultadoAutenticacion {
        val uid = auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")
        return try {
            db.collection(COLECCION_SESIONES)
                .document(sesion.idSesion.toString())
                .update(
                    mapOf(
                        "fecha" to sesion.fecha,
                        "hora" to sesion.hora,
                        "duracionMinutos" to sesion.duracionMinutos,
                        "capacidad" to sesion.capacidad,
                        "plazasDisponibles" to sesion.plazasDisponibles
                    )
                )
                .esperar()
            ResultadoAutenticacion(true, "Sesión actualizada")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * eliminarSesionRemoto
     * --------------------
     * Elimina sesiones/{idSesion}. (La cascada de reservas se implementará
     * en la fase de reservas.)
     */
    suspend fun eliminarSesionRemoto(idSesion: Int): ResultadoAutenticacion {
        val uid = auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")
        return try {
            db.collection(COLECCION_SESIONES)
                .document(idSesion.toString())
                .delete()
                .esperar()
            ResultadoAutenticacion(true, "Sesión eliminada")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * eliminarSesionesFuturasDelServicioRemoto
     * -----------------------------------------
     * Elimina de Firestore las sesiones futuras (fecha >= desde) de un servicio.
     */
    suspend fun eliminarSesionesFuturasDelServicioRemoto(
        idServicio: Int,
        desde: Long
    ): ResultadoAutenticacion {
        val uid = auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")
        return try {
            val ids = obtenerIdsSesionesDelServicio(
                idServicio,
                negocioIdDeAdmin(uid)
            ) { fecha -> fecha >= desde }
            borrarSesionesPorIds(ids)
            ResultadoAutenticacion(true, "Sesiones futuras eliminadas")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * eliminarTodasLasSesionesDelServicioRemoto
     * -----------------------------------------
     * Elimina de Firestore todas las sesiones de un servicio.
     */
    suspend fun eliminarTodasLasSesionesDelServicioRemoto(
        idServicio: Int
    ): ResultadoAutenticacion {
        val uid = auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")
        return try {
            val ids = obtenerIdsSesionesDelServicio(
                idServicio,
                negocioIdDeAdmin(uid)
            ) { true }
            borrarSesionesPorIds(ids)
            ResultadoAutenticacion(true, "Sesiones eliminadas")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * sincronizarSesionesGeneradas
     * ----------------------------
     * Regenera la programación de un servicio en Firestore de forma atómica:
     * elimina las sesiones futuras del servicio (fecha >= desde) y crea las
     * sesiones nuevas en el mismo WriteBatch. Las sesiones pasadas se conservan.
     */
    suspend fun sincronizarSesionesGeneradas(
        idServicio: Int,
        desde: Long,
        sesiones: List<SesionEntity>
    ): ResultadoAutenticacion {
        val uid = auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")
        val negocioId = negocioIdDeAdmin(uid)
        return try {
            val idsFuturas = obtenerIdsSesionesDelServicio(
                idServicio,
                negocioId
            ) { fecha -> fecha >= desde }
            val batch = db.batch()
            idsFuturas.forEach { id ->
                batch.delete(db.collection(COLECCION_SESIONES).document(id.toString()))
            }
            sesiones.forEach { sesion ->
                batch.set(
                    db.collection(COLECCION_SESIONES).document(sesion.idSesion.toString()),
                    mapaDeSesion(sesion, negocioId)
                )
            }
            batch.commit().esperar()
            ResultadoAutenticacion(true, "Programación sincronizada")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * obtenerIdsSesionesDelServicio
     * -----------------------------
     * Consulta las sesiones de un servicio y su negocio con filtros de igualdad
     * y filtra en memoria con el predicado indicado.
     */
    private suspend fun obtenerIdsSesionesDelServicio(
        idServicio: Int,
        negocioId: String,
        aceptarFecha: (Long) -> Boolean
    ): List<Int> {
        val snapshots = db.collection(COLECCION_SESIONES)
            .whereEqualTo("idServicio", idServicio)
            .whereEqualTo("negocioId", negocioId)
            .get()
            .esperar()
        return snapshots.documents.mapNotNull { documento ->
            val idSesion = documento.getLong("idSesion")?.toInt()
            val fecha = documento.getLong("fecha")
            if (idSesion != null && fecha != null && aceptarFecha(fecha)) idSesion else null
        }
    }

    /**
     * borrarSesionesPorIds
     * --------------------
     * Borra las sesiones indicadas en un único WriteBatch.
     */
    private suspend fun borrarSesionesPorIds(ids: List<Int>) {
        if (ids.isEmpty()) return
        val batch = db.batch()
        ids.forEach { id ->
            batch.delete(db.collection(COLECCION_SESIONES).document(id.toString()))
        }
        batch.commit().esperar()
    }

    /**
     * mapaDeSesion
     * ------------
     * Construye el documento remoto de la sesión según el contrato acordado.
     * El negocioId real es el del ADMIN autenticado (su UID).
     */
    private fun mapaDeSesion(sesion: SesionEntity, negocioId: String): Map<String, Any?> {
        return mapOf(
            "idSesion" to sesion.idSesion,
            "negocioId" to negocioId,
            "idServicio" to sesion.idServicio,
            "fecha" to sesion.fecha,
            "hora" to sesion.hora,
            "duracionMinutos" to sesion.duracionMinutos,
            "capacidad" to sesion.capacidad,
            "plazasDisponibles" to sesion.plazasDisponibles
        )
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
 * ColisionSesionException
 * -----------------------
 * Excepción interna para señalar que el idSesion ya pertenece a otro negocio.
 */
private class ColisionSesionException : Exception()
