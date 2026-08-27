package com.roberto.gestorpro.navigation

/**
 * Routes.kt
 * ---------
 * ✔ TIPO: archivo de código fuente Kotlin (navegación)
 * Es el archivo que centraliza las rutas de navegación de GestorPro Admin.
 * Sirve para que NavHost y todas las pantallas usen las mismas rutas y la
 * navegación sea consistente. (Las rutas de CLIENTE y Vía B quedaron fuera:
 * la app Admin es exclusiva del administrador.)
 */
object Routes {

    /**
     * LOGIN
     * -----
     * Ruta de la pantalla de inicio de sesión.
     */
    const val LOGIN = "login"

    /**
     * REGISTRO
     * --------
     * Ruta de la pantalla de creación de cuenta real con Firebase.
     */
    const val REGISTRO = "registro"

    /**
     * RECUPERAR_PASSWORD
     * ------------------
     * Ruta de la pantalla de recuperación de contraseña.
     */
    const val RECUPERAR_PASSWORD = "recuperar_password"

    /**
     * HOME
     * ----
     * Ruta de la pantalla de inicio o menú principal del administrador.
     */
    const val HOME = "home"

    /**
     * CLIENTES
     * --------
     * Ruta de la pantalla de la lista de clientes.
     */
    const val CLIENTES = "clientes"

    /**
     * PERFILCLIENTE
     * -------------
     * Ruta de la pantalla de perfil de un cliente.
     */
    const val PERFILCLIENTE = "perfil_cliente"

    /**
     * perfilCliente
     * -------------
     * Ruta dinámica del perfil de un cliente concreto: perfil_cliente/{id}.
     */
    fun perfilCliente(idCliente: Int): String {
        return "$PERFILCLIENTE/$idCliente"
    }

    /**
     * AÑADIRCLIENTE
     * -------------
     * Ruta del formulario para añadir un nuevo cliente.
     */
    const val AÑADIRCLIENTE = "añadir_cliente"

    /**
     * MODIFICARCLIENTE
     * ----------------
     * Ruta base del formulario para modificar un cliente existente.
     */
    const val MODIFICARCLIENTE = "modificar_cliente"

    const val ECONOMIA = "economia"
    const val CONFIGURACION = "configuracion"
    const val PREFERENCIAS = "preferencias"
    const val DATOS = "datos"
    const val CUENTA = "cuenta"
    const val CLASES = "clases"
    const val CREAR_CLASE = "crear_clase"

    /**
     * MINEGOCIO
     * ---------
     * Ruta de la pantalla de personalización del negocio (nombre y logo).
     */
    const val MINEGOCIO = "mi_negocio"

    /**
     * CREAR_NEGOCIO
     * -------------
     * Ruta del alta remota del negocio (negocios + negocios_publicos +
     * usuarios/{uid}) con su código maestro.
     */
    const val CREAR_NEGOCIO = "crear_negocio"

    fun detalleClase(idClase: Int): String {
        return "detalle_clase/$idClase"
    }

    const val DETALLE_CLASE = "detalle_clase"

    const val DETALLE_SESION_RESERVAS = "detalle_sesion_reservas"

    fun detalleSesionReservas(idSesion: Int): String {
        return "detalle_sesion_reservas/$idSesion"
    }

    /**
     * modificarCliente
     * ----------------
     * Ruta dinámica del formulario de modificación de un cliente concreto.
     */
    fun modificarCliente(idCliente: Int): String {
        return "$MODIFICARCLIENTE/$idCliente"
    }

}
