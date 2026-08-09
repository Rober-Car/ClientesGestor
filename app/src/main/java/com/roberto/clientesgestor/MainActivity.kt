package com.roberto.clientesgestor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.roberto.clientesgestor.navigation.AppNavigation
import com.roberto.clientesgestor.ui.theme.ClientesGestorTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity.kt
 * ---------------
 * ✔ TIPO: archivo de código fuente Kotlin (Activity principal)
 * Es el archivo de entrada principal de la aplicación, contiene la Activity que Android crea al abrir la app.
 * Sirve para arrancar la aplicación y montar la interfaz Compose completa de ClientesGestor.
 */

/**
 * MainActivity
 * ------------
 * ✔ TIPO: clase (ComponentActivity)
 * Es la ventana principal de la aplicación, el punto de entrada que usa Android para lanzarla.
 * Sirve como contenedor base donde se muestra toda la interfaz.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * onCreate
     * --------
     * ✔ TIPO: método sobrescrito del ciclo de vida de Android
     * Es el método que Android ejecuta al crear la Activity por primera vez.
     * Sirve para activar el modo edge-to-edge y montar el tema ClientesGestorTheme con AppNavigation.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClientesGestorTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ClientesGestorTheme {
        Greeting("Android")
    }
}
