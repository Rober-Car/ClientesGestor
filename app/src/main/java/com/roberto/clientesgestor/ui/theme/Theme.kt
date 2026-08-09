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
    /**
     * darkTheme
     * ---------
     * ✔ TIPO: parámetro (param) → Boolean
     * Es el indicador de si la app debe usar el tema oscuro.
     * Sirve para elegir el esquema de colores oscuro o claro,
     * detectando automáticamente la preferencia del sistema por defecto.
     */
    darkTheme: Boolean = isSystemInDarkTheme(),

    // El color dinámico está disponible a partir de Android 12+
    /**
     * dynamicColor
     * ------------
     * ✔ TIPO: parámetro (param) → Boolean
     * Es el indicador de si se usa el color dinámico de Material You.
     * Sirve para que la app tome los colores del fondo de pantalla del dispositivo
     * cuando el sistema lo permite (Android 12+).
     */
    dynamicColor: Boolean = true,

    /**
     * content
     * -------
     * ✔ TIPO: parámetro (param) → @Composable () -> Unit (lambda)
     * Es el contenido de la interfaz que envuelve el tema.
     * Sirve para aplicar el tema a todas las pantallas de la aplicación.
     */
    content: @Composable () -> Unit
) {

    /**
     * colorScheme
     * -----------
     * ✔ TIPO: variable inmutable (val) → ColorScheme
     * Es el esquema de colores elegido para la app.
     * Sirve para decidir qué colores usar según el tema y la versión de Android.
     */
    val colorScheme = when {

        /**
         * Rama del color dinámico
         * -----------------------
         * ✔ TIPO: rama (when) del esquema de colores
         * Es el caso en que el color dinámico está activo y el dispositivo es Android 12+.
         * Sirve para usar los colores dinámicos del fondo de pantalla
         * (oscuros o claros según el tema activo).
         */
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        /**
         * Rama del tema oscuro
         * --------------------
         * ✔ TIPO: rama (when) del esquema de colores
         * Es el caso en que está activo el tema oscuro sin color dinámico.
         * Sirve para usar el esquema de colores oscuro personalizado.
         */
        darkTheme -> DarkColorScheme

        /**
         * Rama del tema claro
         * -------------------
         * ✔ TIPO: rama (when) del esquema de colores
         * Es el caso restante en que la app usa el tema claro.
         * Sirve para usar el esquema de colores claro personalizado.
         */
        else -> LightColorScheme
    }

    /**
     * MaterialTheme
     * -------------
     * ✔ TIPO: función @Composable (androidx.compose.material3.MaterialTheme)
     * Es el componente que aplica el tema a toda la interfaz.
     * Sirve para que todas las pantallas hereden el esquema de colores
     * y la tipografía definidos en la aplicación.
     */
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
