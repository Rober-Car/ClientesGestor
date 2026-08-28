package com.roberto.gestorpro.cliente.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

private const val CARPETA_FOTOS_TEMPORALES = "fotos_camara"

/**
 * FotoUtils
 * ---------
 * Utilidades de fotos de GestorPro Cliente: guarda la foto elegida de la
 * galería o tomada con la cámara en el almacenamiento interno de la app,
 * redimensionándola para no ocupar espacio excesivo.
 */
object FotoUtils {

    private const val MAX_FOTO_DIMENSION = 1024
    private const val FOTO_CALIDAD = 85

    /**
     * guardaFotoEnInterna
     * -------------------
     * Copia y redimensiona el contenido de la Uri a un archivo en
     * filesDir/fotos_perfil y devuelve la ruta absoluta (o null si falla).
     */
    fun guardaFotoEnInterna(context: Context, uri: Uri): String? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, bounds)
            }

            var sample = 1
            while (bounds.outWidth / (sample * 2) >= MAX_FOTO_DIMENSION ||
                bounds.outHeight / (sample * 2) >= MAX_FOTO_DIMENSION
            ) {
                sample *= 2
            }

            val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(
                    input,
                    null,
                    BitmapFactory.Options().apply { inSampleSize = sample }
                )
            } ?: return null

            val escala = minOf(
                1f,
                MAX_FOTO_DIMENSION / maxOf(bounds.outWidth, bounds.outHeight).toFloat()
            )
            val ancho = (bitmap.width * escala).toInt()
            val alto = (bitmap.height * escala).toInt()
            val redimensionada = if (escala < 1f) {
                Bitmap.createScaledBitmap(bitmap, ancho, alto, true)
            } else {
                bitmap
            }

            val dir = File(context.filesDir, "fotos_perfil").apply { mkdirs() }
            val archivo = File(dir, "perfil_${System.currentTimeMillis()}.jpg")

            FileOutputStream(archivo).use { output ->
                redimensionada.compress(Bitmap.CompressFormat.JPEG, FOTO_CALIDAD, output)
            }

            if (redimensionada != bitmap) {
                bitmap.recycle()
            }
            redimensionada.recycle()

            archivo.absolutePath
        } catch (_: Exception) {
            null
        }
    }
}

/**
 * Crea un archivo temporal en caché para que la app de cámara escriba la foto.
 */
internal fun crearFotoTemporal(context: Context): File? {
    val dir = File(context.cacheDir, CARPETA_FOTOS_TEMPORALES).apply { mkdirs() }
    return try {
        File.createTempFile("foto_camara_", ".jpg", dir)
    } catch (_: Exception) {
        null
    }
}

/**
 * Construye la Uri de contenido (FileProvider) de la foto temporal de cámara.
 */
internal fun uriDeFotoTemporal(context: Context, archivo: File): Uri {
    return FileProvider.getUriForFile(
        context,
        context.packageName + ".fileprovider",
        archivo
    )
}

/**
 * Procesa la foto de cámara: la guarda en el almacenamiento interno y borra
 * el temporal. Devuelve la ruta guardada o null si falla.
 */
internal fun guardarFotoDeCamara(context: Context, archivoTemporal: File?): String? {
    val ruta = if (archivoTemporal != null && archivoTemporal.exists()) {
        guardaFotoEnInterna(context, uriDeFotoTemporal(context, archivoTemporal))
    } else {
        null
    }
    archivoTemporal?.delete()
    return ruta
}

/**
 * guardaFotoEnInterna
 * -------------------
 * Función de conveniencia de nivel superior que delega en FotoUtils.
 * Mantiene la misma firma que en GestorPro Admin para facilitar el portado.
 */
fun guardaFotoEnInterna(context: Context, uri: Uri): String? {
    return FotoUtils.guardaFotoEnInterna(context, uri)
}
