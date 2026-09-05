package com.roberto.gestorpro

import com.roberto.gestorpro.util.BajaServicioReglas
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * BajaServicioReglasTest
 * ----------------------
 * Tests de la frontera temporal de la BAJA/DESACTIVACIÓN de una actividad
 * (servicio). Regla existente: al dar de baja se ELIMINAN las sesiones futuras
 * (fecha >= frontera de la baja, inicio del día de la baja) y se CONSERVAN las
 * pasadas (fecha < frontera). El reintento durable reutiliza la MISMA frontera
 * ORIGINAL; nunca se redefine con "ahora".
 *
 * Las operaciones remotas (cascada Firestore) y la persistencia Room requieren
 * infraestructura no disponible en los tests JVM actuales; aquí se prueba la
 * lógica pura de la frontera y las reglas de conservación/eliminación.
 */
class BajaServicioReglasTest {

    // Valores solo ordinales (Long) que representan instantes ordenados.
    private val diaBaja = 5_000L // frontera (inicio del día de la baja)

    @Test
    fun sesion_pasada_se_conserva() {
        // Día 04 (anterior a la frontera) -> se conserva, NO se elimina.
        val sesionPasada = 4_000L
        assertTrue(BajaServicioReglas.esSesionPasadaEnBaja(sesionPasada, diaBaja))
        assertFalse(BajaServicioReglas.esSesionFuturaEnBaja(sesionPasada, diaBaja))
    }

    @Test
    fun sesion_futura_se_elimina() {
        // Días 06, 08 y 10 (posteriores a la frontera) -> se eliminan.
        listOf(6_000L, 8_000L, 10_000L).forEach { fecha ->
            assertTrue(BajaServicioReglas.esSesionFuturaEnBaja(fecha, diaBaja))
            assertFalse(BajaServicioReglas.esSesionPasadaEnBaja(fecha, diaBaja))
        }
    }

    @Test
    fun sesion_del_mismo_dia_de_la_baja_se_considera_futura() {
        // La sesión del día de la baja (fecha == frontera) se elimina (>= desde).
        assertTrue(BajaServicioReglas.esSesionFuturaEnBaja(diaBaja, diaBaja))
        assertFalse(BajaServicioReglas.esSesionPasadaEnBaja(diaBaja, diaBaja))
    }

    @Test
    fun frontera_original_no_se_redefine_con_fecha_posterior() {
        // Reintento un día después: la frontera sigue siendo la ORIGINAL (día 5).
        // Una sesión de la tarde del día de la baja (que era futura) debe seguir
        // eliminándose aunque el reintento ocurra después.
        val sesionDiaDeLaBajaTarde = 5_500L
        assertTrue(BajaServicioReglas.esSesionFuturaEnBaja(sesionDiaDeLaBajaTarde, diaBaja))

        // Una sesión anterior a la frontera NUNCA se elimina, independientemente
        // del día del reintento.
        val sesionDia4 = 4_900L
        assertFalse(BajaServicioReglas.esSesionFuturaEnBaja(sesionDia4, diaBaja))
        assertTrue(BajaServicioReglas.esSesionPasadaEnBaja(sesionDia4, diaBaja))
    }
}
