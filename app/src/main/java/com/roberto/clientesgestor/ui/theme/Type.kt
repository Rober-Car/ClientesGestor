package com.roberto.clientesgestor.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Type.kt
 * -------
 * ✔ TIPO: archivo de código fuente Kotlin (tema)
 * Es el archivo que define la tipografía del tema de la aplicación.
 * Sirve para que todos los textos de la app usen una tipografía consistente.
 */

/**
 * Typography
 * ----------
 * ✔ TIPO: propiedad (val) → Typography de Material 3
 * Es el objeto que define los estilos de texto del tema de la aplicación.
 * Sirve para que todos los textos de la app usen una tipografía consistente.
 */
val Typography = Typography(

    /**
     * bodyLarge
     * ---------
     * ✔ TIPO: estilo de texto (TextStyle) dentro de Typography
     * Es el estilo de texto principal para los cuerpos de texto grandes.
     * Sirve para que los textos normales de la app usen la familia por defecto,
     * peso normal, tamaño 16sp y una altura de línea de 24sp.
     */
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)
