package com.roberto.gestorpro

import com.roberto.gestorpro.data.firebase.BajaClienteRemotoRepository
import com.roberto.gestorpro.data.firebase.NotificacionRemotoRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * NotificacionConfigTest
 * ----------------------
 * Tests unitarios de la regla de negocio de la notificación de BAJA CONFIRMADA:
 * - la configuración inexistente se trata como ACTIVA por defecto;
 * - solo un false explícito la desactiva;
 * - el ID determinista evita duplicados.
 * No requieren Firebase: prueban la lógica pura de los repositorios.
 */
class NotificacionConfigTest {

    /** Configuración inexistente -> BAJA_CONFIRMADA se genera por defecto. */
    @Test
    fun configuracionPorDefecto_bajaConfirmadaActiva() {
        val config = NotificacionRemotoRepository.configuracionPorDefecto()
        assertTrue(config.bajaConfirmadaActiva)
    }

    /** Sin valor de configuración (campo ausente) -> activa por defecto. */
    @Test
    fun bajaConfirmadaSinValor_esActivaPorDefecto() {
        assertTrue(NotificacionRemotoRepository.bajaConfirmadaActivaPorDefecto(null))
    }

    /** Configuración con bajaConfirmada.activa = true -> se genera. */
    @Test
    fun bajaConfirmadaActiva_explicitaTrue_seGenera() {
        assertTrue(NotificacionRemotoRepository.bajaConfirmadaActivaPorDefecto(true))
    }

    /** Configuración con bajaConfirmada.activa = false -> NO se genera. */
    @Test
    fun bajaConfirmadaActiva_explicitaFalse_noSeGenera() {
        assertFalse(NotificacionRemotoRepository.bajaConfirmadaActivaPorDefecto(false))
    }

    /** El ID determinista es idéntico para la misma ficha y fecha de baja. */
    @Test
    fun idBajaConfirmada_esDeterminista() {
        val id1 = BajaClienteRemotoRepository.idNotificacionBajaConfirmada(7, 1_700_000_000_000L)
        val id2 = BajaClienteRemotoRepository.idNotificacionBajaConfirmada(7, 1_700_000_000_000L)
        val idOtro = BajaClienteRemotoRepository.idNotificacionBajaConfirmada(8, 1_700_000_000_000L)
        assertEquals(id1, id2)
        assertNotEquals(id1, idOtro)
    }
}
