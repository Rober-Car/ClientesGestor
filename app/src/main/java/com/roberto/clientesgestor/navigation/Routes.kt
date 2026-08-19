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

    /**
     * perfilCliente
     * -------------
     * ✔ TIPO: función (fun) → String
     * Es la función que genera la ruta completa de la pantalla de perfil de un cliente concreto.
     * Sirve para construir la ruta dinámica concatenando PERFILCLIENTE con el id del cliente.
     * Ejemplo de uso: perfilCliente(5) → "perfil_cliente/5"
     */
    fun perfilCliente(idCliente: Int): String {
        return "$PERFILCLIENTE/$idCliente"
    }

    /**
     * AÑADIRCLIENTE
     * -------------
     * ✔ TIPO: constante (const val) → String
     * Es la ruta de la pantalla de formulario para añadir un nuevo cliente.
     * Sirve para que NavHost y las pantallas naveguen hasta el formulario de alta con la misma ruta.
     */
    const val AÑADIRCLIENTE = "añadir_cliente"

    /**
     * MODIFICARCLIENTE
     * ----------------
     * ✔ TIPO: constante (const val) → String
     * Es la ruta base de la pantalla de formulario para modificar un cliente existente.
     * Sirve como prefijo de la ruta dinámica que incluye el id del cliente a modificar.
     */
    const val MODIFICARCLIENTE = "modificar_cliente"

    const val ECONOMIA = "economia"

    /**
     * modificarCliente
     * ----------------
     * ✔ TIPO: función (fun) → String
     * Es la función que genera la ruta completa de la pantalla de modificación de un cliente concreto.
     * Sirve para construir la ruta dinámica concatenando MODIFICARCLIENTE con el id del cliente.
     * Ejemplo de uso: modificarCliente(3) → "modificar_cliente/3"
     */
    fun modificarCliente(idCliente: Int): String {
        return "$MODIFICARCLIENTE/$idCliente"
    }

}
