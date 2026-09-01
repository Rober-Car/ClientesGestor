package com.roberto.gestorpro.cliente.ui.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.roberto.gestorpro.cliente.MainActivity
import com.roberto.gestorpro.cliente.R
import com.roberto.gestorpro.cliente.data.firebase.DispositivoRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * FcmService
 * ----------
 * Servicio de mensajería Firebase (FCM) de GestorPro Cliente.
 * - onNewToken: registra el token nuevo en Firestore si el cliente está vinculado.
 * - onMessageReceived: muestra una notificación local cuando la app está en
 *   primer plano (o el proceso activo); en segundo plano el sistema muestra la
 *   notificación de FCM. El buzón real se leerá de Firestore en la Fase C.
 */
@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {

    @Inject
    lateinit var dispositivoRepository: DispositivoRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM")
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                dispositivoRepository.registrarToken(token)
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo registrar el token FCM: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val titulo = data["titulo"] ?: message.notification?.title ?: "Notificación"
        val cuerpo = data["mensaje"] ?: message.notification?.body ?: ""
        mostrarNotificacion(titulo, cuerpo)
    }

    private fun mostrarNotificacion(titulo: String, cuerpo: String) {
        val canal = CANAL_NOTIFICACIONES
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                canal,
                "Notificaciones del gimnasio",
                NotificationManager.IMPORTANCE_HIGH
            )
            val gestor = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            gestor.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pending = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(this, canal)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(ID_NOTIFICACION, notificacion)
        } catch (e: SecurityException) {
            Log.w(TAG, "Sin permiso para mostrar la notificación local")
        }
    }

    companion object {
        private const val TAG = "FcmService"
        private const val CANAL_NOTIFICACIONES = "notificaciones"
        private const val ID_NOTIFICACION = 1000
    }
}
