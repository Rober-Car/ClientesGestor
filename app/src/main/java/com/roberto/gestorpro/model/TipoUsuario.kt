package com.roberto.gestorpro.model

/**
 * TipoUsuario.kt
 * --------------
 * ✔ TIPO: archivo de código fuente Kotlin (enum de tipos de usuario)
 * Es el archivo que define los perfiles con los que se puede usar la aplicación.
 * Sirve para decidir qué menú inicial ve cada usuario (HomeScreen o HomeClienteScreen).
 */

/**
 * TipoUsuario
 * -----------
 * ✔ TIPO: enum (enumerado de Kotlin)
 * Es el enumerado que contiene los perfiles posibles de la app.
 * Sirve para guardar la elección hecha en SeleccionTipoUsuarioScreen en DataStore
 * y para que Login navegue al inicio correspondiente según el perfil elegido.
 */
enum class TipoUsuario {

    /**
     * ADMINISTRADOR
     * -------------
     * ✔ TIPO: constante (valor del enum TipoUsuario)
     * Es el perfil del dueño del negocio.
     * Sirve para mostrar el menú completo (clientes, clases, economía y configuración).
     */
    ADMINISTRADOR,

    /**
     * CLIENTE
     * -------
     * ✔ TIPO: constante (valor del enum TipoUsuario)
     * Es el perfil del cliente del negocio.
     * Sirve para mostrar un menú propio y reducido (mi perfil y clases).
     */
    CLIENTE
}
