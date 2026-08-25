package com.roberto.gestorpro

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.roberto.gestorpro.navigation.AppNavigation
import com.roberto.gestorpro.navigation.EnlacePendiente
import com.roberto.gestorpro.ui.theme.GestorProTheme
import com.roberto.gestorpro.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity
 * ------------
 * ✔ TIPO: Activity única de la aplicación (@AndroidEntryPoint)
 * Es el punto de entrada Compose y el receptor del deep link de vinculación
 * gestorpro://vincular/{token}. Sirve para extraer el token del intent y
 * dejarlo retenido en EnlacePendiente hasta que el CLIENTE pueda reclamar
 * su ficha; la estructura permite evolucionar a App Links HTTPS sin tocar
 * las pantallas.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        retenerCodigoDeEnlace(intent)
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val themeMode by viewModel.themeMode.collectAsState()

            GestorProTheme(themeMode = themeMode) {
                AppNavigation()
            }
        }
    }

    /**
     * onNewIntent
     * -----------
     * ✔ TIPO: método (override fun) de Activity
     * Se ejecuta cuando llega un deep link con la actividad ya abierta
     * (launchMode singleTask). Sirve para retener el nuevo token sin
     * recrear la pantalla.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        retenerCodigoDeEnlace(intent)
    }

    /**
     * retenerCodigoDeEnlace
     * ---------------------
     * ✔ TIPO: método (fun) privado de Kotlin
     * Extrae el token de un URI gestorpro://vincular/{token} y lo guarda en
     * EnlacePendiente. Sirve para que ni el Manifest ni las pantallas
     * dependan de cómo llegue el enlace (custom scheme hoy, App Links luego).
     */
    private fun retenerCodigoDeEnlace(intent: Intent?) {
        val uri: Uri = intent?.data ?: return
        if (uri.scheme == SCHEMA_GESTORPRO && uri.host == HOST_VINCULAR) {
            val token = uri.lastPathSegment
            if (!token.isNullOrBlank()) {
                EnlacePendiente.codigo = token
            }
        }
    }

    companion object {
        private const val SCHEMA_GESTORPRO = "gestorpro"
        private const val HOST_VINCULAR = "vincular"
    }
}
