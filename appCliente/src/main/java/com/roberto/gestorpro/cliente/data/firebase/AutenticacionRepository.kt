package com.roberto.gestorpro.cliente.data.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * ResultadoAutenticacion
 * ---------------------
 * ✔ TIPO: data class
 * Resultado unificado de las operaciones de autenticación.
 */
data class ResultadoAutenticacion(
    val exito: Boolean,
    val mensaje: String,
    val rol: String? = null
)

/**
 * AutenticacionRepository
 * -----------------------
 * ✔ TIPO: clase @Singleton inyectada por Hilt
 * Encapsula Firebase Authentication y el documento usuarios/{uid} del CLIENTE.
 */
@Singleton
class AutenticacionRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    companion object {
        const val ROL_CLIENTE = "CLIENTE"

        private const val COLECCION_USUARIOS = "usuarios"

        private const val MENSAJE_RECUPERACION_ENVIADO =
            "Si el email existe, recibirás un enlace para restablecer tu contraseña"
    }

    fun haySesionActiva(): Boolean {
        return auth.currentUser != null
    }

    /**
     * registrar
     * ---------
     * Crea la cuenta en Firebase Authentication y el documento usuarios/{uid}
     * con rol CLIENTE, activo true, clienteId null y negocioId null.
     */
    suspend fun registrar(
        email: String,
        contrasena: String
    ): ResultadoAutenticacion {
        return try {
            val credencial = auth.createUserWithEmailAndPassword(email, contrasena).esperar()
            val usuario = credencial.user

            if (usuario == null) {
                ResultadoAutenticacion(false, "No se pudo crear la cuenta")
            } else {
                try {
                    db.collection(COLECCION_USUARIOS)
                        .document(usuario.uid)
                        .set(
                            mapOf(
                                "rol" to ROL_CLIENTE,
                                "activo" to true,
                                "clienteId" to null,
                                "negocioId" to null
                            )
                        )
                        .esperar()
                    ResultadoAutenticacion(true, "Cuenta creada correctamente", ROL_CLIENTE)
                } catch (e: Exception) {
                    try {
                        usuario.delete().esperar()
                    } catch (_: Exception) {
                    }
                    ResultadoAutenticacion(
                        false,
                        "No se pudo crear el perfil del usuario: ${mensajeDe(e, false)}"
                    )
                }
            }
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e, false))
        }
    }

    /**
     * iniciarSesion
     * -------------
     * Inicia sesión y comprueba el documento usuarios/{uid}.
     */
    suspend fun iniciarSesion(
        email: String,
        contrasena: String
    ): ResultadoAutenticacion {
        return try {
            val credencial = auth.signInWithEmailAndPassword(email, contrasena).esperar()
            val usuario = credencial.user

            if (usuario == null) {
                cerrarSesion()
                return ResultadoAutenticacion(false, "No se pudo iniciar sesión")
            }

            try {
                val documento = db.collection(COLECCION_USUARIOS)
                    .document(usuario.uid)
                    .get()
                    .esperar()

                if (!documento.exists()) {
                    cerrarSesion()
                    ResultadoAutenticacion(
                        false,
                        "El usuario no tiene perfil en la base de datos"
                    )
                } else if (documento.getBoolean("activo") != true) {
                    cerrarSesion()
                    ResultadoAutenticacion(false, "Esta cuenta está desactivada")
                } else if (documento.getString("rol") != ROL_CLIENTE) {
                    cerrarSesion()
                    ResultadoAutenticacion(
                        false,
                        "Esta cuenta no pertenece a la app de clientes"
                    )
                } else {
                    ResultadoAutenticacion(
                        true,
                        "Sesión iniciada correctamente",
                        ROL_CLIENTE
                    )
                }
            } catch (e: Exception) {
                cerrarSesion()
                ResultadoAutenticacion(
                    false,
                    "No se pudo leer el perfil del usuario: ${mensajeDe(e, true)}"
                )
            }
        } catch (e: Exception) {
            ResultadoAutenticacion(false, mensajeDe(e, true))
        }
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    /**
     * enviarCorreoRecuperacion
     * ------------------------
     * Envía el correo de recuperación con sendPasswordResetEmail. Ante errores
     * de autenticación responde el mismo mensaje genérico (no revela existencia).
     */
    suspend fun enviarCorreoRecuperacion(email: String): ResultadoAutenticacion {
        return try {
            auth.sendPasswordResetEmail(email).esperar()
            ResultadoAutenticacion(true, MENSAJE_RECUPERACION_ENVIADO)
        } catch (e: Exception) {
            when (e) {
                is FirebaseAuthInvalidUserException,
                is FirebaseAuthInvalidCredentialsException,
                is FirebaseAuthUserCollisionException,
                is FirebaseAuthWeakPasswordException ->
                    ResultadoAutenticacion(true, MENSAJE_RECUPERACION_ENVIADO)
                is FirebaseNetworkException ->
                    ResultadoAutenticacion(
                        false,
                        "No hay conexión con Firebase. Comprueba tu conexión a Internet"
                    )
                else ->
                    ResultadoAutenticacion(
                        false,
                        "No se pudo enviar el correo. Inténtalo de nuevo"
                    )
            }
        }
    }

    private fun mensajeDe(e: Exception, alIniciarSesion: Boolean): String {
        return when (e) {
            is FirebaseAuthUserCollisionException ->
                "Ya existe una cuenta con este email"
            is FirebaseAuthWeakPasswordException ->
                "La contraseña debe tener al menos 6 caracteres"
            is FirebaseAuthInvalidUserException ->
                "No existe una cuenta con este email"
            is FirebaseAuthInvalidCredentialsException ->
                if (alIniciarSesion) {
                    "Email o contraseña incorrectos"
                } else {
                    "El email no tiene un formato válido"
                }
            else -> e.message ?: "Error inesperado. Inténtalo de nuevo"
        }
    }
}

/**
 * esperar
 * -------
 * ✔ TIPO: función de extensión internal (suspend) sobre Task<T>
 * Convierte un Task de Firebase en una llamada suspendible.
 */
internal suspend fun <T> Task<T>.esperar(): T =
    suspendCancellableCoroutine { continuacion ->
        addOnSuccessListener { resultado ->
            continuacion.resume(resultado)
        }
        addOnFailureListener { error ->
            continuacion.resumeWithException(error)
        }
    }
