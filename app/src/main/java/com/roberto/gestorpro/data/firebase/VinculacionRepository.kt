package com.roberto.gestorpro.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import java.security.SecureRandom

/**
 * EnlaceVinculacion
 * -----------------
 * ✔ TIPO: data class de Kotlin
 * Es el resultado de una operación de generación/regeneración de enlace.
 * Sirve para entregar a la UI el token activo y su fecha de expiración.
 */
data class EnlaceVinculacion(
    val exito: Boolean,
    val mensaje: String,
    val token: String? = null,
    val fechaExpiracion: Timestamp? = null
)

/**
 * ConsultaEnlace
 * --------------
 * ✔ TIPO: data class de Kotlin
 * Es el estado del enlace de vinculación de una ficha remota.
 * Sirve a la UI para mostrar si hay enlace activo y si está caducado.
 */
data class ConsultaEnlace(
    val token: String?,
    val caducado: Boolean
)

/**
 * ColisionIdClienteException
 * --------------------------
 * ✔ TIPO: clase de excepción de Kotlin
 * Es la señal interna que lanza la Transaction cuando el idCliente aleatorio
 * elegido ya existe en la colección clientes. Sirve para que el bucle de
 * reintento genere otro identificador sin comunicar error al usuario.
 */
private class ColisionIdClienteException : Exception()

/**
 * VinculacionRepository
 * ---------------------
 * ✔ TIPO: clase @Singleton inyectada por Hilt (data/firebase)
 * Es el repositorio que encapsula las dos vías de vinculación de un CLIENTE:
 * Vía A (código maestro del negocio → el cliente crea su propia ficha) y
 * Vía B (enlace individual generado por el ADMIN → el cliente reclama una
 * ficha existente). Sirve para que las pantallas no toquen Firestore
 * directamente y para que toda escritura sea atómica y respete las Rules.
 */
@Singleton
class VinculacionRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    companion object {
        private const val COLECCION_USUARIOS = "usuarios"
        private const val COLECCION_CLIENTES = "clientes"
        private const val COLECCION_NEGOCIOS_PUBLICOS = "negocios_publicos"
        private const val COLECCION_VINCULACIONES = "vinculaciones"

        private const val ESTADO_PENDIENTE = "PENDIENTE"
        private const val ESTADO_USADA = "USADA"

        /** Intentos máximos ante una colisión teórica de idCliente aleatorio. */
        private const val MAX_INTENTOS_ID = 5

        /**
         * Rango del idCliente aleatorio: enteros positivos altos que caben
         * siempre en un Int de Room (entre 1.000.000.000 e Int.MAX_VALUE).
         */
        private const val ID_CLIENTE_MINIMO = 1_000_000_000

        /** Caracteres del alfabeto para el token individual (sin ambiguos). */
        private const val ALFABETO_TOKEN =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789"

        /** Longitud mínima acordada para el token individual. */
        private const val LONGITUD_TOKEN = 24

