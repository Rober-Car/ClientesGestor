package com.roberto.gestorpro.navigation

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
     * REGISTRO
     * --------
     * ✔ TIPO: constante (const val) → String
     * Es la ruta de la pantalla de creación de cuenta real con Firebase.
     * Sirve para que un usuario nuevo se registre con email y contraseña
     * manteniendo el perfil (Administrador/Cliente) elegido previamente.
     */
    const val REGISTRO = "registro"

    /**
     * SELECCION_TIPO_USUARIO
     * ----------------------
     * ✔ TIPO: constante (const val) → String
     * Es la ruta de la pantalla inicial de selección de perfil ("¿Cómo vas a utilizar GestorPro?").
     * Sirve para que el usuario elija entre Administrador/Negocio o Cliente
     * solo la primera vez que abre la app (o tras restablecerlo desde Cuenta).
     */
    const val SELECCION_TIPO_USUARIO = "seleccion_tipo_usuario"

    /**
     * HOME
     * ----
     * ✔ TIPO: constante (const val) → String
     * Es la ruta de la pantalla de inicio o menú principal.
     * Sirve para que NavHost y las pantallas naveguen hasta Home con la misma ruta.
     */
    const val HOME = "home"

    /**
     * HOME_CLIENTE
     * ------------
     * ✔ TIPO: constante (const val) → String
     * Es la ruta de la pantalla de inicio exclusiva del perfil Cliente.
     * Sirve para que los usuarios que entraron como Cliente vean un menú propio,
     * distinto al del administrador, tras iniciar sesión.
     */
    const val HOME_CLIENTE = "home_cliente"

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
    const val CONFIGURACION = "configuracion"
    const val PREFERENCIAS = "preferencias"
    const val DATOS = "datos"
    const val CUENTA = "cuenta"
    const val CLASES = "clases"
    const val CREAR_CLASE = "crear_clase"

    /**
     * CLIENTE_CLASES
     * --------------
     * ✔ TIPO: constante (const val) → String
     * Es la ruta de la pantalla de clases vista desde el perfil Cliente.
     * Sirve para mostrar ClasesScreen en modo solo lectura (esCliente = true),
     * sin crear/configurar clases; de momento muestra un estado vacío con mensaje
     * hasta integrar las inscripciones de Firestore.
     */
    const val CLIENTE_CLASES = "cliente_clases"

    /**
     * MINEGOCIO
     * ---------
     * ✔ TIPO: constante (const val) → String
     * Es la ruta de la pantalla de personalización del negocio (nombre y logo).
     * Sirve para que el administrador configure la identidad del negocio
     * que se muestra en Home y Login.
     */
    const val MINEGOCIO = "mi_negocio"

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
     * ✔ TIPO: función (fun) → String
     * Es la función que genera la ruta completa de la pantalla de modificación de un cliente concreto.
     * Sirve para construir la ruta dinámica concatenando MODIFICARCLIENTE con el id del cliente.
     * Ejemplo de uso: modificarCliente(3) → "modificar_cliente/3"
     */
    fun modificarCliente(idCliente: Int): String {
        return "$MODIFICARCLIENTE/$idCliente"
    }

    /**
     * MIPERFIL
     * --------
     * ✔ TIPO: constante (const val) → String
     * Es la ruta de la pantalla "Mi perfil" del usuario con perfil Cliente.
     * Sirve para que HomeClienteScreen navegue a la ficha propia; esa pantalla decide
     * por sí sola si pedir el registro o mostrar los datos del cliente guardado.
     */
    const val MIPERFIL = "mi_perfil"

    /**
     * REGISTRO_CLIENTE
     * ----------------
     * ✔ TIPO: constante (const val) → String
     * Es la ruta del formulario de registro del propio cliente (alta desde "Mi perfil").
     * Sirve para mostrar AñadirClienteScreen en modo registro, sin los campos exclusivos
     * del administrador; al guardar se registra el id creado como sesión del dispositivo.
     */
    const val REGISTRO_CLIENTE = "registro_cliente"

    /**
     * MODIFICAR_MIPERFIL
     * ------------------
     * ✔ TIPO: constante (const val) → String
     * Es la ruta base de la edición de los propios datos del cliente ("Modificar mis datos").
     * Sirve como prefijo de la ruta dinámica que incluye el id del cliente a modificar,
     * mostrando el mismo formulario pero adaptado al perfil Cliente.
     */
    const val MODIFICAR_MIPERFIL = "modificar_mi_perfil"

    /**
     * modificarMiPerfil
     * -----------------
     * ✔ TIPO: función (fun) → String
     * Es la función que genera la ruta completa de edición de los propios datos.
     * Sirve para construir la ruta dinámica concatenando MODIFICAR_MIPERFIL con el id.
     * Ejemplo de uso: modificarMiPerfil(7) → "modificar_mi_perfil/7"
     */
    fun modificarMiPerfil(idCliente: Int): String {
        return "$MODIFICAR_MIPERFIL/$idCliente"
    }

}
