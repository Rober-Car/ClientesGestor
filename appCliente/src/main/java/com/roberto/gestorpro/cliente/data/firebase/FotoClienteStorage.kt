package com.roberto.gestorpro.cliente.data.firebase

import com.google.firebase.storage.FirebaseStorage
import java.io.File

/**
 * FotoClienteStorage
 * ------------------
 * Sube/borra la foto de perfil del CLIENTE a Firebase Storage en la ruta
 * definitiva `clientes/{clienteId}/foto.jpg`. El objeto se SOBRESCRIBE al
 * cambiar (misma ruta) y la URL lleva cache-busting `?rev=<epoch>`.
 *
 * La app ya comprime/redimensiona la imagen en local (FotoUtils) antes de
 * llamar aquí. La regla de Storage valida imagen y <= 10 MB.
 *
 * Object STATELESS: recibe la instancia de FirebaseStorage (proveedor Hilt).
 */
object FotoClienteStorage {

    const val RUTA_RAIZ_CLIENTES = "clientes"
    const val NOMBRE_FOTO = "foto.jpg"

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
     * Sube el archivo local comprimido a `clientes/{clienteId}/foto.jpg` y
     * devuelve la URL con cache-busting. null si no hay archivo o si falla.
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
            conRevision(referencia.downloadUrl.esperar().toString())
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
     * Elimina `clientes/{clienteId}/foto.jpg` (no-op si no existe).
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
