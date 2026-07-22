package com.roberto.clientesgestor.navigation

/**
 * Routes
 * ------
 * ✔ TIPO: object (objeto singleton de Kotlin)
 * Es una declaración de un objeto único en toda la aplicación.
 * Sirve para almacenar valores constantes relacionados con las rutas de navegación.
 *
 * Sintaxis:
 * object Nombre {
 *     const val RUTA = "valor"
 * }
 *
 * Uso:
 * Se utiliza para evitar errores tipográficos y centralizar las rutas del NavHost.
 */
object Routes {

    /**
     * LOGIN
     * -----
     * ✔ TIPO: constante (const val)
     * Es una cadena inmutable que representa la ruta de la pantalla de login.
     * Sirve para navegar a la pantalla de inicio de sesión mediante navController.navigate(LOGIN).
     *
     * (Detalles completos sobre constantes ya explicados en esta primera aparición)
     */
    const val LOGIN = "login"
    const val HOME = "home"

    const val CLIENTES = "clientes"
}
