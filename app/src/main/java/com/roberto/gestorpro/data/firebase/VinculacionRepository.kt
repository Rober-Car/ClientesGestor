package com.roberto.gestorpro.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class VinculacionRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun vincularCliente(
        codigo: String,
        onResultado: (Boolean, String) -> Unit
    ) {

        // Comprobamos que hay un usuario autenticado.
        val usuario = auth.currentUser

        if (usuario == null) {
            onResultado(false, "No hay ningún usuario autenticado")
            return
        }

        val uid = usuario.uid

        // Documento de la vinculación.
        val vinculacionRef = db
            .collection("vinculaciones")
            .document(codigo)

        // Primero buscamos el código introducido.
        vinculacionRef.get()
            .addOnSuccessListener { documento ->

                // El código no existe.
                if (!documento.exists()) {
                    onResultado(false, "El código no existe")
                    return@addOnSuccessListener
                }

                val estado = documento.getString("estado")
                val clienteId = documento.getLong("clienteId")?.toInt()
                val negocioId = documento.getString("negocioId")
                val fechaExpiracion = documento.getTimestamp("fechaExpiracion")

                // El código ya ha sido utilizado.
                if (estado != "PENDIENTE") {
                    onResultado(false, "El código ya no está disponible")
                    return@addOnSuccessListener
                }

                // Comprobamos que el código tiene cliente asignado.
                if (clienteId == null) {
                    onResultado(false, "El código no está asociado a un cliente")
                    return@addOnSuccessListener
                }

                // Comprobamos que tiene negocio asociado.
                if (negocioId == null) {
                    onResultado(false, "El código no tiene un negocio asociado")
                    return@addOnSuccessListener
                }

                // Comprobamos que el código no ha caducado.
                if (
                    fechaExpiracion == null ||
                    fechaExpiracion.toDate().time < System.currentTimeMillis()
                ) {
                    onResultado(false, "El código ha caducado")
                    return@addOnSuccessListener
                }

                // Referencias de los documentos que vamos a modificar.
                val usuarioRef = db
                    .collection("usuarios")
                    .document(uid)

                val clienteRef = db
                    .collection("clientes")
                    .document(clienteId.toString())

                // Creamos un WriteBatch.
                val batch = db.batch()

                // Actualizamos el usuario Firebase.
                batch.update(
                    usuarioRef,
                    mapOf(
                        "clienteId" to clienteId,
                        "negocioId" to negocioId
                    )
                )

                // Vinculamos la ficha del cliente con Firebase UID.
                batch.update(
                    clienteRef,
                    "firebaseUid",
                    uid
                )

                // Marcamos el código como utilizado.
                batch.update(
                    vinculacionRef,
                    "estado",
                    "USADA"
                )

                // Ejecutamos las tres escrituras juntas.
                batch.commit()
                    .addOnSuccessListener {
                        onResultado(
                            true,
                            "Cliente vinculado correctamente"
                        )
                    }
                    .addOnFailureListener { error ->
                        onResultado(
                            false,
                            error.message ?: "Error al realizar la vinculación"
                        )
                    }
            }
            .addOnFailureListener { error ->

                onResultado(
                    false,
                    error.message ?: "Error al consultar el código"
                )
            }
    }
}
