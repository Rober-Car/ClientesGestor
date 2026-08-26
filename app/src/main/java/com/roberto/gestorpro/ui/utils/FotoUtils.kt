package com.roberto.gestorpro.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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