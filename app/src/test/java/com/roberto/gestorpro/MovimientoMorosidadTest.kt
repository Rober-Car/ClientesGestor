package com.roberto.gestorpro

import com.roberto.gestorpro.data.entity.MovimientoEntity
import com.roberto.gestorpro.model.EstadoCliente
import com.roberto.gestorpro.model.EstadoMovimiento
import com.roberto.gestorpro.util.MovimientoMorosidad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * MovimientoMorosidadTest
 * -----------------------
 * Tests unitarios de la FASE 5 (deuda y morosidad). Cubren los casos 1-19
 * de la especificación.
 */
class MovimientoMorosidadTest {

    // Instante de referencia: 15/09/2026.
    private val ahora: Long = fecha(15, 9)

    private fun movimiento(
        id: Int,
        idCliente: Int = 1,
        fechaInicio: Long,
        fechaFin: Long,
        precio: Double,
        estado: EstadoMovimiento
    ): MovimientoEntity = MovimientoEntity(
        idMovimiento = id,
        idCliente = idCliente,
        servicios = listOf(1),
        fechaInicio = fechaInicio,
        fechaFin = fechaFin,
        precioFinal = precio,
        estado = estado,
        fechaPago = if (estado == EstadoMovimiento.PAGADO) fechaFin else null,
        metodoPago = null,
        observaciones = null
    )

    private fun fecha(dia: Int, mes: Int, anio: Int = 2026): Long =
        java.time.LocalDate.of(anio, mes, dia)
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli()

