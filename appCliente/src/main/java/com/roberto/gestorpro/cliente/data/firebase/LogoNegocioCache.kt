package com.roberto.gestorpro.cliente.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.net.URLDecoder

/**
 * LogoNegocioCache
 * ----------------
 * Lectura AUTENTICADA del logo del negocio (negocios/{negocioId}/logo.jpg) con
 * caché local determinista por URL (mismo patrón autenticado que las fotos de
 * perfil). El CLIENTE descarga con el SDK (nunca GET HTTP anónimo a la URL).
 */
object LogoNegocioCache {

    private const val RUTA_RAIZ_NEGOCIOS = "negocios"
    private const val NOMBRE_LOGO = "logo.jpg"

    fun rutaLogo(negocioId: String): String =
        "$RUTA_RAIZ_NEGOCIOS/$negocioId/$NOMBRE_LOGO"

    fun esUrlLogo(valor: String?): Boolean =
        !valor.isNullOrBlank() &&
            (valor.startsWith("https://") || valor.startsWith("http://"))

    fun negocioIdDeUrl(url: String): String = try {
        val encoded = url.substringAfter("/o/", "").substringBefore("?")
        val ruta = URLDecoder.decode(encoded, "UTF-8")
        ruta.removePrefix("$RUTA_RAIZ_NEGOCIOS/").substringBefore("/$NOMBRE_LOGO")
    } catch (e: Exception) {
        Log.w("LogoNegocioCache", "No se pudo interpretar la URL del logo", e)
        ""
    }

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
