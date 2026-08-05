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

    /**
     * LOGIN
     * -----
     * ✔ TIPO: constante (const val)
     * Es la ruta de la pantalla de inicio de sesión.
     * Sirve para navegar al login con navController.navigate(LOGIN).
     */
    const val LOGIN = "login"

    /**
     * HOME
     * ----
     * ✔ TIPO: constante (const val)
     * Es la ruta de la pantalla principal.
     * Sirve para navegar al menú principal con navController.navigate(HOME).
     */
    const val HOME = "home"

    /**
     * CLIENTES
     * --------
     * ✔ TIPO: constante (const val)
     * Es la ruta de la pantalla de gestión de clientes.
     * Sirve para navegar a la pantalla de clientes con navController.navigate(CLIENTES).
     */
    const val CLIENTES = "clientes"


    /**
     * PERFILCLIENTE
     * --------------
     * ✔ TIPO: constante (const val)
     * Es la ruta de la pantalla de perfil del cliente.
     * Sirve para navegar al detalle de un cliente con navController.navigate(PERFILCLIENTE).
     */
    const val PERFILCLIENTE = "perfil_cliente"
}
