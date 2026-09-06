package com.roberto.gestorpro.cliente.data.firebase

import android.content.Context
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FotoClienteCache
 * ----------------
 * Lectura AUTENTICADA de la foto de perfil del cliente mediante Firebase
 * Storage SDK (no HTTP anónimo) con caché local determinista.
 *
 * Reglas:
 *  - La foto remota vive en `clientes/{clienteId}/foto.jpg` y `clientes/{id}.foto`
 *    guarda la URL con `?rev=` (revisión/cache-busting).
 *  - Caché local: `filesDir/fotos_clientes/foto_<clienteId>_<rev>.jpg`.
 *  - Solo se descarga si no existe un fichero definitivo para ese (cliente, rev).
 *  - La descarga va primero a un temporal y solo al terminar correctamente se
 *    renombra al definitivo (nunca queda un definitivo incompleto).
 *  - Al conseguir la nueva imagen se limpian las revisiones antiguas del mismo
 *    cliente. Un fichero antiguo nunca sobrescribe uno nuevo (el nombre incluye
 *    la revisión).
 *  - Ante un fallo se conserva la caché anterior (si existe) como fallback.
 *  - No gestiona URLs (subida) ni rutas locales legacy: esas las muestran las
 *    pantallas directamente con File.
 */
@Singleton
class FotoClienteCache @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: FirebaseStorage
) {

    private fun directorio(): File =
        File(context.filesDir, "fotos_clientes").apply { mkdirs() }

    /** Extrae el valor de `?rev=` / `&rev=` de la URL de descarga. */
    private fun revDe(url: String?): String {
        if (url == null) return "legacy"
        val indice = url.indexOf('?')
        if (indice < 0) return "legacy"
        val parametros = url.substring(indice + 1)
        for (parametro in parametros.split('&')) {
            if (parametro.startsWith("rev=") && parametro.length > 4) {
                val valor = parametro.substring(4)
                if (valor.isNotBlank()) return valor
            }
        }
        return "legacy"
    }

    /** Revisión sanitizada para usarla como parte del nombre de fichero. */
    private fun revLimpia(rev: String): String =
        rev.filter { it.isLetterOrDigit() || it == '.' || it == '_' || it == '-' }
            .ifBlank { "legacy" }

    private fun nombreFichero(clienteId: Int, rev: String): String =
        "foto_${clienteId}_${revLimpia(rev)}.jpg"

    /**
     * Devuelve el fichero local listo para Coil, descargándolo con el SDK
     * autenticado si hace falta. Devuelve null si la foto es local/legacy, si
     * no existe foto remota o si falla la descarga y no hay caché previa.
     */
    suspend fun obtener(clienteId: Int, foto: String?): File? {
        if (!FotoClienteStorage.esUrlFoto(foto)) return null

        val directorio = directorio()
        val rev = revLimpia(revDe(foto))
        val objetivo = File(directorio, nombreFichero(clienteId, rev))

        // Caché válida para esta revisión.
        if (objetivo.isFile && objetivo.length() > 0L) {
            return objetivo
        }

        val temporal = File(
            directorio,
            ".foto_${clienteId}_${System.currentTimeMillis()}.tmp"
        )
        return try {
            storage.getReference(FotoClienteStorage.rutaCliente(clienteId))
                .getFile(temporal)
                .esperar()
            if (!temporal.isFile || temporal.length() <= 0L) {
                temporal.delete()
                objetivo.takeIf { it.isFile && it.length() > 0L }
            } else {
                if (!objetivo.exists()) {
                    temporal.renameTo(objetivo)
                } else {
                    // Otro proceso ya dejó la misma revisión: se descarta el temporal.
                    temporal.delete()
                }
                limpiarRevisionesAntiguas(clienteId, objetivo.name)
                objetivo.takeIf { it.isFile && it.length() > 0L }
            }
        } catch (e: Exception) {
            android.util.Log.w(
                "FotoClienteCache",
                "No se pudo descargar la foto del cliente $clienteId",
                e
            )
            temporal.delete()
            // Fallback: si existía una caché anterior (otra revisión) se conserva.
            objetivo.takeIf { it.isFile && it.length() > 0L }
                ?: directorio.listFiles()
                    ?.filter { it.isFile && it.name.startsWith("foto_${clienteId}_") }
                    ?.maxByOrNull { it.lastModified() }
        }
    }

    /**
     * Elimina las revisiones antiguas de un cliente (solo después de haber
     * dejado correctamente la imagen nueva en el fichero objetivo).
     */
    private fun limpiarRevisionesAntiguas(clienteId: Int, nombreNuevo: String) {
        directorio().listFiles()?.forEach { fichero ->
            if (fichero.isFile &&
                fichero.name.startsWith("foto_${clienteId}_") &&
                fichero.name != nombreNuevo
            ) {
                fichero.delete()
            }
        }
    }
}
