package com.roberto.clientesgestor.ui.theme

import android.app.Activity
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
 *
 * ¿Qué es?
 * El archivo que define el tema de Material 3 de la aplicación.
 *
 * ¿Qué hace?
 * - Declara los esquemas de color claro y oscuro.
 * - Declara ClientesGestorTheme, el composable que aplica el tema.
 * - Soporta color dinámico en Android 12+ y modo oscuro automático.
 *
 * ¿Para qué sirve?
 * Para que toda la app use un estilo visual coherente (colores y tipografía).
 */

/**
 * DarkColorScheme
 * ---------------
 * ✔ TIPO: constante privada (private val)
 * ✔ TIPO REAL: ColorScheme
 *
 * ¿Qué es?
 * El esquema de colores que se usa en modo oscuro.
 *
 * ¿Qué hace?
 * - Define primary, secondary y tertiary con los colores ...80.
 *
 * ¿Para qué sirve?
 * Para dar estilo a la app cuando el sistema está en tema oscuro.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

/**
 * LightColorScheme
 * ----------------
 * ✔ TIPO: constante privada (private val)
 * ✔ TIPO REAL: ColorScheme
 *
 * ¿Qué es?
 * El esquema de colores que se usa en modo claro.
 *
 * ¿Qué hace?
 * - Define primary, secondary y tertiary con los colores ...40.
 *
 * ¿Para qué sirve?
 * Para dar estilo a la app cuando el sistema está en tema claro.
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
 *
 * ¿Qué es?
 * El componente que envuelve toda la interfaz para aplicar el tema.
 *
 * ¿Qué hace?
 * - Detecta el modo oscuro del sistema.
 * - Usa color dinámico en Android 12+ o los esquemas fijos.
 * - Aplica el ColorScheme y la tipografía a MaterialTheme.
 *
 * ¿Para qué sirve?
 * Para que todas las pantallas hereden colores y estilos consistentes.
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