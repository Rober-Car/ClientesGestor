package com.roberto.gestorpro.cliente.data.firebase

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
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
 * ResultadoIndice
 * ---------------
 * ✔ TIPO: sealed class
 * Resultado de la consulta a indices_clientes/{negocioId}_{dni}:
 *   - Ficha(clienteId): el índice existe y apunta a una ficha.
 *   - NoExiste: el índice no existe (el gimnasio aún no creó una ficha para ese DNI).
 * Los errores (permisos, red) NO se devuelven aquí: se lanzan como excepción para
 * que el llamador los distinga de un índice inexistente.
 */
sealed class ResultadoIndice {
    data class Ficha(val clienteId: Int) : ResultadoIndice()
    object NoExiste : ResultadoIndice()
}

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
        private const val COLECCION_PERFILES_PENDIENTES = "perfiles_pendientes"
        private const val COLECCION_NEGOCIOS_PUBLICOS = "negocios_publicos"

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
     * Devuelve el estado del índice negocio+DNI.
     *   - ResultadoIndice.Ficha(clienteId): el índice existe.
     *   - ResultadoIndice.NoExiste: el índice no existe.
     * NO traga las excepciones: un PERMISSION_DENIED o un error de red se
     * propagan al llamador para que no se confundan con "índice inexistente"
     * (un índice inexistente significa VÍA 2; un permiso denegado no).
     */
    suspend fun localizarFicha(negocioId: String, dni: String): ResultadoIndice {
        val documento = db.collection(COLECCION_INDICES)
            .document(indiceId(negocioId, dni))
            .get()
            .esperar()
        val clienteId = documento.getLong("clienteId")?.toInt()
        return if (clienteId != null) {
            ResultadoIndice.Ficha(clienteId)
        } else {
            ResultadoIndice.NoExiste
        }
    }

    /**
     * vincularConCodigoYDNI
     * ---------------------
     * Flujo principal de vinculación. Resuelve el negocio por código maestro,
     * declara temporalmente { dni, negocioId } en perfiles_pendientes/{uid}
     * (sin destruir el perfil completo) y, según el índice:
     *   - si la ficha existe y está libre (firebaseUid == null): la vincula (VÍA 1);
     *   - si la ficha ya está vinculada: rechaza;
     *   - si el índice NO existe (VÍA 2): crea la ficha con los datos de
     *     perfiles_pendientes/{uid} (fuente de verdad) en una Transaction.
     * El perfil pendiente SOLO se elimina cuando la vinculación se completa con
     * éxito. Ante cualquier error (falta de perfil, permisos, red, fallo
     * intermedio) se conserva.
     */
    suspend fun vincularConCodigoYDNI(
        codigoMaestro: String,
        dni: String
    ): ResultadoVinculacion {
        return try {
            val usuario = auth.currentUser
                ?: return ResultadoVinculacion(false, "No hay ningún usuario autenticado")
            val uid = usuario.uid

            val negocio = db.collection(COLECCION_NEGOCIOS_PUBLICOS)
                .whereEqualTo("codigoMaestro", codigoMaestro.trim())
                .limit(1)
                .get()
                .esperar()
            val negocioId = negocio.documents.firstOrNull()?.id
                ?: return ResultadoVinculacion(false, "No existe ningún negocio con ese código maestro")

            val dniNorm = dni.trim().uppercase()

            // Declaración temporal de VÍA 1: permite a las Rules validar que el
            // índice consultado es exactamente { negocioId, dni } del propio uid.
            // Con merge no destruye el perfil completo guardado previamente.
            val declaracion = perfilPendienteRepository.guardarDeclaracion(uid, dniNorm, negocioId)
            if (!declaracion.exito) {
                return ResultadoVinculacion(false, declaracion.mensaje)
            }

            when (val indice = localizarFicha(negocioId, dniNorm)) {
                is ResultadoIndice.Ficha -> {
                    val resultado = vincularFichaExistente(
                        uid, indice.clienteId, negocioId, dniNorm
                    )
                    if (resultado.exito) {
                        perfilPendienteRepository.borrar(uid)
                    }
                    resultado
                }

                // VÍA 2: el índice no existe → el gimnasio aún no creó una ficha
                // para este DNI. Se crea con los datos de perfiles_pendientes/{uid}.
                ResultadoIndice.NoExiste -> {
                    val perfil = leerPerfilPendiente(uid) ?: return ResultadoVinculacion(
                        false,
                        "Completa primero tu perfil para registrarte"
                    )
                    if (perfil.dni.trim().uppercase() != dniNorm) {
                        return ResultadoVinculacion(
                            false,
                            "El DNI del perfil no coincide con el introducido"
                        )
                    }
                    val resultado = crearFicha(uid, negocioId, dniNorm, perfil)
                    if (resultado.exito) {
                        perfilPendienteRepository.borrar(uid)
                    }
                    resultado
                }
            }
        } catch (e: Exception) {
            ResultadoVinculacion(false, mensajeDe(e))
        }
    }

    /**
     * leerPerfilPendiente
     * -------------------
     * Lee el perfil pendiente completo de Firestore. A diferencia de
     * PerfilPendienteRepository.leer(), NO traga las excepciones: un error de
     * permisos o de red se propaga para que no se interprete como "no hay perfil".
     */
    private suspend fun leerPerfilPendiente(uid: String): PerfilPendiente? {
        val documento = db.collection(COLECCION_PERFILES_PENDIENTES)
            .document(uid)
            .get()
            .esperar()
        if (!documento.exists()) return null
        return PerfilPendiente(
            nombre = documento.getString("nombre") ?: "",
            apellidos = documento.getString("apellidos") ?: "",
            dni = documento.getString("dni") ?: "",
            telefono = documento.getString("telefono") ?: "",
            email = documento.getString("email"),
            foto = documento.getString("foto") ?: "",
            fechaNacimiento = documento.getLong("fechaNacimiento") ?: 0L
        )
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
                                "serviciosContratados" to emptyList<Int>(),
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
        return when (e) {
            is FirebaseFirestoreException -> when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    "No tienes permisos para esta operación. Revisa el código y el DNI"

                FirebaseFirestoreException.Code.UNAVAILABLE,
                FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
                    "No hay conexión con el servidor. Comprueba tu conexión a Internet"

                else -> e.message ?: "Error inesperado. Inténtalo de nuevo"
            }

            is FirebaseNetworkException ->
                "No hay conexión con el servidor. Comprueba tu conexión a Internet"

            else -> e.message ?: "Error inesperado. Inténtalo de nuevo"
        }
    }
}
