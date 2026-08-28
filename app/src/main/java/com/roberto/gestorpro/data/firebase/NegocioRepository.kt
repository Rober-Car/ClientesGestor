package com.roberto.gestorpro.data.firebase

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ResultadoLogo
 * -------------
 * ✔ TIPO: data class
 * Resultado de la subida del logo: éxito/mensaje y, en caso de éxito, la URL
 * remota de descarga que se guarda en Firestore y en DataStore.
 */
data class ResultadoLogo(
    val exito: Boolean,
    val mensaje: String,
    val url: String? = null
)

/**
 * NegocioRepository
 * -----------------
 * ✔ TIPO: clase @Singleton inyectada por Hilt (data/firebase)
 * Es el repositorio que encapsula la gestión remota del negocio del ADMIN:
 * creación atómica de negocios/{negocioId} + negocios_publicos/{negocioId}
 * junto a usuarios/{uid}, y modificación posterior del código maestro.
 * Sirve para que las pantallas no toquen Firestore directamente y para que
 * toda escritura respete las Security Rules vigentes (operaciones atómicas).
 */
@Singleton
class NegocioRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    companion object {
        private const val COLECCION_USUARIOS = "usuarios"
        private const val COLECCION_NEGOCIOS = "negocios"
        private const val COLECCION_NEGOCIOS_PUBLICOS = "negocios_publicos"

