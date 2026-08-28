package com.roberto.gestorpro.cliente.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.roberto.gestorpro.cliente.model.Cliente
import com.roberto.gestorpro.cliente.model.EstadoCliente
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ClienteRepository
 * -----------------
 * ✔ TIPO: clase @Singleton inyectada por Hilt
 * Lee y actualiza la ficha del CLIENTE autenticado en clientes/{idCliente}.
 * La edición solo permite los campos personales (nombre, apellidos, telefono,
 * email, foto, fechaNacimiento); las Rules bloquean el resto.
 */
@Singleton
class ClienteRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    companion object {
        private const val COLECCION_CLIENTES = "clientes"
    }

    /**
     * leerFicha
     * ---------
     * Lee clientes/{idCliente} y la convierte en el modelo Cliente.
     */
    suspend fun leerFicha(idCliente: Int): Cliente? {
        return try {
            val documento = db.collection(COLECCION_CLIENTES)
                .document(idCliente.toString())
                .get()
                .esperar()
            if (!documento.exists()) return null
            val datos = documento.data ?: return null

            Cliente(
                idCliente = datos["idCliente"]?.let { (it as? Number)?.toInt() } ?: idCliente,
                negocioId = datos["negocioId"] as? String ?: "",
                firebaseUid = datos["firebaseUid"] as? String,
                nombre = datos["nombre"] as? String ?: "",
                apellidos = datos["apellidos"] as? String ?: "",
                dni = datos["dni"] as? String ?: "",
                telefono = datos["telefono"] as? String ?: "",
                email = datos["email"] as? String,
                foto = datos["foto"] as? String ?: "",
                fechaNacimiento = (datos["fechaNacimiento"] as? Number)?.toLong() ?: 0L,
                fechaRegistro = (datos["fechaRegistro"] as? Number)?.toLong() ?: 0L,
                fechaAlta = (datos["fechaAlta"] as? Number)?.toLong(),
                fechaBaja = (datos["fechaBaja"] as? Number)?.toLong(),
                estado = when (datos["estado"]) {
                    "ACTIVO" -> EstadoCliente.ACTIVO
                    "BAJA" -> EstadoCliente.BAJA
                    "ARCHIVADO" -> EstadoCliente.ARCHIVADO
                    "REGISTRADO" -> EstadoCliente.REGISTRADO
                    else -> EstadoCliente.REGISTRADO
                },
                tieneLlave = datos["tieneLlave"] as? Boolean ?: false,
                serviciosContratados = (datos["serviciosContratados"] as? List<*>)?.mapNotNull {
                    it as? String
                } ?: emptyList(),
                fechaInicioActual = (datos["fechaInicioActual"] as? Number)?.toLong(),
                fechaFinActual = (datos["fechaFinActual"] as? Number)?.toLong()
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * actualizarDatosPersonales
     * -------------------------
     * Actualiza solo los campos personales de la propia ficha.
     */
    suspend fun actualizarDatosPersonales(
        idCliente: Int,
        nombre: String,
        apellidos: String,
        telefono: String,
        email: String?,
        foto: String,
        fechaNacimiento: Long
    ): ResultadoAutenticacion {
        return try {
            db.collection(COLECCION_CLIENTES)
                .document(idCliente.toString())
                .update(
                    mapOf(
                        "nombre" to nombre,
                        "apellidos" to apellidos,
                        "telefono" to telefono,
                        "email" to email,
                        "foto" to foto,
                        "fechaNacimiento" to fechaNacimiento
                    )
                )
                .esperar()
            ResultadoAutenticacion(true, "Datos actualizados")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    private fun mensajeDe(e: Exception): String {
        return when {
            e.message?.contains("permission", ignoreCase = true) == true ->
                "No tienes permisos para esta operación"
            else -> e.message ?: "Error inesperado. Inténtalo de nuevo"
        }
    }
}
