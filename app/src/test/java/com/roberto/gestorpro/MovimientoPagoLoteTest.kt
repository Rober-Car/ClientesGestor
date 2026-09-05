package com.roberto.gestorpro

import com.roberto.gestorpro.data.entity.MovimientoEntity
import com.roberto.gestorpro.model.EstadoMovimiento
import com.roberto.gestorpro.model.MetodoPago
import com.roberto.gestorpro.util.MovimientoPago
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * MovimientoPagoLoteTest
 * ----------------------
 * Tests de la resolución MASIVA de estado (FASE 4 aplicada a varios
 * movimientos). Reutiliza `MovimientoPago.resolverLote`, que a su vez llama a
 * `MovimientoPago.resolver` (la misma lógica individual del editor): no existe
 * una regla paralela para el cambio masivo.
 */
class MovimientoPagoLoteTest {

    private val ahora = 1_752_000_000_000L

    private fun movimiento(
        id: Int,
        estado: EstadoMovimiento,
        fechaPago: Long? = null,
        metodoPago: MetodoPago? = null
    ): MovimientoEntity = MovimientoEntity(
        idMovimiento = id,
        idCliente = 1,
        fechaInicio = 1_750_000_000_000L,
        fechaFin = 1_758_000_000_000L,
        precioFinal = 30.0,
        estado = estado,
        fechaPago = fechaPago,
        metodoPago = metodoPago
    )

    @Test
    fun pendiente_a_pagado_sin_metodo_fija_fecha() {
        val resultado = MovimientoPago.resolverLote(
            listOf(movimiento(1, EstadoMovimiento.PENDIENTE)),
            pagar = true,
            metodoPago = null,
            ahora = ahora
        )
        assertEquals(1, resultado.size)
        assertEquals(EstadoMovimiento.PAGADO, resultado[0].estado)
        assertEquals(ahora, resultado[0].fechaPago)
        assertNull(resultado[0].metodoPago) // "Sin especificar" no inventa método.
    }

    @Test
    fun pendiente_a_pagado_con_efectivo() {
        val resultado = MovimientoPago.resolverLote(
            listOf(movimiento(1, EstadoMovimiento.PENDIENTE)),
            pagar = true,
            metodoPago = MetodoPago.EFECTIVO,
            ahora = ahora
        )
        assertEquals(MetodoPago.EFECTIVO, resultado[0].metodoPago)
        assertEquals(EstadoMovimiento.PAGADO, resultado[0].estado)
    }

    @Test
    fun pendiente_a_pagado_con_bizum() {
        val resultado = MovimientoPago.resolverLote(
            listOf(movimiento(1, EstadoMovimiento.PENDIENTE)),
            pagar = true,
            metodoPago = MetodoPago.BIZUM,
            ahora = ahora
        )
        assertEquals(MetodoPago.BIZUM, resultado[0].metodoPago)
    }

    @Test
    fun pendiente_a_pagado_con_transferencia() {
        val resultado = MovimientoPago.resolverLote(
            listOf(movimiento(1, EstadoMovimiento.PENDIENTE)),
            pagar = true,
            metodoPago = MetodoPago.TRANSFERENCIA,
            ahora = ahora
        )
        assertEquals(MetodoPago.TRANSFERENCIA, resultado[0].metodoPago)
    }

    @Test
    fun pagado_a_pendiente_limpia_fecha_y_metodo() {
        val resultado = MovimientoPago.resolverLote(
            listOf(movimiento(1, EstadoMovimiento.PAGADO, ahora, MetodoPago.BIZUM)),
            pagar = false,
            metodoPago = null,
            ahora = ahora
        )
        assertEquals(1, resultado.size)
        assertEquals(EstadoMovimiento.PENDIENTE, resultado[0].estado)
        assertNull(resultado[0].fechaPago)
        assertNull(resultado[0].metodoPago)
    }

    @Test
    fun ya_pagado_no_se_incluye_al_marcar_pagados() {
        // "Marcar como pagados" con un ya PAGADO: no se toca ni se sobrescribe
        // su método, aunque se elija "Sin especificar" (metodoPago = null).
        val yaPagado = movimiento(1, EstadoMovimiento.PAGADO, ahora, MetodoPago.TRANSFERENCIA)
        val resultado = MovimientoPago.resolverLote(
            listOf(yaPagado),
            pagar = true,
            metodoPago = null,
            ahora = ahora
        )
        assertTrue(resultado.isEmpty())
        assertEquals(MetodoPago.TRANSFERENCIA, yaPagado.metodoPago)
        assertEquals(ahora, yaPagado.fechaPago)
    }

    @Test
    fun ya_pendiente_no_se_incluye_al_marcar_pendientes() {
        val yaPendiente = movimiento(1, EstadoMovimiento.PENDIENTE)
        val resultado = MovimientoPago.resolverLote(
            listOf(yaPendiente),
            pagar = false,
            metodoPago = null,
            ahora = ahora
        )
        assertTrue(resultado.isEmpty())
        assertEquals(EstadoMovimiento.PENDIENTE, yaPendiente.estado)
    }

    @Test
    fun mixto_marcar_pagados_solo_transforma_los_pendientes() {
        val pendiente = movimiento(1, EstadoMovimiento.PENDIENTE)
        val yaPagado = movimiento(2, EstadoMovimiento.PAGADO, ahora, MetodoPago.EFECTIVO)

        val resultado = MovimientoPago.resolverLote(
            listOf(pendiente, yaPagado),
            pagar = true,
            metodoPago = MetodoPago.BIZUM,
            ahora = ahora
        )

        // Solo se transforma el que estaba PENDIENTE (id 1) -> PAGADO con BIZUM.
        assertEquals(1, resultado.size)
        assertEquals(1, resultado[0].idMovimiento)
        assertEquals(EstadoMovimiento.PAGADO, resultado[0].estado)
        assertEquals(MetodoPago.BIZUM, resultado[0].metodoPago)

        // El ya PAGADO conserva su método EFECTIVO original (no se inventa/sobrescribe).
        assertEquals(EstadoMovimiento.PAGADO, yaPagado.estado)
        assertEquals(MetodoPago.EFECTIVO, yaPagado.metodoPago)
    }

    @Test
    fun mixto_marcar_pendientes_solo_transforma_los_pagados() {
        val pendiente = movimiento(1, EstadoMovimiento.PENDIENTE)
        val pagado = movimiento(2, EstadoMovimiento.PAGADO, ahora, MetodoPago.TRANSFERENCIA)

        val resultado = MovimientoPago.resolverLote(
            listOf(pendiente, pagado),
            pagar = false,
            metodoPago = null,
            ahora = ahora
        )

        assertEquals(1, resultado.size)
        assertEquals(2, resultado[0].idMovimiento)
        assertEquals(EstadoMovimiento.PENDIENTE, resultado[0].estado)
        assertNull(resultado[0].fechaPago)
        assertNull(resultado[0].metodoPago)

        // El que ya estaba PENDIENTE se conserva tal cual.
        assertEquals(EstadoMovimiento.PENDIENTE, pendiente.estado)
        assertNull(pendiente.fechaPago)
    }
}
