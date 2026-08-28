package com.roberto.gestorpro.cliente.ui.utils

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * FotoUtils
 * ---------
 * Utilidades de fotos de GestorPro Cliente: guarda la foto elegida de la
 * galería en el almacenamiento interno de la app.
 */
object FotoUtils {

    /**
     * guardaFotoEnInterna
     * -------------------
     * Copia el contenido de la Uri a un archivo en filesDir/fotos_perfil y
     * devuelve la ruta absoluta (o null si falla).
     */
    fun guardaFotoEnInterna(context: Context, uri: Uri): String? {
        return try {
            val dir = File(context.filesDir, "fotos_perfil").apply { mkdirs() }
            val destino = File(dir, "perfil_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                destino.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            destino.absolutePath
        } catch (_: Exception) {
            null
        }
    }
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
