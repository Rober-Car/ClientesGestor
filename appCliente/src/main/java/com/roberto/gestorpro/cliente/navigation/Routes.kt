package com.roberto.gestorpro.cliente.navigation

/**
 * Routes
 * ------
 * ✔ TIPO: object
 * Centraliza las rutas de navegación de GestorPro Cliente.
 */
object Routes {

    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val RECUPERAR_PASSWORD = "recuperar_password"

    /**
     * INICIO
     * ------
     * Pantalla "¿Tu gimnasio ya te ha registrado?" (código maestro + DNI).
     */
    const val INICIO = "inicio"

    /**
     * COMPLETAR_PERFIL
     * ----------------
     * Formulario de datos personales del CLIENTE sin negocio (VÍA 2).
     */
    const val COMPLETAR_PERFIL = "completar_perfil"

    /**
     * HOME
     * ----
     * Menú principal del cliente ya vinculado.
     */
    const val HOME = "home"

    /**
     * MI_PERFIL
     * ---------
     * Ver y editar los datos personales de la propia ficha.
     */
    const val MI_PERFIL = "mi_perfil"

    /**
     * EDITAR_PERFIL
     * -------------
     * Formulario de edición de los datos personales de la propia ficha.
     */
    const val EDITAR_PERFIL = "editar_perfil"

    /**
     * CUENTA
     * ------
     * Cerrar sesión / recuperar contraseña.
     */
    const val CUENTA = "cuenta"
}
