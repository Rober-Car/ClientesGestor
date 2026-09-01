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
     * Menú principal del cliente (vinculado o sin vincular).
     */
    const val HOME = "home"

    /**
     * CLASES
     * ------
     * Apartado de clases/sesiones del cliente.
     */
    const val CLASES = "clases"

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

    /**
     * CONFIGURACION
     * -------------
     * Ajustes de la aplicación (tema claro/oscuro).
     */
    const val CONFIGURACION = "configuracion"

    /**
     * NOTIFICACIONES
     * --------------
     * Configuración visual temporal de avisos del cliente.
     */
    const val NOTIFICACIONES = "notificaciones"

    /**
     * CONFIGURACION_NOTIFICACIONES
     * -----------------------------
     * Configuración visual temporal de los avisos del cliente.
     */
    const val CONFIGURACION_NOTIFICACIONES = "configuracion_notificaciones"

    /**
     * RUTINAS
     * -------
     * Apartado visual de rutinas de entrenamiento.
     */
    const val RUTINAS = "rutinas"

    /** Destino visual provisional de la política de privacidad. */
    const val POLITICA_PRIVACIDAD = "politica_privacidad"

    /** Destino visual provisional de los términos y condiciones. */
    const val TERMINOS_CONDICIONES = "terminos_condiciones"
}