    // 1. PENDIENTE cuyo periodo aún no ha terminado -> no deuda.
    @Test
    fun pendiente_no_vencido_no_genera_deuda() {
        val mov = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PENDIENTE)
        assertEquals(0.0, MovimientoMorosidad.deudaDe(listOf(mov), ahora), 0.0001)
        assertFalse(MovimientoMorosidad.esExigible(mov, ahora))
    }

    // 2. PENDIENTE cuyo periodo ya ha terminado -> deuda.
    @Test
    fun pendiente_vencido_genera_deuda() {
        val mov = movimiento(1, 1, fecha(1, 8), fecha(31, 8), 30.0, EstadoMovimiento.PENDIENTE)
        assertEquals(30.0, MovimientoMorosidad.deudaDe(listOf(mov), ahora), 0.0001)
    }

    // 3. PAGADO no genera deuda.
    @Test
    fun pagado_no_genera_deuda() {
        val mov = movimiento(1, 1, fecha(1, 8), fecha(31, 8), 30.0, EstadoMovimiento.PAGADO)
        assertEquals(0.0, MovimientoMorosidad.deudaDe(listOf(mov), ahora), 0.0001)
    }

    // 4. Dos PENDIENTES exigibles -> suma.
    @Test
    fun dos_pendientes_exigibles_suman() {
        val a = movimiento(1, 1, fecha(1, 7), fecha(31, 7), 30.0, EstadoMovimiento.PENDIENTE)
        val b = movimiento(2, 1, fecha(1, 8), fecha(31, 8), 20.0, EstadoMovimiento.PENDIENTE)
        assertEquals(50.0, MovimientoMorosidad.deudaDe(listOf(a, b), ahora), 0.0001)
    }

    // 5. Movimiento futuro PENDIENTE no genera deuda.
    @Test
    fun pendiente_futuro_no_genera_deuda() {
        val mov = movimiento(1, 1, fecha(1, 10), fecha(31, 10), 30.0, EstadoMovimiento.PENDIENTE)
        assertEquals(0.0, MovimientoMorosidad.deudaDe(listOf(mov), ahora), 0.0001)
    }

    // 6. ACTIVO con periodo PAGADO vigente -> no moroso.
    @Test
    fun activo_con_cobertura_pagada_vigente_no_moroso() {
        val pagado = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PAGADO)
        val res = MovimientoMorosidad.resultadoDe(EstadoCliente.ACTIVO, listOf(pagado), ahora)
        assertFalse(res.moroso)
        assertEquals(0.0, res.deuda, 0.0001)
    }

    // 7 y 8. ACTIVO cuyo periodo PAGADO terminó sin continuidad -> moroso (aunque no exista siguiente movimiento).
    @Test
    fun activo_periodo_pagado_terminado_sin_continuidad_moroso() {
        val pagado = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PAGADO)
        // Sin crear ningún movimiento de octubre: ahora es 02/10.
        val ahoraOctubre = fecha(2, 10)
        val res = MovimientoMorosidad.resultadoDe(EstadoCliente.ACTIVO, listOf(pagado), ahoraOctubre)
        assertTrue(res.moroso)
    }

    // 9. fechaEntradaMorosidad = fechaFin del periodo que provoca la entrada.
    @Test
    fun fecha_entrada_es_fechafin_del_periodo() {
        val pagado = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PAGADO)
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.ACTIVO, listOf(pagado), fecha(2, 10)
        )
        assertTrue(res.moroso)
        assertEquals(fecha(30, 9), res.fechaEntradaSugerida)
    }

    // 10. Recalcular un cliente ya moroso no cambia fechaEntradaMorosidad.
    @Test
    fun recalcular_moroso_conserva_fecha_entrada() {
        val pagado = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PAGADO)
        val final1 = MovimientoMorosidad.resultadoFinal(
            EstadoCliente.ACTIVO, listOf(pagado), false, null, fecha(2, 10)
        )
        assertTrue(final1.moroso)
        val entrada = final1.fechaEntradaMorosidad

        // Recalcular días después, sigue moroso: conserva la misma fecha.
        val final2 = MovimientoMorosidad.resultadoFinal(
            EstadoCliente.ACTIVO, listOf(pagado), true, entrada, fecha(5, 10)
        )
        assertTrue(final2.moroso)
        assertEquals(entrada, final2.fechaEntradaMorosidad)
    }

    // 11 y 13. BAJA sin deuda y sin siguiente movimiento -> no moroso.
    @Test
    fun baja_sin_deuda_no_moroso_por_ausencia_de_movimiento() {
        val pagado = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PAGADO)
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.BAJA, listOf(pagado), fecha(2, 10)
        )
        assertFalse(res.moroso)
        assertEquals(0.0, res.deuda, 0.0001)
    }

    // 12 y 13. BAJA con deuda -> moroso.
    @Test
    fun baja_con_deuda_moroso() {
        val pendiente = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PENDIENTE)
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.BAJA, listOf(pendiente), fecha(2, 10)
        )
        assertTrue(res.moroso)
        assertEquals(30.0, res.deuda, 0.0001)
    }

    // 14. Pagar toda la deuda de un BAJA -> no moroso.
    @Test
    fun baja_paga_toda_la_deuda_no_moroso() {
        val pagado = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PAGADO)
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.BAJA, listOf(pagado), fecha(2, 10)
        )
        assertFalse(res.moroso)
        assertNull(res.fechaEntradaSugerida)
    }

    // 15. ACTIVO paga deuda y tiene cobertura PAGADA actual -> no moroso.
    @Test
    fun activo_sin_deuda_con_cobertura_actual_no_moroso() {
        val pagado = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PAGADO)
        val res = MovimientoMorosidad.resultadoDe(EstadoCliente.ACTIVO, listOf(pagado), ahora)
        assertFalse(res.moroso)
    }

    // 16. Movimiento futuro PENDIENTE no provoca morosidad por sí solo.
    @Test
    fun pendiente_futuro_no_provoca_morosidad() {
        val futuro = movimiento(1, 1, fecha(1, 10), fecha(31, 10), 30.0, EstadoMovimiento.PENDIENTE)
        val res = MovimientoMorosidad.resultadoDe(EstadoCliente.ACTIVO, listOf(futuro), ahora)
        assertFalse(res.moroso)
    }

    // 17. Uno exigible + uno futuro: solo cuenta el exigible.
    @Test
    fun solo_exigible_cuenta_en_deuda() {
        val vencido = movimiento(1, 1, fecha(1, 8), fecha(31, 8), 30.0, EstadoMovimiento.PENDIENTE)
        val futuro = movimiento(2, 1, fecha(1, 10), fecha(31, 10), 30.0, EstadoMovimiento.PENDIENTE)
        assertEquals(30.0, MovimientoMorosidad.deudaDe(listOf(vencido, futuro), ahora), 0.0001)
    }

    // 18. Cliente que sale de morosidad -> fechaEntradaMorosidad = null.
    @Test
    fun sale_de_morosidad_limpia_fecha() {
        val pagadoActual = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PAGADO)
        val final = MovimientoMorosidad.resultadoFinal(
            EstadoCliente.ACTIVO,
            listOf(pagadoActual),
            morosoPrevio = true,
            fechaEntradaPrevia = fecha(31, 8),
            ahora = ahora
        )
        assertFalse(final.moroso)
        assertNull(final.fechaEntradaMorosidad)
    }

    // 19. Cliente que vuelve a entrar -> nueva fecha de entrada.
    @Test
    fun vuelve_a_entrar_establece_nueva_fecha() {
        val vencido = movimiento(1, 1, fecha(1, 8), fecha(31, 8), 30.0, EstadoMovimiento.PENDIENTE)
        val final = MovimientoMorosidad.resultadoFinal(
            EstadoCliente.BAJA,
            listOf(vencido),
            morosoPrevio = false,
            fechaEntradaPrevia = null,
            ahora = fecha(15, 9)
        )
        assertTrue(final.moroso)
        assertEquals(fecha(31, 8), final.fechaEntradaMorosidad)
    }
}
