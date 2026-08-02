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
 *
 * ¿Qué es?
 * El archivo que define la tipografía del tema de la aplicación.
 *
 * ¿Qué hace?
 * - Declara Typography, el estilo de texto base del tema.
 * - Personaliza bodyLarge con la fuente, tamaño y espaciado por defecto.
 *
 * ¿Para qué sirve?
 * Para que todos los textos de la app usen una tipografía consistente.
 */

/**
 * Typography
 * ----------
 * ✔ TIPO: constante (val) de tipografía
 * ✔ TIPO REAL: androidx.compose.material3.Typography
 *
 * ¿Qué es?
 * El conjunto de estilos de texto (text styles) que usa la aplicación.
 *
 * ¿Qué hace?
 * - Define bodyLarge con fuente por defecto, peso normal, 16sp de tamaño,
 *   24sp de alto de línea y 0.5sp de espaciado.
 *
 * ¿Para qué sirve?
 * Para estilizar los textos con MaterialTheme.typography en toda la app.
 */

// Set of Material typography styles to start with
val Typography = Typography(
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