package com.roberto.clientesgestor

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * ClientesGestorApplication.kt
 * ----------------------------
 * ✔ TIPO: archivo de código fuente Kotlin (aplicación)
 * Es el archivo que define la clase Application de la aplicación.
 * Sirve para inicializar Hilt y dar el punto de entrada de la app a Android.
 */

/**
 * @HiltAndroidApp
 * ---------------
 * ✔ TIPO: anotación (dagger.hilt.android.HiltAndroidApp)
 * Es la anotación que marca esta clase como la Application principal con Hilt.
 * Sirve para que Hilt genere el componente y pueda inyectar dependencias en toda la app.
 */
@HiltAndroidApp
class ClientesGestorApplication : Application()
