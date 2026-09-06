package com.roberto.gestorpro.data.firebase

import com.google.firebase.storage.FirebaseStorage
import java.io.File

/**
 * FotoClienteStorage
 * ------------------
 * Sube/borra la foto de perfil de un cliente en Firebase Storage en la ruta
 * definitiva `clientes/{clienteId}/foto.jpg` (el objeto se SOBRESCRIBE al
 * cambiar; no se acumulan versiones).
 *
 * Compresión: la app ya genera el fichero local comprimido/redimensionado
 * (guardaFotoEnInterna) ANTES de llamar aquí, por lo que solo se sube ese
 * fichero. La regla de Storage valida independientemente que sea imagen y que
 * no supere los 10 MB.
 *
 * Cache-busting: la URL devuelta incorpora `?rev=<epoch>` para que Coil
 * re-descargue la foto cuando el mismo objeto se reemplaza.
 *
 * Es un object STATELESS (no se inyecta por Hilt): cada llamada recibe la
 * instancia de FirebaseStorage.
 */
object FotoClienteStorage {

    const val RUTA_RAIZ_CLIENTES = "clientes"
    const val NOMBRE_FOTO = "foto.jpg"

    /** ¿El valor almacenado en `foto` es una URL remota (no una ruta local)? */
    fun esUrlFoto(valor: String?): Boolean =
        !valor.isNullOrBlank() &&
            (valor.startsWith("https://") || valor.startsWith("http://"))

    fun rutaCliente(clienteId: Int): String =
        "$RUTA_RAIZ_CLIENTES/$clienteId/$NOMBRE_FOTO"

    fun conRevision(url: String, ahora: Long = System.currentTimeMillis()): String {
        val separador = if (url.contains("?")) "&" else "?"
        return "$url${separador}rev=$ahora"
    }

    /**
     * Sube la foto local a `clientes/{clienteId}/foto.jpg` y devuelve la URL de
     * descarga con cache-busting. Si el archivo no existe o el valor ya es una
     * URL, devuelve null sin subir. Un fallo devuelve null (no lanza).
     *
     * La subida NO adjunta metadata: la autorización la concede la Storage Rule
     * normal (esAdminDelCliente/esDuenioDeLaFoto) porque en el alta doc-first la
     * ficha clientes/{clienteId} YA existe antes de subir. Si la subida se
     * completa pero no se consigue la URL, se elimina el objeto recién subido
     * para no dejar un archivo huérfano.
     */
    suspend fun subirFotoCliente(
        storage: FirebaseStorage,
        clienteId: Int,
        rutaLocal: String?
    ): String? {
        if (esUrlFoto(rutaLocal)) return null
        if (rutaLocal.isNullOrBlank()) return null
        val archivo = File(rutaLocal)
        if (!archivo.exists()) return null
        return try {
            val referencia = storage.reference.child(rutaCliente(clienteId))
            referencia.putFile(android.net.Uri.fromFile(archivo)).esperar()
            try {
                conRevision(referencia.downloadUrl.esperar().toString())
            } catch (e: Exception) {
                // La subida fue correcta pero no se pudo obtener la URL: se
                // elimina el objeto recién subido para no dejar un huérfano.
                try {
                    referencia.delete().esperar()
                } catch (eLimpiar: Exception) {
                    android.util.Log.w(
                        "FotoClienteStorage",
                        "No se pudo limpiar la foto subida sin URL del cliente $clienteId",
                        eLimpiar
                    )
                }
                throw e
            }
        } catch (e: Exception) {
            android.util.Log.e(
                "FotoClienteStorage",
                "No se pudo subir la foto del cliente $clienteId",
                e
            )
            null
        }
    }

    /**
     * Elimina `clientes/{clienteId}/foto.jpg` (si no existe es un no-op).
     */
    suspend fun eliminarFotoCliente(storage: FirebaseStorage, clienteId: Int) {
        try {
            storage.reference.child(rutaCliente(clienteId)).delete().esperar()
        } catch (e: Exception) {
            android.util.Log.w(
                "FotoClienteStorage",
                "No se pudo eliminar la foto remota del cliente $clienteId",
                e
            )
        }
    }
}