        /**
         * El negocioId del ADMIN es su propio UID: determinista, único y
         * coherente con la regla que exige getAfter(usuarios).negocioId.
         */
        fun negocioIdDeAdmin(uid: String): String = uid
    }

    /**
     * existeNegocioPropio
     * -------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → Boolean
     * Indica si el ADMIN autenticado ya tiene creado su documento de negocio.
     * Sirve a MiNegocioScreen para decidir su modo dual (sin negocio → alta;
     * con negocio → edición del código maestro).
     */
    suspend fun existeNegocioPropio(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            db.collection(COLECCION_NEGOCIOS)
                .document(negocioIdDeAdmin(uid))
                .get()
                .esperar()
                .exists()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * obtenerCodigoMaestro
     * --------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → String?
     * Lee el código maestro actual del negocio propio.
     * Devuelve null cuando no hay sesión, no hay negocio o falla la lectura.
     * Sirve para precargar MiNegocioScreen en modo edición.
     */
    suspend fun obtenerCodigoMaestro(): String? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val documento = db.collection(COLECCION_NEGOCIOS)
                .document(negocioIdDeAdmin(uid))
                .get()
                .esperar()
            if (documento.exists()) documento.getString("codigoMaestro") else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * crearNegocio
     * ------------
     * ✔ TIPO: método (fun) suspend de Kotlin → ResultadoAutenticacion
     * Crea en un único Batch negocios/{negocioId}, negocios_publicos/{negocioId}
     * y la asignación usuarios/{uid}.negocioId, exactamente lo que exigen las
     * Security Rules. Devuelve exito true o el mensaje de error para la UI.
     */
    suspend fun crearNegocio(
        nombre: String,
        codigoMaestro: String
    ): ResultadoAutenticacion {
        val uid = auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")

        val negocioId = negocioIdDeAdmin(uid)
        val batch = db.batch()

        batch.set(
            db.collection(COLECCION_NEGOCIOS).document(negocioId),
            mapOf(
                "adminUid" to uid,
                "nombre" to nombre,
                "codigoMaestro" to codigoMaestro
            )
        )

        batch.set(
            db.collection(COLECCION_NEGOCIOS_PUBLICOS).document(negocioId),
            mapOf(
                "nombre" to nombre,
                "codigoMaestro" to codigoMaestro
            )
        )

        batch.update(
            db.collection(COLECCION_USUARIOS).document(uid),
            mapOf("negocioId" to negocioId)
        )

        return try {
            batch.commit().esperar()
            ResultadoAutenticacion(true, "Negocio creado correctamente")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * guardarCodigoMaestro
     * --------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → ResultadoAutenticacion
     * Actualiza el código maestro en negocios/{id} y negocios_publicos/{id}
     * dentro del mismo Batch. Cambiarlo no afecta a clientes ya vinculados.
     * Sirve al modo edición de MiNegocioScreen.
     */
    suspend fun guardarCodigoMaestro(codigoMaestro: String): ResultadoAutenticacion {
        val uid = auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")

        val negocioId = negocioIdDeAdmin(uid)
        val batch = db.batch()

        batch.update(
            db.collection(COLECCION_NEGOCIOS).document(negocioId),
            mapOf("codigoMaestro" to codigoMaestro)
        )

        batch.update(
            db.collection(COLECCION_NEGOCIOS_PUBLICOS).document(negocioId),
            mapOf("codigoMaestro" to codigoMaestro)
        )

        return try {
            batch.commit().esperar()
            ResultadoAutenticacion(true, "Código maestro actualizado")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * guardarNombreNegocio
     * --------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → ResultadoAutenticacion
     * Actualiza el nombre del negocio en negocios/{id} y negocios_publicos/{id}
     * dentro del mismo WriteBatch (mismo mecanismo que guardarCodigoMaestro),
     * para que ambos documentos queden siempre coherentes. La app Cliente lee
     * el nombre desde negocios_publicos/{id}, por lo que este método es el que
     * propaga el cambio que el ADMIN hace en MiNegocioScreen.
     * Sirve al modo edición de MiNegocioScreen.
     */
    suspend fun guardarNombreNegocio(nombre: String): ResultadoAutenticacion {
        val uid = auth.currentUser?.uid
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")

        val negocioId = negocioIdDeAdmin(uid)
        val batch = db.batch()

        batch.update(
            db.collection(COLECCION_NEGOCIOS).document(negocioId),
            mapOf("nombre" to nombre)
        )

        batch.update(
            db.collection(COLECCION_NEGOCIOS_PUBLICOS).document(negocioId),
            mapOf("nombre" to nombre)
        )

        return try {
            batch.commit().esperar()
            ResultadoAutenticacion(true, "Nombre actualizado")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * guardarLogoRemoto
     * -----------------
     * ✔ TIPO: método (fun) suspend de Kotlin → ResultadoLogo
     * Sube el logo del gimnasio a Firebase Storage en negocios/{negocioId}/logo.jpg
     * (sobrescribiendo el anterior), obtiene la URL de descarga y actualiza el
     * campo logo de negocios/{id} y negocios_publicos/{id} en un único WriteBatch.
     * No se guarda la imagen en Firestore, solo la URL. El archivo local solo se
     * usa como origen de la subida. Si falla la subida o el Batch se devuelve el
     * error sin marcar la operación como exitosa (el logo local queda para reintentar).
     */
    suspend fun guardarLogoRemoto(rutaLocal: String): ResultadoLogo {
        val uid = auth.currentUser?.uid
            ?: return ResultadoLogo(false, "No hay ningún usuario autenticado")

        val negocioId = negocioIdDeAdmin(uid)

        return try {
            val referencia = storage.reference.child("negocios/$negocioId/logo.jpg")
            referencia.putFile(Uri.fromFile(File(rutaLocal))).esperar()
            val url = referencia.downloadUrl.esperar().toString()

            val batch = db.batch()
            batch.update(
                db.collection(COLECCION_NEGOCIOS).document(negocioId),
                mapOf("logo" to url)
            )
            batch.update(
                db.collection(COLECCION_NEGOCIOS_PUBLICOS).document(negocioId),
                mapOf("logo" to url)
            )
            batch.commit().esperar()

            ResultadoLogo(true, "Logo actualizado", url)
        } catch (e: Exception) {
            ResultadoLogo(false, mensajeDe(e))
        }
    }

    /**
     * mensajeDe
     * ---------
     * ✔ TIPO: método (fun) privado de Kotlin → String
     * Traduce los errores típicos de Firestore a mensajes en español.
     * Sirve para que la UI nunca muestre textos técnicos en inglés.
     */
    private fun mensajeDe(e: Exception): String {
        return when {
            e.message?.contains("permission", ignoreCase = true) == true ->
                "No tienes permisos para esta operación"
            else -> e.message ?: "Error inesperado. Inténtalo de nuevo"
        }
    }
}
