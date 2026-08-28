package com.roberto.gestorpro.cliente.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * ResultadoVinculacion
 * --------------------
 * ✔ TIPO: data class
 * Resultado del intento de vinculación: ficha encontrada y vinculada, ficha
 * creada, DNI ya vinculado, o error.
 */
data class ResultadoVinculacion(
    val exito: Boolean,
    val mensaje: String,
    val clienteId: Int? = null,
    val negocioId: String? = null
)

/**
 * VinculacionRepository
 * ---------------------
 * ✔ TIPO: clase @Singleton inyectada por Hilt
 * Implementa las DOS vías de alta del CLIENTE:
 *   VÍA 1 — el ADMIN creó la ficha: localizar por indices_clientes/{negocio}_{dni}
 *           y vincular el UID a la ficha existente (sin crear otra).
 *   VÍA 2 — el CLIENTE se registró primero: crear la ficha con los datos de
 *           perfiles_pendientes + su índice, siempre dentro de la misma Transaction.
 * La unicidad negocio+DNI está garantizada por el documentId del índice.
 */
@Singleton
class VinculacionRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val perfilPendienteRepository: PerfilPendienteRepository
) {

    companion object {
        private const val COLECCION_CLIENTES = "clientes"
        private const val COLECCION_INDICES = "indices_clientes"
        private const val COLECCION_USUARIOS = "usuarios"

        private const val MAX_INTENTOS_ID = 5
        private const val ID_CLIENTE_MINIMO = 1_000_000_000
    }

    /**
     * indiceId
     * --------
     * DocumentId del índice negocio+DNI.
     */
    fun indiceId(negocioId: String, dni: String): String =
        "${negocioId}_${dni.trim().uppercase()}"

    /**
     * localizarFicha
     * --------------
     * Devuelve el clienteId del índice negocio+DNI, o null si no existe.
     */
    suspend fun localizarFicha(negocioId: String, dni: String): Int? {
        return try {
            val documento = db.collection(COLECCION_INDICES)
                .document(indiceId(negocioId, dni))
                .get()
                .esperar()
            documento.getLong("clienteId")?.toInt()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * vincularConCodigoYDNI
     * ---------------------
     * Flujo principal de vinculación. Resuelve el negocio por código maestro,
     * declara temporalmente { dni, negocioId } en perfiles_pendientes/{uid}
     * (VÍA 1) para poder consultar el índice de forma segura y, en una
     * Transaction:
     *   - si la ficha existe y está libre (firebaseUid == null): la vincula (VÍA 1);
     *   - si la ficha ya está vinculada: rechaza;
     *   - si la ficha no existe: la crea con los datos del perfil pendiente (VÍA 2).
     * En todos los casos el perfil pendiente se borra al terminar.
     */
    suspend fun vincularConCodigoYDNI(
        codigoMaestro: String,
        dni: String,
        perfil: PerfilPendiente?
    ): ResultadoVinculacion {
        val usuario = auth.currentUser
            ?: return ResultadoVinculacion(false, "No hay ningún usuario autenticado")
        val uid = usuario.uid

        val negocio = db.collection("negocios_publicos")
            .whereEqualTo("codigoMaestro", codigoMaestro.trim())
            .limit(1)
            .get()
            .esperar()
        val negocioId = negocio.documents.firstOrNull()?.id
            ?: return ResultadoVinculacion(false, "No existe ningún negocio con ese código maestro")

        val dniNorm = dni.trim().uppercase()

        // Declaración temporal de VÍA 1: permite a las Rules validar que el
        // índice consultado es exactamente { negocioId, dni } del propio uid.
        val declaracion = perfilPendienteRepository.guardarDeclaracion(uid, dniNorm, negocioId)
        if (!declaracion.exito) {
            return ResultadoVinculacion(false, declaracion.mensaje)
        }

        // Primero localizamos la ficha por el índice (VÍA 1).
        val clienteId = localizarFicha(negocioId, dniNorm)
        if (clienteId != null) {
            val resultado = vincularFichaExistente(uid, clienteId, negocioId, dniNorm)
            perfilPendienteRepository.borrar(uid)
            return resultado
        }

        // No existe ficha: VÍA 2, requiere perfil pendiente con sus datos.
        if (perfil == null || perfil.dni.isBlank()) {
            perfilPendienteRepository.borrar(uid)
            return ResultadoVinculacion(
                false,
                "No existe una ficha creada por tu gimnasio. Completa tu perfil para registrarte"
            )
        }
        if (perfil.dni.trim().uppercase() != dniNorm) {
            perfilPendienteRepository.borrar(uid)
            return ResultadoVinculacion(
                false,
                "El DNI del perfil no coincide con el introducido"
            )
        }

        val resultadoCrear = crearFicha(uid, negocioId, dniNorm, perfil)
        perfilPendienteRepository.borrar(uid)
        return resultadoCrear
    }

    /**
     * vincularFichaExistente
     * ----------------------
     * VÍA 1: Transaction que escribe el UID en la ficha libre y actualiza
     * usuarios/{uid}. Si la ficha ya tiene UID, las Rules deniegan la escritura
     * y la Transaction falla.
     */
    private suspend fun vincularFichaExistente(
        uid: String,
        clienteId: Int,
        negocioId: String,
        dni: String
    ): ResultadoVinculacion {
        return try {
            val clienteRef = db.collection(COLECCION_CLIENTES).document(clienteId.toString())
            val usuarioRef = db.collection(COLECCION_USUARIOS).document(uid)

            db.runTransaction { transaction ->
                val ficha = transaction.get(clienteRef)
                if (!ficha.exists()) {
                    // El índice quedó huérfano: se ignora y se trata como VÍA 2.
                    throw FichaInexistenteException()
                }
                if (ficha.getString("firebaseUid") != null) {
                    throw DniYaVinculadoException()
                }
                transaction.update(clienteRef, mapOf("firebaseUid" to uid))
                transaction.update(
                    usuarioRef,
                    mapOf(
                        "clienteId" to clienteId,
                        "negocioId" to negocioId
                    )
                )
            }.esperar()

            ResultadoVinculacion(
                true,
                "Te has vinculado a la ficha de tu gimnasio",
                clienteId,
                negocioId
            )
        } catch (e: DniYaVinculadoException) {
            ResultadoVinculacion(false, "Ese DNI ya está vinculado a otra cuenta")
        } catch (e: FichaInexistenteException) {
            ResultadoVinculacion(false, "La ficha ya no existe. Inténtalo de nuevo")
        } catch (e: Exception) {
            ResultadoVinculacion(false, mensajeDe(e))
        }
    }

    /**
     * crearFicha
     * ----------
     * VÍA 2: Transaction que crea la ficha con los datos del perfil pendiente,
     * crea el índice negocio+DNI y actualiza usuarios/{uid}. La unicidad la
     * garantiza el documentId del índice: si otro cliente creó la ficha antes,
     * el set del índice colisiona y la Transaction se aborta.
     */
    private suspend fun crearFicha(
        uid: String,
        negocioId: String,
        dni: String,
        perfil: PerfilPendiente
    ): ResultadoVinculacion {
        val usuarioRef = db.collection(COLECCION_USUARIOS).document(uid)

        return try {
            repeat(MAX_INTENTOS_ID) { intento ->
                val idCliente = generarIdCliente()
                val fichaRef = db.collection(COLECCION_CLIENTES)
                    .document(idCliente.toString())
                val indiceRef = db.collection(COLECCION_INDICES)
                    .document(indiceId(negocioId, dni))

                try {
                    db.runTransaction { transaction ->
                        if (transaction.get(indiceRef).exists()) {
                            throw DniYaVinculadoException()
                        }
                        if (transaction.get(fichaRef).exists()) {
                            throw ColisionIdClienteException()
                        }
                        transaction.set(
                            fichaRef,
                            mapOf(
                                "idCliente" to idCliente,
                                "negocioId" to negocioId,
                                "firebaseUid" to uid,
                                "nombre" to perfil.nombre,
                                "apellidos" to perfil.apellidos,
                                "dni" to dni,
                                "telefono" to perfil.telefono,
                                "email" to perfil.email,
                                "foto" to perfil.foto,
                                "fechaNacimiento" to perfil.fechaNacimiento,
                                "fechaRegistro" to com.google.firebase.Timestamp.now(),
                                "fechaAlta" to null,
                                "fechaBaja" to null,
                                "estado" to "REGISTRADO",
                                "tieneLlave" to false,
                                "serviciosContratados" to emptyList<String>(),
                                "fechaInicioActual" to null,
                                "fechaFinActual" to null
                            )
                        )
                        transaction.set(
                            indiceRef,
                            mapOf(
                                "negocioId" to negocioId,
                                "dni" to dni,
                                "clienteId" to idCliente
                            )
                        )
                        transaction.update(
                            usuarioRef,
                            mapOf(
                                "clienteId" to idCliente,
                                "negocioId" to negocioId
                            )
                        )
                    }.esperar()

                    return ResultadoVinculacion(
                        true,
                        "Te has registrado en tu gimnasio",
                        idCliente,
                        negocioId
                    )
                } catch (e: DniYaVinculadoException) {
                    return ResultadoVinculacion(
                        false,
                        "Ese DNI ya está vinculado a otra cuenta"
                    )
                } catch (e: ColisionIdClienteException) {
                    if (intento == MAX_INTENTOS_ID - 1) {
                        return ResultadoVinculacion(
                            false,
                            "No se pudo generar un identificador único. Inténtalo de nuevo"
                        )
                    }
                }
            }
            ResultadoVinculacion(false, "No se pudo crear la ficha. Inténtalo de nuevo")
        } catch (e: Exception) {
            ResultadoVinculacion(false, mensajeDe(e))
        }
    }

    private fun generarIdCliente(): Int {
        return Random.nextInt(ID_CLIENTE_MINIMO, Int.MAX_VALUE)
    }

    private class DniYaVinculadoException : Exception()
    private class FichaInexistenteException : Exception()
    private class ColisionIdClienteException : Exception()

    private fun mensajeDe(e: Exception): String {
        return when {
            e.message?.contains("permission", ignoreCase = true) == true ->
                "No tienes permisos para esta operación. Revisa el código y el DNI"
            else -> e.message ?: "Error inesperado. Inténtalo de nuevo"
        }
    }
}
