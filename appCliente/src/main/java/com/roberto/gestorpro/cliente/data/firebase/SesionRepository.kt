package com.roberto.gestorpro.cliente.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.roberto.gestorpro.cliente.model.Servicio
import com.roberto.gestorpro.cliente.model.Sesion
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SesionRepository
 * ----------------
 * Repositorio de lectura de SERVICIOS y SESIONES para el CLIENTE.
 *
 * El CLIENTE no puede listar servicios (las Rules solo permiten get de
 * servicios ACTIVOS de su negocio) ni hacer consultas globales de sesiones
 * (las Rules se evalúan por documento y una consulta que devuelva una sesión
 * no autorizada falla entera). Por eso el acceso se hace por servicio:
 *
 *   1. getDoc servicios/{idServicio}  -> null si inactivo/eliminado (permiso);
     *   2. getDocs sesiones.where("idServicio","==",id)
     *      .where("negocioId","==",negocioId) -> solo ese servicio y negocio.
 *
 * Un error de red o de otro tipo se propaga para que el ViewModel muestre un
 * estado de error; los errores de permiso (servicio inactivo/eliminado) se
 * traducen a "no disponible" sin romper la pantalla.
 */
@Singleton
class SesionRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    companion object {
        private const val COLECCION_SERVICIOS = "servicios"
        private const val COLECCION_SESIONES = "sesiones"
    }

    /**
     * obtenerServicioActivo
     * ---------------------
     * Lee servicios/{idServicio}. Las Rules solo permiten al CLIENTE leer un
     * servicio ACTIVO de su negocio. El negocio se comprueba también en el
     * modelo para no mezclar datos de otro negocio.
     */
    suspend fun obtenerServicioActivo(idServicio: Int, negocioId: String): Servicio? {
        return try {
            val documento = db.collection(COLECCION_SERVICIOS)
                .document(idServicio.toString())
                .get()
                .esperar()
            if (!documento.exists()) return null
            val datos = documento.data ?: return null
            val servicio = Servicio(
                idServicio = (datos["idServicio"] as? Number)?.toInt() ?: idServicio,
                negocioId = datos["negocioId"] as? String ?: "",
                nombre = datos["nombre"] as? String ?: "",
                descripcion = datos["descripcion"] as? String ?: "",
                activo = datos["activo"] as? Boolean ?: false
            )
            servicio.takeIf { it.negocioId == negocioId && it.activo }
        } catch (e: Exception) {
            if (e.message?.contains("permission", ignoreCase = true) == true) null else throw e
        }
    }

    /**
     * obtenerSesionesPorServicio
     * --------------------------
     * Consulta las sesiones de un servicio y negocio con filtros de igualdad
     * y las devuelve sin ordenar; el filtro del día actual y el orden por hora
     * los aplica el ViewModel en memoria.
     * Si la consulta se deniega (servicio desactivado entre comprobaciones)
     * se devuelve una lista vacía para omitir ese servicio.
     */
    suspend fun obtenerSesionesPorServicio(idServicio: Int, negocioId: String): List<Sesion> {
        return try {
            db.collection(COLECCION_SESIONES)
                .whereEqualTo("idServicio", idServicio)
                .whereEqualTo("negocioId", negocioId)
                .get()
                .esperar()
                .documents.mapNotNull { documento ->
                    val datos = documento.data ?: return@mapNotNull null
                    val idSesion = (datos["idSesion"] as? Number)?.toInt() ?: return@mapNotNull null
                    Sesion(
                        idSesion = idSesion,
                        negocioId = datos["negocioId"] as? String ?: "",
                        idServicio = (datos["idServicio"] as? Number)?.toInt() ?: idServicio,
                        fecha = (datos["fecha"] as? Number)?.toLong() ?: 0L,
                        hora = datos["hora"] as? String ?: "",
                        duracionMinutos = (datos["duracionMinutos"] as? Number)?.toInt() ?: 0,
                        capacidad = (datos["capacidad"] as? Number)?.toInt() ?: 0,
                        plazasDisponibles = (datos["plazasDisponibles"] as? Number)?.toInt() ?: 0
                    )
                }
        } catch (e: Exception) {
            if (e.message?.contains("permission", ignoreCase = true) == true) emptyList() else throw e
        }
    }
}
