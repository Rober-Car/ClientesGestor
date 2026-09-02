package com.roberto.gestorpro

import com.roberto.gestorpro.model.EstadoMovimiento
import com.roberto.gestorpro.model.MetodoPago
import com.roberto.gestorpro.util.MovimientoPago
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * MovimientoPagoTest
 * ------------------
 * Tests unitarios de la FASE 4 (pago dentro de MovimientoEntity).
 */
class MovimientoPagoTest {

    private val ahora = 1_752_000_000_000L

    @Test
    fun pendiente_a_pagado_establece_fechaPago() {
        val resultado = MovimientoPago.resolver(
            nuevoPagado = true,
            eraPagado = false,
            fechaPagoElegida = null,
            metodoPago = null,
            ahora = ahora
        )
        assertEquals(EstadoMovimiento.PAGADO, resultado.estado)
        assertEquals(ahora, resultado.fechaPago)
    }

    @Test
    fun pendiente_a_pagado_sin_metodo_es_valido() {
        val resultado = MovimientoPago.resolver(true, false, null, null, ahora)
        assertEquals(EstadoMovimiento.PAGADO, resultado.estado)
        assertEquals(ahora, resultado.fechaPago)
        assertNull(resultado.metodoPago)
    }

    @Test
    fun pendiente_a_pagado_con_efectivo() {
        val resultado = MovimientoPago.resolver(true, false, null, MetodoPago.EFECTIVO, ahora)
        assertEquals(MetodoPago.EFECTIVO, resultado.metodoPago)
        assertEquals(ahora, resultado.fechaPago)
    }

    @Test
    fun pendiente_a_pagado_con_bizum() {
        val resultado = MovimientoPago.resolver(true, false, null, MetodoPago.BIZUM, ahora)
        assertEquals(MetodoPago.BIZUM, resultado.metodoPago)
    }

    @Test
    fun pendiente_a_pagado_con_transferencia() {
        val resultado = MovimientoPago.resolver(true, false, null, MetodoPago.TRANSFERENCIA, ahora)
        assertEquals(MetodoPago.TRANSFERENCIA, resultado.metodoPago)
    }

    @Test
    fun pagado_a_pendiente_limpia_fechaPago() {
        val resultado = MovimientoPago.resolver(false, true, ahora, MetodoPago.BIZUM, ahora)
        assertEquals(EstadoMovimiento.PENDIENTE, resultado.estado)
        assertNull(resultado.fechaPago)
    }

    @Test
    fun pagado_a_pendiente_limpia_metodoPago() {
        val resultado = MovimientoPago.resolver(false, true, ahora, MetodoPago.BIZUM, ahora)
        assertNull(resultado.metodoPago)
    }

    @Test
    fun editar_pagado_conserva_fechaPago() {
        val fechaExistente = 1_750_000_000_000L
        val resultado = MovimientoPago.resolver(
            nuevoPagado = true,
            eraPagado = true,
            fechaPagoElegida = fechaExistente,
            metodoPago = null,
            ahora = ahora
        )
        assertEquals(fechaExistente, resultado.fechaPago)
    }

    @Test
    fun editar_pagado_conserva_metodoPago() {
        val fechaExistente = 1_750_000_000_000L
        val resultado = MovimientoPago.resolver(
            nuevoPagado = true,
            eraPagado = true,
            fechaPagoElegida = fechaExistente,
            metodoPago = MetodoPago.BIZUM,
            ahora = ahora
        )
        assertEquals(MetodoPago.BIZUM, resultado.metodoPago)
        assertEquals(fechaExistente, resultado.fechaPago)
    }

    @Test
    fun una_transicion_manual_a_pagado_nunca_deja_sin_fechaPago() {
        // Sin fecha elegida y sin método: la transición PENDIENTE -> PAGADO
        // siempre rellena fechaPago con "ahora".
        val resultado = MovimientoPago.resolver(true, false, null, null, ahora)
        assertEquals(EstadoMovimiento.PAGADO, resultado.estado)
        assertEquals(ahora, resultado.fechaPago)
    }

    @Test
    fun metodo_pago_no_es_obligatorio() {
        assertNull(MovimientoPago.metodoPagoDe(null))
        assertEquals("Sin especificar", MovimientoPago.metodoPagoLabel(null))
    }

    @Test
    fun metodoPagoDe_mapa_correcto() {
        assertEquals(MetodoPago.EFECTIVO, MovimientoPago.metodoPagoDe("EFECTIVO"))
        assertEquals(MetodoPago.BIZUM, MovimientoPago.metodoPagoDe("BIZUM"))
        assertEquals(MetodoPago.TRANSFERENCIA, MovimientoPago.metodoPagoDe("TRANSFERENCIA"))
        assertNull(MovimientoPago.metodoPagoDe("INEXISTENTE"))
    }
}
