package com.roberto.gestorpro.cliente.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NegocioRepository
 * -----------------
 * ✔ TIPO: clase @Singleton inyectada por Hilt
 * Encapsula la lectura pública de negocios_publicos para resolver un negocio
 * a partir de su código maestro.
 */
@Singleton
class NegocioRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    companion object {
        private const val COLECCION_NEGOCIOS_PUBLICOS = "negocios_publicos"
    }

    /**
     * resolverNegocioPorCodigoMaestro
     * -------------------------------
     * Busca el negocio público cuyo codigoMaestro coincide.
     * Devuelve el negocioId o null si no existe.
     */
    suspend fun resolverNegocioPorCodigoMaestro(codigoMaestro: String): String? {
        return try {
            val coincidencias = db.collection(COLECCION_NEGOCIOS_PUBLICOS)
                .whereEqualTo("codigoMaestro", codigoMaestro.trim())
                .limit(1)
                .get()
                .esperar()
            coincidencias.documents.firstOrNull()?.id
        } catch (_: Exception) {
            null
        }
    }

    /**
     * obtenerNombreNegocio
     * --------------------
     * Lee el nombre de negocios_publicos/{negocioId} (información pública).
     */
    suspend fun obtenerNombreNegocio(negocioId: String): String? {
        return try {
            db.collection(COLECCION_NEGOCIOS_PUBLICOS)
                .document(negocioId)
                .get()
                .esperar()
                .getString("nombre")
        } catch (_: Exception) {
            null
        }
    }
}
