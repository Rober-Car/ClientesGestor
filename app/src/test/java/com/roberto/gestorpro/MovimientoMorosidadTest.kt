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
 * Tests unitarios del motor de deuda y morosidad (modelo económico definitivo,
 * F2). Regla de deuda: suma de TODOS los PENDIENTES (sin filtrar por fechaFin).
 * Dos causas de morosidad: por deuda y por fecha. fechaEntradaMorosidad = fecha
 * actual (ahora) de detección, nunca fechaFin. Soporta exentoMorosidad.
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

    // =========================================================
    // DEUDA (suma de TODOS los PENDIENTES, sin filtrar fechaFin)
    // =========================================================

    // 1. Un PENDIENTE genera deuda (aunque su período no haya terminado).
    @Test
    fun pendiente_no_vencido_genera_deuda() {
        val mov = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PENDIENTE)
        assertEquals(30.0, MovimientoMorosidad.deudaDe(listOf(mov)), 0.0001)
    }

    // 2. PENDIENTE con fechaFin futura también cuenta.
    @Test
    fun pendiente_futuro_cuenta_como_deuda() {
        val mov = movimiento(1, 1, fecha(1, 10), fecha(31, 10), 30.0, EstadoMovimiento.PENDIENTE)
        assertEquals(30.0, MovimientoMorosidad.deudaDe(listOf(mov)), 0.0001)
    }

    // 3. PAGADO no genera deuda.
    @Test
    fun pagado_no_genera_deuda() {
        val mov = movimiento(1, 1, fecha(1, 8), fecha(31, 8), 30.0, EstadoMovimiento.PAGADO)
        assertEquals(0.0, MovimientoMorosidad.deudaDe(listOf(mov)), 0.0001)
    }

    // 4. Varios PENDIENTES suman (vencidos o futuros, da igual).
    @Test
    fun varios_pendientes_suman() {
        val vencido = movimiento(1, 1, fecha(1, 8), fecha(31, 8), 30.0, EstadoMovimiento.PENDIENTE)
        val futuro = movimiento(2, 1, fecha(1, 10), fecha(31, 10), 20.0, EstadoMovimiento.PENDIENTE)
        assertEquals(50.0, MovimientoMorosidad.deudaDe(listOf(vencido, futuro)), 0.0001)
    }

    // 5. Sin PENDIENTES -> deuda cero.
    @Test
    fun sin_pendientes_deuda_cero() {
        val pagado = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PAGADO)
        assertEquals(0.0, MovimientoMorosidad.deudaDe(listOf(pagado)), 0.0001)
    }

    // =========================================================
    // MOROSIDAD POR DEUDA
    // =========================================================

    // 6. ACTIVO + PENDIENTE (aunque no vencido) -> moroso por deuda.
    @Test
    fun activo_con_pendiente_moroso_por_deuda() {
        val mov = movimiento(1, 1, fecha(1, 10), fecha(31, 10), 30.0, EstadoMovimiento.PENDIENTE)
        val res = MovimientoMorosidad.resultadoDe(EstadoCliente.ACTIVO, listOf(mov), ahora)
        assertTrue(res.moroso)
        assertTrue(res.morosoPorDeuda)
        assertEquals(30.0, res.deuda, 0.0001)
    }

    // 7. BAJA + PENDIENTE -> moroso por deuda.
    @Test
    fun baja_con_pendiente_moroso_por_deuda() {
        val mov = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PENDIENTE)
        val res = MovimientoMorosidad.resultadoDe(EstadoCliente.BAJA, listOf(mov), ahora)
        assertTrue(res.moroso)
        assertTrue(res.morosoPorDeuda)
        assertFalse(res.morosoPorFecha)
        assertEquals(30.0, res.deuda, 0.0001)
    }

    // =========================================================
    // MOROSIDAD POR FECHA (solo ACTIVO)
    // =========================================================

    // 8. ACTIVO + período PAGADO terminado + sin nueva cobertura:
    //    deuda = 0, moroso = true (por fecha).
    @Test
    fun activo_periodo_terminado_sin_nueva_cobertura_moroso_por_fecha() {
        val pagado = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PAGADO)
        val ahoraOctubre = fecha(2, 10)
        val res = MovimientoMorosidad.resultadoDe(EstadoCliente.ACTIVO, listOf(pagado), ahoraOctubre)
        assertTrue(res.moroso)
        assertFalse(res.morosoPorDeuda)
        assertTrue(res.morosoPorFecha)
        assertEquals(0.0, res.deuda, 0.0001)
    }

    // 9. BAJA + período PAGADO terminado sin deuda -> NO moroso.
    @Test
    fun baja_periodo_terminado_sin_deuda_no_moroso() {
        val pagado = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PAGADO)
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.BAJA, listOf(pagado), fecha(2, 10)
        )
        assertFalse(res.moroso)
        assertEquals(0.0, res.deuda, 0.0001)
    }

    // 10. ACTIVO con cobertura PAGADA vigente -> no moroso.
    @Test
    fun activo_con_cobertura_pagada_vigente_no_moroso() {
        val pagado = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PAGADO)
        val res = MovimientoMorosidad.resultadoDe(EstadoCliente.ACTIVO, listOf(pagado), ahora)
        assertFalse(res.moroso)
        assertEquals(0.0, res.deuda, 0.0001)
    }

    // =========================================================
    // TRANSICIONES
    // =========================================================

    // 11. Pago completo de la deuda pero período terminado -> sigue moroso por fecha.
    @Test
    fun paga_deuda_sin_renovar_sigue_moroso_por_fecha() {
        val pagado = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PAGADO)
        // El PENDIENTE antiguo ya se pagó; solo queda el período terminado sin renovar.
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.ACTIVO, listOf(pagado), fecha(2, 10)
        )
        assertTrue(res.moroso)
        assertFalse(res.morosoPorDeuda)
        assertTrue(res.morosoPorFecha)
        assertEquals(0.0, res.deuda, 0.0001)
    }

    // 12. Nuevo período PAGADO que cubre hoy -> deja de ser moroso por fecha.
    @Test
    fun renovacion_con_periodo_pagado_vigente_deja_de_ser_moroso() {
        val vencido = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PAGADO)
        val nuevo = movimiento(2, 1, fecha(1, 10), fecha(31, 10), 30.0, EstadoMovimiento.PAGADO)
        val ahoraOctubre = fecha(2, 10)
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.ACTIVO, listOf(vencido, nuevo), ahoraOctubre
        )
        assertFalse(res.moroso)
        assertEquals(0.0, res.deuda, 0.0001)
    }

    // 13. REGISTRADO / ARCHIVADO: nunca moroso por fecha, aunque termine el período.
    @Test
    fun registrado_o_archivado_no_moroso_por_fecha() {
        val pagado = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PAGADO)
        for (estado in listOf(EstadoCliente.REGISTRADO, EstadoCliente.ARCHIVADO)) {
            val res = MovimientoMorosidad.resultadoDe(
                estado, listOf(pagado), fecha(2, 10)
            )
            assertFalse(res.moroso)
        }
    }

    // 14. ACTIVO sin ningún movimiento -> no moroso (situación válida, sin servicios).
    @Test
    fun activo_sin_movimientos_no_moroso() {
        val res = MovimientoMorosidad.resultadoDe(EstadoCliente.ACTIVO, emptyList(), ahora)
        assertFalse(res.moroso)
        assertEquals(0.0, res.deuda, 0.0001)
    }

    // =========================================================
    // EXENTO DE MOROSIDAD
    // =========================================================

    // 15. exentoMorosidad = true -> moroso=false, deuda intacta.
    @Test
    fun exento_true_oculta_morosidad_pero_conserva_deuda() {
        val pendiente = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 40.0, EstadoMovimiento.PENDIENTE)
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.ACTIVO, listOf(pendiente), exentoMorosidad = true, ahora = ahora
        )
        assertFalse(res.moroso)
        assertEquals(40.0, res.deuda, 0.0001)
    }

    // 16. exentoMorosidad = false -> cálculo normal.
    @Test
    fun exento_false_calculo_normal() {
        val pendiente = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 40.0, EstadoMovimiento.PENDIENTE)
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.ACTIVO, listOf(pendiente), exentoMorosidad = false, ahora = ahora
        )
        assertTrue(res.moroso)
        assertEquals(40.0, res.deuda, 0.0001)
    }

    // 17. Exento sobre morosidad por fecha.
    @Test
    fun exento_true_tambien_oculta_morosidad_por_fecha() {
        val pagado = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PAGADO)
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.ACTIVO, listOf(pagado), exentoMorosidad = true, ahora = fecha(2, 10)
        )
        assertFalse(res.moroso)
        assertEquals(0.0, res.deuda, 0.0001)
    }

    // =========================================================
    // fechaEntradaMorosidad
    // =========================================================

    // 18. Pasa de no moroso a moroso -> fechaEntradaMorosidad = ahora (detección).
    @Test
    fun entrada_usa_ahora_no_fechafin() {
        val pendiente = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PENDIENTE)
        val instanteEntrada = fecha(15, 9)
        val final = MovimientoMorosidad.resultadoFinal(
            EstadoCliente.ACTIVO, listOf(pendiente), morosoPrevio = false,
            fechaEntradaPrevia = null, ahora = instanteEntrada
        )
        assertTrue(final.moroso)
        assertEquals(instanteEntrada, final.fechaEntradaMorosidad)
    }

    // 19. Sigue moroso -> conserva la fecha de entrada.
    @Test
    fun recalcular_moroso_conserva_fecha_entrada() {
        val pendiente = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PENDIENTE)
        val entrada = fecha(15, 9)
        val final = MovimientoMorosidad.resultadoFinal(
            EstadoCliente.ACTIVO, listOf(pendiente), morosoPrevio = true,
            fechaEntradaPrevia = entrada, ahora = fecha(20, 9)
        )
        assertTrue(final.moroso)
        assertEquals(entrada, final.fechaEntradaMorosidad)
    }

    // 20. Deja de ser moroso -> fechaEntradaMorosidad = null.
    @Test
    fun sale_de_morosidad_limpia_fecha() {
        val pagado = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PAGADO)
        val final = MovimientoMorosidad.resultadoFinal(
            EstadoCliente.ACTIVO, listOf(pagado), morosoPrevio = true,
            fechaEntradaPrevia = fecha(31, 8), ahora = ahora
        )
        assertFalse(final.moroso)
        assertNull(final.fechaEntradaMorosidad)
    }

    // 21. Vuelve a entrar -> nueva fecha de entrada.
    @Test
    fun vuelve_a_entrar_establece_nueva_fecha() {
        val pendiente = movimiento(1, 1, fecha(1, 10), fecha(31, 10), 30.0, EstadoMovimiento.PENDIENTE)
        val reentrada = fecha(5, 10)
        val final = MovimientoMorosidad.resultadoFinal(
            EstadoCliente.ACTIVO, listOf(pendiente), morosoPrevio = false,
            fechaEntradaPrevia = null, ahora = reentrada
        )
        assertTrue(final.moroso)
        assertEquals(reentrada, final.fechaEntradaMorosidad)
    }

    // 22. exentoMorosidad = true en resultadoFinal -> moroso=false y fecha null.
    @Test
    fun exento_true_en_resultado_final_limpia_moroso_y_fecha() {
        val pendiente = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PENDIENTE)
        val final = MovimientoMorosidad.resultadoFinal(
            EstadoCliente.ACTIVO, listOf(pendiente), exentoMorosidad = true,
            morosoPrevio = true, fechaEntradaPrevia = fecha(1, 9), ahora = ahora
        )
        assertFalse(final.moroso)
        assertNull(final.fechaEntradaMorosidad)
    }
}
