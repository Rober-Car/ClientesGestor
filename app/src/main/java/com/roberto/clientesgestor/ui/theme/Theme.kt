package com.roberto.clientesgestor.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Theme.kt
 * --------
 * ✔ TIPO: archivo de código fuente Kotlin (tema)
 * Es el archivo que define el tema de Material 3 de la aplicación.
 * Sirve para que toda la app use un estilo visual coherente (colores y tipografía).
 */

/**
 * DarkColorScheme
 * ---------------
 * ✔ TIPO: constante privada (private val) → ColorScheme
 * Es el esquema de colores que se usa en modo oscuro.
 * Sirve para dar estilo a la app cuando el sistema está en tema oscuro.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

/**
 * LightColorScheme
 * ----------------
 * ✔ TIPO: constante privada (private val) → ColorScheme
 * Es el esquema de colores que se usa en modo claro.
 * Sirve para dar estilo a la app cuando el sistema está en tema claro.
 */
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

/**
 * ClientesGestorTheme
 * -------------------
 * ✔ TIPO: función @Composable (tema de la aplicación)
 * Es el componente que envuelve toda la interfaz para aplicar el tema.
 * Sirve para que todas las pantallas hereden colores y estilos consistentes.
 */
@Composable
fun ClientesGestorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