        /** Días de validez de un enlace individual recién generado. */
        private const val DIAS_EXPIRACION_ENLACE = 7L
    }

    /**
     * secureRandom
     * ------------
     * ✔ TIPO: propiedad (private val) → SecureRandom
     * Es el generador criptográfico de números aleatorios.
     * Sirve para crear tokens de vinculación no predecibles.
     */
    private val secureRandom = SecureRandom()

    /**
     * estaVinculado
     * -------------
     * ✔ TIPO: método (fun) suspend de Kotlin → Boolean
     * Indica si el CLIENTE autenticado ya tiene ficha asignada (usuarios/{uid}
     * con clienteId distinto de null). Un CLIENTE solo puede vincularse una vez.
     * Sirve a VincularClienteScreen para bloquear re-vinculaciones.
     */
    suspend fun estaVinculado(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            db.collection(COLECCION_USUARIOS)
                .document(uid)
                .get()
                .esperar()
                .getLong("clienteId") != null
        } catch (_: Exception) {
            false
        }
    }

    /**
     * vincularConCodigoMaestro
     * ------------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → ResultadoAutenticacion (Vía A)
     * Busca el negocio por su código maestro en negocios_publicos, genera un
     * idCliente entero positivo aleatorio (Random.nextLong(1, Long.MAX_VALUE),
     * que nunca produce 0, negativos ni Long.MIN_VALUE, así que no hace falta
     * abs()) único en toda la colección clientes y ejecuta la Transaction:
     * set(clientes/{id}) + update(usuarios/{uid}). Sin vinculaciones/{codigo}.
     * Si el id elegido colisiona, reintenta con otro hasta MAX_INTENTOS_ID.
     */
    suspend fun vincularConCodigoMaestro(codigoMaestro: String): ResultadoAutenticacion {
        val usuario = auth.currentUser
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")
        val uid = usuario.uid

        return try {
            val coincidencias = db.collection(COLECCION_NEGOCIOS_PUBLICOS)
                .whereEqualTo("codigoMaestro", codigoMaestro)
                .limit(1)
                .get()
                .esperar()

            val negocioPublico = coincidencias.documents.firstOrNull()
                ?: return ResultadoAutenticacion(
                    false,
                    "No existe ningún negocio con ese código maestro"
                )

            val negocioId = negocioPublico.id
            val usuarioRef = db.collection(COLECCION_USUARIOS).document(uid)

            repeat(MAX_INTENTOS_ID) { intento ->
                val idCliente = generarIdClienteUnico()
                try {
                    db.runTransaction { transaction ->
                        val fichaRef = db.collection(COLECCION_CLIENTES)
                            .document(idCliente.toString())

                        if (transaction.get(fichaRef).exists()) {
                            throw ColisionIdClienteException()
                        }

                        transaction.set(
                            fichaRef,
                            mapOf(
                                "idCliente" to idCliente,
                                "negocioId" to negocioId,
                                "firebaseUid" to uid,
                                "codigoVinculacion" to null,
                                "serviciosContratados" to emptyList<String>(),
                                "fechaRegistro" to FieldValue.serverTimestamp(),
                                "estado" to "ACTIVO"
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

                    return ResultadoAutenticacion(
                        true,
                        "Te has vinculado al negocio correctamente"
                    )
                } catch (e: ColisionIdClienteException) {
                    // Colisión teórica de id aleatorio: se reintenta con otro.
                    if (intento == MAX_INTENTOS_ID - 1) {
                        return ResultadoAutenticacion(
                            false,
                            "No se pudo generar un identificador único. Inténtalo de nuevo"
                        )
                    }
                }
            }

            ResultadoAutenticacion(
                false,
                "No se pudo generar un identificador único. Inténtalo de nuevo"
            )
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * reclamarFichaConEnlace
     * ----------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → ResultadoAutenticacion (Vía B)
     * Lee vinculaciones/{codigo} (que identifica exactamente clienteId y
     * negocioId, sin consultar ni listar la colección clientes) y ejecuta el
     * Batch atómico: update(clientes/{clienteId}) con el UID, PENDIENTE→USADA
     * y update(usuarios/{uid}). El enlace caducado o usado se rechaza antes
     * de escribir; cualquier fallo de permisos llega traducido al usuario.
     */
    suspend fun reclamarFichaConEnlace(codigo: String): ResultadoAutenticacion {
        val usuario = auth.currentUser
            ?: return ResultadoAutenticacion(false, "No hay ningún usuario autenticado")
        val uid = usuario.uid

        return try {
            val documento = db.collection(COLECCION_VINCULACIONES)
                .document(codigo)
                .get()
                .esperar()

            if (!documento.exists()) {
                return ResultadoAutenticacion(false, "El enlace no existe")
            }

            val estado = documento.getString("estado")
            val clienteId = documento.getLong("clienteId")
            val negocioId = documento.getString("negocioId")
            val fechaExpiracion = documento.getTimestamp("fechaExpiracion")

            if (estado != ESTADO_PENDIENTE) {
                return ResultadoAutenticacion(false, "El enlace ya no está disponible")
            }

            if (clienteId == null || negocioId == null) {
                return ResultadoAutenticacion(false, "El enlace no es válido")
            }

            if (
                fechaExpiracion == null ||
                fechaExpiracion.toDate().time < System.currentTimeMillis()
            ) {
                return ResultadoAutenticacion(false, "El enlace ha caducado")
            }

            val batch = db.batch()

            batch.update(
                db.collection(COLECCION_CLIENTES).document(clienteId.toString()),
                mapOf(
                    "firebaseUid" to uid,
                    "negocioId" to negocioId
                )
            )

            batch.update(
                db.collection(COLECCION_VINCULACIONES).document(codigo),
                mapOf("estado" to ESTADO_USADA)
            )

            batch.update(
                db.collection(COLECCION_USUARIOS).document(uid),
                mapOf(
                    "clienteId" to clienteId,
                    "negocioId" to negocioId
                )
            )

            batch.commit().esperar()

            ResultadoAutenticacion(true, "Ficha reclamada correctamente")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * generarIdClienteUnico
     * ---------------------
     * ✔ TIPO: método (fun) privado de Kotlin → Int
     * Genera el candidato a idCliente: un entero aleatorio positivo dentro
     * del rango válido de Int (entre 1.000.000.000 e Int.MAX_VALUE), sin usar
     * hashCode() ni contadores. La unicidad real la garantiza la Transaction
     * con su verificación de existencia y el reintento ante colisión.
     */
    private fun generarIdClienteUnico(): Int {
        return Random.nextInt(ID_CLIENTE_MINIMO, Int.MAX_VALUE)
    }

    /**
     * generarToken
     * ------------
     * ✔ TIPO: método (fun) privado de Kotlin → String
     * Genera un token individual de 24 caracteres alfanuméricos con
     * SecureRandom. No contiene el id del cliente y es impredecible.
     */
    private fun generarToken(): String {
        return buildString {
            repeat(LONGITUD_TOKEN) {
                append(ALFABETO_TOKEN[secureRandom.nextInt(ALFABETO_TOKEN.length)])
            }
        }
    }

    /**
     * obtenerNegocioDelAdmin
     * ----------------------
     * ✔ TIPO: método (fun) suspend privado de Kotlin → String?
     * Lee el negocioId asignado al ADMIN autenticado en usuarios/{uid}.
     * Sirve para que los enlaces se creen siempre con el negocio oficial.
     */
    private suspend fun obtenerNegocioDelAdmin(): String? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            db.collection(COLECCION_USUARIOS)
                .document(uid)
                .get()
                .esperar()
                .getString("negocioId")
        } catch (_: Exception) {
            null
        }
    }

    /**
     * consultarEnlace
     * ---------------
     * ✔ TIPO: método (fun) suspend de Kotlin → ConsultaEnlace
     * Lee la ficha remota y su documento de vinculacion para informar a la UI
     * del enlace activo y de si ha caducado.
     */
    suspend fun consultarEnlace(idClienteRemoto: Int): ConsultaEnlace {
        val uid = auth.currentUser?.uid ?: return ConsultaEnlace(null, false)
        return try {
            val ficha = db.collection(COLECCION_CLIENTES)
                .document(idClienteRemoto.toString())
                .get()
                .esperar()

            if (!ficha.exists()) return ConsultaEnlace(null, false)

            val token = ficha.getString("codigoVinculacion") ?: return ConsultaEnlace(null, false)

            val vinculacion = db.collection(COLECCION_VINCULACIONES)
                .document(token)
                .get()
                .esperar()

            val fechaExpiracion = vinculacion.getTimestamp("fechaExpiracion")
            val caducado = !vinculacion.exists() ||
                (fechaExpiracion?.toDate()?.time ?: Long.MAX_VALUE) < System.currentTimeMillis()

            ConsultaEnlace(token, caducado)
        } catch (_: Exception) {
            ConsultaEnlace(null, false)
        }
    }

    /**
     * generarEnlaceParaCliente
     * ------------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → EnlaceVinculacion
     * Genera un token individual y lo asigna atómicamente: Batch con
     * set(vinculaciones/{token}) PENDIENTE a 7 días + update de la ficha
     * clientes/{id} (que debe existir y no tener UID todavía).
     */
    suspend fun generarEnlaceParaCliente(idClienteRemoto: Int): EnlaceVinculacion {
        val negocioId = obtenerNegocioDelAdmin()
            ?: return EnlaceVinculacion(false, "No hay ningún negocio sincronizado")

        val token = generarToken()
        val expiracion = Timestamp(
            java.util.Date(System.currentTimeMillis() + DIAS_EXPIRACION_ENLACE * 24 * 60 * 60 * 1000)
        )

        return try {
            val batch = db.batch()
            batch.set(
                db.collection(COLECCION_VINCULACIONES).document(token),
                mapOf(
                    "clienteId" to idClienteRemoto,
                    "negocioId" to negocioId,
                    "estado" to ESTADO_PENDIENTE,
                    "fechaExpiracion" to expiracion
                )
            )
            batch.update(
                db.collection(COLECCION_CLIENTES).document(idClienteRemoto.toString()),
                mapOf("codigoVinculacion" to token)
            )
            batch.commit().esperar()
            EnlaceVinculacion(true, "Enlace generado correctamente", token, expiracion)
        } catch (e: Exception) {
            EnlaceVinculacion(false, mensajeDe(e))
        }
    }

    /**
     * regenerarEnlaceDeCliente
     * ------------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → EnlaceVinculacion
     * Sustituye el enlace activo por uno nuevo en un único Batch: borra el
     * documento anterior, crea el nuevo token y actualiza la ficha.
     */
    suspend fun regenerarEnlaceDeCliente(
        idClienteRemoto: Int,
        tokenAnterior: String
    ): EnlaceVinculacion {
        val negocioId = obtenerNegocioDelAdmin()
            ?: return EnlaceVinculacion(false, "No hay ningún negocio sincronizado")

        val tokenNuevo = generarToken()
        val expiracion = Timestamp(
            java.util.Date(System.currentTimeMillis() + DIAS_EXPIRACION_ENLACE * 24 * 60 * 60 * 1000)
        )

        return try {
            val batch = db.batch()
            batch.delete(db.collection(COLECCION_VINCULACIONES).document(tokenAnterior))
            batch.set(
                db.collection(COLECCION_VINCULACIONES).document(tokenNuevo),
                mapOf(
                    "clienteId" to idClienteRemoto,
                    "negocioId" to negocioId,
                    "estado" to ESTADO_PENDIENTE,
                    "fechaExpiracion" to expiracion
                )
            )
            batch.update(
                db.collection(COLECCION_CLIENTES).document(idClienteRemoto.toString()),
                mapOf("codigoVinculacion" to tokenNuevo)
            )
            batch.commit().esperar()
            EnlaceVinculacion(true, "Enlace regenerado correctamente", tokenNuevo, expiracion)
        } catch (e: Exception) {
            EnlaceVinculacion(false, mensajeDe(e))
        }
    }

    /**
     * revocarEnlaceDeCliente
     * ----------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → ResultadoAutenticacion
     * Revoca el enlace activo en un único Batch: limpia codigoVinculacion de
     * la ficha y elimina el documento vinculaciones/{token}.
     */
    suspend fun revocarEnlaceDeCliente(
        idClienteRemoto: Int,
        token: String
    ): ResultadoAutenticacion {
        return try {
            val batch = db.batch()
            batch.delete(db.collection(COLECCION_VINCULACIONES).document(token))
            batch.update(
                db.collection(COLECCION_CLIENTES).document(idClienteRemoto.toString()),
                mapOf("codigoVinculacion" to null)
            )
            batch.commit().esperar()
            ResultadoAutenticacion(true, "Enlace revocado")
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e))
        }
    }

    /**
     * mensajeDe
     * ---------
     * ✔ TIPO: método (fun) privado de Kotlin → String
     * Traduce los errores típicos de Firestore a mensajes en español.
     * Sirve para que la UI nunca muestre textos técnicos en inglés.
     */
    private fun mensajeDe(e: Exception): String {
        return when {
            e.message?.contains("permission", ignoreCase = true) == true ->
                "No tienes permisos para esta operación. Revisa el código introducido"
            else -> e.message ?: "Error inesperado. Inténtalo de nuevo"
        }
    }
}
