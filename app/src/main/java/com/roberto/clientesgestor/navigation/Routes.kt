package com.roberto.clientesgestor.navigation

/**
 * Routes.kt
 * ---------
 * ✔ TIPO: archivo de código fuente Kotlin (navegación)
 * Es el archivo que centraliza las rutas de navegación de la aplicación.
 * Sirve para que NavHost y todas las pantallas usen las mismas rutas y la navegación sea consistente.
 */

/**
 * Routes
 * ------
 * ✔ TIPO: object (objeto singleton de Kotlin)
 * Es un objeto único que almacena las constantes de las rutas de navegación.
 * Sirve para evitar errores tipográficos y centralizar las rutas del NavHost.
 */
object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val CLIENTES = "clientes"
    const val PERFILCLIENTE = "perfil_cliente"
}
