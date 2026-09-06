package com.roberto.gestorpro.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.net.URLDecoder

/**
 * LogoNegocioCache
 * ----------------
 * Lectura AUTENTICADA del logo del negocio (negocios/{negocioId}/logo.jpg) con
 * caché local determinista por URL. Es el MISMO patrón autenticado que se usa
 * para las fotos de perfil, porque las Storage Rules exigen autenticación y el
 * GET HTTP anónimo a la URL no es fiable en producción.
 *
 * - Caché: filesDir/logos_negocio/logo_<hashUrl>.jpg
 * - Si la URL cambia (nuevo logo), el hash cambia y se vuelve a descargar.
 * - Descarga a temporal y rename tras éxito (nunca queda un fichero incompleto).
 * - No gestiona rutas locales legacy (esas las muestran las pantallas con File).
 */
object LogoNegocioCache {

    private const val RUTA_RAIZ_NEGOCIOS = "negocios"
    private const val NOMBRE_LOGO = "logo.jpg"

    fun rutaLogo(negocioId: String): String =
        "$RUTA_RAIZ_NEGOCIOS/$negocioId/$NOMBRE_LOGO"

    /** ¿El valor es una URL remota (no una ruta local)? */
    fun esUrlLogo(valor: String?): Boolean =
        !valor.isNullOrBlank() &&
            (valor.startsWith("https://") || valor.startsWith("http://"))

    /**
     * Extrae el negocioId de la URL de descarga (negocios/{negocioId}/logo.jpg).
     * Devuelve vacío si no se puede reconocer.
     */
    fun negocioIdDeUrl(url: String): String = try {
        val encoded = url.substringAfter("/o/", "").substringBefore("?")
        val ruta = URLDecoder.decode(encoded, "UTF-8")
        ruta.removePrefix("$RUTA_RAIZ_NEGOCIOS/").substringBefore("/$NOMBRE_LOGO")
    } catch (e: Exception) {
        Log.w("LogoNegocioCache", "No se pudo interpretar la URL del logo", e)
        ""
    }

    /**
     * Devuelve el fichero local con el logo (descargándolo con el SDK si hace
     * falta). null si la URL no es remota, no se puede descargar o no hay caché.
     */
    suspend fun obtener(context: Context, url: String): File? {
        if (!esUrlLogo(url)) return null
        val negocioId = negocioIdDeUrl(url)
        if (negocioId.isBlank()) return null

        val directorio = File(context.filesDir, "logos_negocio").apply { mkdirs() }
        val nombre = "logo_${Math.abs(url.hashCode())}.jpg"
        val objetivo = File(directorio, nombre)
        if (objetivo.isFile && objetivo.length() > 0L) return objetivo

        val temporal = File(directorio, ".tmp_$nombre")
        return try {
            val storage = FirebaseStorage.getInstance()
            storage.getReference(rutaLogo(negocioId))
                .getFile(temporal)
                .esperar()
            if (!temporal.isFile || temporal.length() <= 0L) {
                temporal.delete()
                objetivo.takeIf { it.isFile && it.length() > 0L }
            } else {
                if (!objetivo.exists()) {
                    temporal.renameTo(objetivo)
                } else {
                    temporal.delete()
                }
                objetivo.takeIf { it.isFile && it.length() > 0L }
            }
        } catch (e: Exception) {
            Log.w("LogoNegocioCache", "No se pudo descargar el logo $negocioId", e)
            temporal.delete()
            objetivo.takeIf { it.isFile && it.length() > 0L }
        }
    }
}
