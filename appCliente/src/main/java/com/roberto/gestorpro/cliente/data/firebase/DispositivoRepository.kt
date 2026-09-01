package com.roberto.gestorpro.cliente.data.firebase

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.roberto.gestorpro.cliente.data.repository.PreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/**
 * DispositivoRepository
 * ---------------------
 * Gestiona el registro del token FCM del dispositivo del CLIENTE en Firestore:
 * clientes/{idCliente}/dispositivos/{token}. Un cliente puede tener varios
 * dispositivos (una subcolección por token). El envío real del push lo hace el
 * backend (Cloud Functions), nunca la app.
 */
@Singleton
class DispositivoRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val preferencesRepository: PreferencesRepository
) {

    companion object {
        private const val TAG = "DispositivoRepository"
        private const val COLECCION_CLIENTES = "clientes"
        private const val COLECCION_DISPOSITIVOS = "dispositivos"
    }

    /**
     * registrarTokenActual
     * --------------------
     * Lee el token FCM actual del dispositivo y lo registra si el cliente está
     * vinculado (hay idCliente guardado). Sin cliente vinculado no hace nada.
     */
    suspend fun registrarTokenActual() {
        val idCliente = preferencesRepository.idCliente.first() ?: return
        val token = try {
            FirebaseMessaging.getInstance().token.esperar()
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo obtener el token FCM: ${e.message}")
            return
        }
        registrarToken(token)
    }

    /**
     * registrarToken
     * --------------
     * Guarda o actualiza el token del dispositivo bajo la ficha del cliente.
     * Si el cliente no está vinculado aún, no escribe nada. Los fallos se
     * registran pero no rompen la app (las Rules de la subcolección pueden
     * llegar más tarde; se reintentará con el siguiente token/mensaje).
     */
    suspend fun registrarToken(token: String) {
        val idCliente = preferencesRepository.idCliente.first() ?: return
        try {
            db.collection(COLECCION_CLIENTES)
                .document(idCliente.toString())
                .collection(COLECCION_DISPOSITIVOS)
                .document(token)
                .set(
                    mapOf(
                        "token" to token,
                        "plataforma" to "android",
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
                .esperar()
            Log.d(TAG, "Token FCM registrado para el cliente $idCliente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar el token FCM del cliente $idCliente: ${e.message}", e)
        }
    }
}
