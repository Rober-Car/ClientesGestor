package com.roberto.gestorpro.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Funciones y constantes auxiliares para el manejo de fotos en la app.
 * 
 * Contiene la lógica de guardado de imágenes en el almacenamiento interno
 * para que quede independiente del permiso temporal de lectura de URIs.
 */
internal const val MAX_FOTO_DIMENSION = 1024
internal const val FOTO_CALIDAD = 85

/**
 * Nombre de la carpeta dentro del directorio de caché donde se crean los
 * archivos temporales que la app de cámara rellena al hacer una foto.
 */
internal const val CARPETA_FOTOS_TEMPORALES = "fotos_camara"

/**
 * Crear un archivo temporal vacío para que la app de cámara escriba la foto.
 *
 * @param context Contexto de la actividad
 * @return El archivo temporal creado, o null si no se pudo crear
 */
internal fun crearFotoTemporal(context: Context): File? {
    val dir = File(context.cacheDir, CARPETA_FOTOS_TEMPORALES).apply { mkdirs() }
    return try {
        File.createTempFile("foto_camara_", ".jpg", dir)
    } catch (e: Exception) {
        null
    }
}

/**
 * Convierte el archivo temporal de cámara en un Uri accesible para la app
 * de cámara mediante FileProvider.
 *
 * @param context Contexto de la actividad
 * @param archivo Archivo temporal de la foto
 * @return Uri de contenido listo para lanzar TakePicture
 */
internal fun uriDeFotoTemporal(context: Context, archivo: File): Uri {
    return FileProvider.getUriForFile(
        context,
        context.packageName + ".fileprovider",
        archivo
    )
}

/**
 * Procesa la foto tomada con la cámara: la copia al almacenamiento interno
 * mediante [guardaFotoEnInterna] y borra el archivo temporal.
 *
 * @param context Contexto de la actividad
 * @param archivoTemporal Archivo temporal donde la cámara escribió la foto
 * @return Ruta absoluta de la foto guardada, o null si falla
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
 * ✔ TIPO: función interna (internal fun) → String?
 * Es la función que copia la imagen elegida en la galería o tomada con la cámara
 * al almacenamiento interno de la app.
 * Sirve para que la foto no dependa del permiso temporal de lectura del URI,
 * comprimiéndola a JPEG y devolviendo la ruta absoluta del archivo guardado.
 * 
 * @param context Contexto de la actividad
 * @param uri URI de la imagen (puede venir de galería o cámara)
 * @return Ruta absoluta del archivo guardado, o null si falla
 */
internal fun guardaFotoEnInterna(context: Context, uri: Uri): String? {
    return try {

        /**
         * bounds
         * ------
         * ✔ TIPO: variable (val) → BitmapFactory.Options
         * Es la configuración que solo lee las dimensiones de la imagen sin cargarla completa.
         * Sirve para conocer el tamaño real de la foto y decidir cuánto reducirla después.
         */
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        }

        /**
         * sample
         * ------
         * ✔ TIPO: variable (var) → Int
         * Es el factor de reducción que se aplica al decodificar la imagen.
         * Sirve para que el sistema no cargue en memoria una foto gigante antes de redimensionarla.
         */
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= MAX_FOTO_DIMENSION ||
            bounds.outHeight / (sample * 2) >= MAX_FOTO_DIMENSION
        ) {
            sample *= 2
        }

        /**
         * bitmap
         * ------
         * ✔ TIPO: variable (val) → Bitmap
         * Es la imagen ya decodificada desde el URI con el tamaño reducido.
         * Sirve para redimensionarla a la dimensión máxima y comprimirla después.
         */
        val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null

        /**
         * escala / redimensionada
         * -----------------------
         * ✔ TIPO: variables (val) → Float / Bitmap
         * Son el factor de escala y la imagen ajustada a la dimensión máxima permitida.
         * Sirve para limitar el peso final de la foto y que ocupe poco en el dispositivo.
         */
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

        /**
         * dir / archivo
         * -------------
         * ✔ TIPO: variables (val) → File
         * Son la carpeta de fotos del almacenamiento interno y el archivo JPEG final.
         * Sirven para guardar la foto de forma permanente con un nombre único por cliente.
         */
        val dir = File(context.filesDir, "fotos").apply { mkdirs() }
        val archivo = File(dir, "foto_${System.currentTimeMillis()}.jpg")

        FileOutputStream(archivo).use { output ->
            redimensionada.compress(Bitmap.CompressFormat.JPEG, FOTO_CALIDAD, output)
        }

        if (redimensionada != bitmap) {
            bitmap.recycle()
        }
        redimensionada.recycle()

        archivo.absolutePath
    } catch (e: Exception) {
        null
    }
}

/**
 * Tamaño máximo (bytes) permitido para una foto de cliente en Cloud Storage
 * (5 MB), coherente con storage.rules.
 */
const val MAX_FOTO_STORAGE_BYTES = 5 * 1024 * 1024

/**
 * rutaFotoClienteEnStorage
 * ------------------------
 * Ruta estable de la foto de un cliente en Cloud Storage (migración futura
 * desde el almacenamiento local). Estructura: clientes/{clienteId}/foto.jpg.
 * La ruta es fija por cliente: al sustituir la foto se sobrescribe el mismo
 * objeto y se actualiza la referencia en Firestore.
 *
 * NOTA: mientras el proyecto no tenga Storage habilitado (Blaze + bucket),
 * la app sigue guardando y mostrando las fotos desde el almacenamiento local
 * (campo `foto` = ruta de archivo absoluta). Este helper solo fija la
 * convención de la migración futura.
 */
fun rutaFotoClienteEnStorage(clienteId: Int): String = "clientes/$clienteId/foto.jpg"