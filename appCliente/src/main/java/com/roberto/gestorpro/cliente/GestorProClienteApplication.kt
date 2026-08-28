package com.roberto.gestorpro.cliente

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * GestorProClienteApplication
 * ---------------------------
 * ✔ TIPO: Application de Hilt
 * Es el punto de entrada de la app GestorPro Cliente.
 * Sirve para que Hilt construya el grafo de dependencias de la aplicación.
 */
@HiltAndroidApp
class GestorProClienteApplication : Application()
