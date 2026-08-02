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

/**
 * MainActivity.kt
 * ---------------
 * ✔ TIPO: archivo de código fuente Kotlin (Activity principal)
 *
 * ¿Qué es?
 * El archivo de entrada principal de la aplicación.
 * Contiene la Activity que Android crea al abrir la app.
 *
 * ¿Qué hace?
 * - Declara la clase MainActivity, que extiende ComponentActivity.
 * - En onCreate monta el contenido de la app con Compose:
 *   el tema ClientesGestorTheme y la navegación AppNavigation.
 *
 * ¿Para qué sirve?
 * Para arrancar la aplicación y mostrar la interfaz completa
 * de ClientesGestor (login, menú y pantallas de gestión).
 */

/**
 * MainActivity
 * ------------
 * ✔ TIPO: clase
 * ✔ TIPO REAL: ComponentActivity
 * ✔ CLASE: android.app.Activity / androidx.activity.ComponentActivity
 *
 * ¿Qué es?
 * La ventana principal de la aplicación, el punto de entrada
 * que Android usa para lanzar la app.
 *
 * ¿Qué hace?
 * - Hereda de ComponentActivity (Activity compatible con Compose).
 * - Activa el modo edge-to-edge para que el contenido ocupe toda la pantalla.
 * - Llama a setContent para dibujar la interfaz con Jetpack Compose.
 *
 * ¿Para qué sirve?
 * Para ser el contenedor base donde se muestra toda la aplicación.
 */
class MainActivity : ComponentActivity() {

    /**
     * onCreate
     * --------
     * ✔ TIPO: función sobrescrita (override)
     * ✔ CLASE: ciclo de vida de Android
     *
     * ¿Qué es?
     * El método que Android ejecuta cuando la Activity se crea por primera vez.
     *
     * ¿Qué hace?
     * - Configura el modo edge-to-edge.
     * - Monta el contenido Compose: ClientesGestorTheme (tema) y
     *   AppNavigation (sistema de navegación entre pantallas).
     *
     * ¿Para qué sirve?
     * Para preparar la pantalla principal y mostrar la UI de la app.
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

/**
 * Greeting
 * --------
 * ✔ TIPO: función @Composable
 *
 * ¿Qué es?
 * Un composable sencillo que muestra un texto de saludo.
 *
 * ¿Qué hace?
 * - Recibe un nombre y un Modifier.
 * - Dibuja un Text con el mensaje "Hello $name".
 *
 * ¿Para qué sirve?
 * Es un ejemplo de función composable; no se usa en la navegación real.
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

/**
 * GreetingPreview
 * ---------------
 * ✔ TIPO: función @Composable con anotación @Preview
 *
 * ¿Qué es?
 * Una vista previa de diseño que se muestra dentro de Android Studio.
 *
 * ¿Qué hace?
 * - Renderiza Greeting("Android") dentro del tema de la app.
 * - Solo sirve para previsualizar en el editor, no se ejecuta en el dispositivo.
 *
 * ¿Para qué sirve?
 * Para ver el resultado de Greeting sin necesidad de instalar la app.
 */
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ClientesGestorTheme {
        Greeting("Android")
    }
}