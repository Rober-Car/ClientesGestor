package com.roberto.gestorpro.util

/**
 * BajaServicioReglas
 * ------------------
 * Regla pura de frontera temporal de la BAJA/DESACTIVACIÓN de una actividad
 * (servicio). Encapsula la semántica existente de sesión pasada/futura que usan
 * la cascada local de Room y la consulta remota de Firestore:
 *
 *  - Sesión FUTURA (se elimina al dar de baja): fecha >= desde (desde = inicio
 *    del día en que se produce la baja). Se conserva la misma frontera ORIGINAL
 *    de la baja en los reintentos, nunca se recalcula con "ahora".
 *  - Sesión PASADA (se conserva): fecha < desde.
 */
object BajaServicioReglas {

    /**
     * ¿La sesión (por su fecha) debe eliminarse en la baja? Es "futura" si su
     * fecha es igual o posterior a la frontera original de la baja.
     */
    fun esSesionFuturaEnBaja(fechaSesion: Long, desdeBaja: Long): Boolean =
        fechaSesion >= desdeBaja

    /**
     * ¿La sesión es pasada respecto a la baja y debe conservarse?
     */
    fun esSesionPasadaEnBaja(fechaSesion: Long, desdeBaja: Long): Boolean =
        fechaSesion < desdeBaja
}
