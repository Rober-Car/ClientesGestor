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
/**
 * @AndroidEntryPoint
 * ------------------
 * ✔ TIPO: anotación (dagger.hilt.android.AndroidEntryPoint)
 * Es la anotación que registra esta Activity como punto de inyección de Hilt.
 * Sirve para que Hilt genere el código que inyecta las dependencias
 * en esta Activity y en las que se montan a partir de ella.
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
        // super.onCreate: ejecuta la preparación básica de la Activity que hace Android.
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge: permite que la app pinte detrás de la barra de estado y navegación.
        enableEdgeToEdge()
        // setContent: monta el contenido Compose de la pantalla; se llama una única vez.
        setContent {
            // ClientesGestorTheme: aplica colores y tipografía del tema a toda la interfaz.
            ClientesGestorTheme {
                // AppNavigation: define el NavHost con todas las rutas de la aplicación.
                AppNavigation()
            }
        }
    }
}

/**
 * Greeting
 * --------
 * ✔ TIPO: función @Composable
 * Es un componente simple que muestra un texto de saludo.
 * Sirve como ejemplo de composable con parámetros (nombre y modificador).
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    // Text: composable de Material que muestra el texto de saludo.
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

/**
 * GreetingPreview
 * ---------------
 * ✔ TIPO: función @Composable anotada con @Preview
 * Es la vista previa del componente Greeting para el modo diseño.
 * Sirve para previsualizar el saludo de ejemplo dentro del tema de la app en Android Studio.
 */
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ClientesGestorTheme {
        // Greeting: se muestra el saludo de ejemplo dentro del tema de la app.
        Greeting("Android")
    }
}
