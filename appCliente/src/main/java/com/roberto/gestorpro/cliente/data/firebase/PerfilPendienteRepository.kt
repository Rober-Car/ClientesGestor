package com.roberto.gestorpro.cliente.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PerfilPendiente
 * ---------------
 * ✔ TIPO: data class
 * Datos personales de un CLIENTE registrado que aún no pertenece a un negocio.
 */
data class PerfilPendiente(
    val nombre: String,
    val apellidos: String,
    val dni: String,
    val telefono: String,
    val email: String?,
    val foto: String,
    val fechaNacimiento: Long
)

/**
 * PerfilPendienteRepository
 * -------------------------
 * ✔ TIPO: clase @Singleton inyectada por Hilt
 * Gestiona el documento temporal perfiles_pendientes/{uid} del CLIENTE.
 */
@Singleton
class PerfilPendienteRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    companion object {
        private const val COLECCION_PERFILES = "perfiles_pendientes"
    }

    /**
     * guardar
     * -------
     * Crea o actualiza el perfil pendiente completo del usuario autenticado
     * (VÍA 2: "No tengo código").
     */
    suspend fun guardar(uid: String, perfil: PerfilPendiente): ResultadoAutenticacion {
        return try {
            db.collection(COLECCION_PERFILES)
                .document(uid)
                .set(
                    mapOf(
                        "nombre" to perfil.nombre,
                        "apellidos" to perfil.apellidos,
                        "dni" to perfil.dni.trim().uppercase(),
                        "telefono" to perfil.telefono,
                        "email" to perfil.email,
                        "foto" to perfil.foto,
                        "fechaNacimiento" to perfil.fechaNacimiento
                    )
                )
                .esperar()
            ResultadoAutenticacion(true, "Perfil guardado")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * guardarDeclaracion
     * ------------------
     * Crea o actualiza la declaración temporal de VÍA 1 en
     * perfiles_pendientes/{uid}: únicamente { dni, negocioId }. Sirve para que
     * las Rules validen que el índice consultado corresponde exactamente a la
     * combinación negocio+DNI que el CLIENTE introdujo. NO es un perfil ficticio:
     * es el dato declarado en el momento de la vinculación y se borra al terminar.
     */
    suspend fun guardarDeclaracion(
        uid: String,
        dni: String,
        negocioId: String
    ): ResultadoAutenticacion {
        return try {
            db.collection(COLECCION_PERFILES)
                .document(uid)
                .set(
                    mapOf(
                        "dni" to dni.trim().uppercase(),
                        "negocioId" to negocioId
                    )
                )
                .esperar()
            ResultadoAutenticacion(true, "Declaración guardada")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * leer
     * ----
     * Lee el perfil pendiente del usuario (null si no existe).
     */
    suspend fun leer(uid: String): PerfilPendiente? {
        return try {
            val documento = db.collection(COLECCION_PERFILES)
                .document(uid)
                .get()
                .esperar()
            if (!documento.exists()) return null
            PerfilPendiente(
                nombre = documento.getString("nombre") ?: "",
                apellidos = documento.getString("apellidos") ?: "",
                dni = documento.getString("dni") ?: "",
                telefono = documento.getString("telefono") ?: "",
                email = documento.getString("email"),
                foto = documento.getString("foto") ?: "",
                fechaNacimiento = documento.getLong("fechaNacimiento") ?: 0L
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * borrar
     * ------
     * Elimina el perfil pendiente tras completar la vinculación.
     */
    suspend fun borrar(uid: String) {
        try {
            db.collection(COLECCION_PERFILES)
                .document(uid)
                .delete()
                .esperar()
        } catch (_: Exception) {
        }
    }

    private fun mensajeDe(e: Exception): String {
        return when {
            e.message?.contains("permission", ignoreCase = true) == true ->
                "No tienes permisos para esta operación"
            else -> e.message ?: "Error inesperado. Inténtalo de nuevo"
        }
    }
}
