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
     * ✔ TIPO: constante (const val) → String
     * Es la ruta de la pantalla de inicio de sesión.
     * Sirve para que NavHost y las pantallas naveguen hasta Login con la misma ruta.
     */
    const val LOGIN = "login"

    /**
     * HOME
     * ----
     * ✔ TIPO: constante (const val) → String
     * Es la ruta de la pantalla de inicio o menú principal.
     * Sirve para que NavHost y las pantallas naveguen hasta Home con la misma ruta.
     */
    const val HOME = "home"

    /**
     * CLIENTES
     * --------
     * ✔ TIPO: constante (const val) → String
     * Es la ruta de la pantalla de la lista de clientes.
     * Sirve para que NavHost y las pantallas naveguen hasta Clientes con la misma ruta.
     */
    const val CLIENTES = "clientes"

    /**
     * PERFILCLIENTE
     * -------------
     * ✔ TIPO: constante (const val) → String
     * Es la ruta de la pantalla de perfil de un cliente.
     * Sirve para que NavHost y las pantallas naveguen hasta el perfil con la misma ruta.
     */
    const val PERFILCLIENTE = "perfil_cliente"
}
