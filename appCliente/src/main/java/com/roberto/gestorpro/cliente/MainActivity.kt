package com.roberto.gestorpro.cliente

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.roberto.gestorpro.cliente.navigation.AppNavigation
import com.roberto.gestorpro.cliente.ui.theme.GestorProClienteTheme
import com.roberto.gestorpro.cliente.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity
 * ------------
 * ✔ TIPO: Activity única de GestorPro Cliente (@AndroidEntryPoint)
 * Es el punto de entrada Compose de la aplicación.
 * Sirve para arrancar el NavHost del cliente con el tema del sistema.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val themeMode by viewModel.themeMode.collectAsState()

            GestorProClienteTheme(themeMode = themeMode) {
                AppNavigation()
            }
        }
    }
}
