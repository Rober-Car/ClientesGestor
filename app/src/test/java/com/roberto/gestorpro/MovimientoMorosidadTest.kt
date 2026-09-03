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

    // =========================================================
    // REINSCRIPCIÓN / NUEVA ETAPA (inicioEtapa = fecha de la última baja)
    // Casos límite: BAJA de un año -> reactivación -> nuevo período.
    // Solo cuentan los PAGADO con fechaFin >= inicioEtapa.
    // =========================================================

    // 23. Caso 1: reactivar hoy con un nuevo PAGADO que empieza hoy -> NO moroso.
    @Test
    fun reinscripcion_pagado_empieza_hoy_no_moroso() {
        val antiguo = movimiento(1, 1, fecha(1, 1, 2025), fecha(31, 1, 2025), 30.0, EstadoMovimiento.PAGADO)
        val nuevo = movimiento(2, 1, fecha(1, 7, 2026), fecha(31, 7, 2026), 40.0, EstadoMovimiento.PAGADO)
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.ACTIVO, listOf(antiguo, nuevo), fecha(10, 7),
            inicioEtapa = fecha(1, 7)
        )
        assertFalse(res.moroso)
        assertFalse(res.morosoPorFecha)
        assertEquals(0.0, res.deuda, 0.0001)
    }

    // 24. Caso 2: el nuevo PAGADO empieza mañana (aún sin cobertura) -> NO moroso.
    @Test
    fun reinscripcion_pagado_empieza_manana_no_moroso() {
        val antiguo = movimiento(1, 1, fecha(1, 1, 2025), fecha(31, 1, 2025), 30.0, EstadoMovimiento.PAGADO)
        val nuevo = movimiento(2, 1, fecha(1, 8), fecha(31, 8), 40.0, EstadoMovimiento.PAGADO)
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.ACTIVO, listOf(antiguo, nuevo), fecha(10, 7),
            inicioEtapa = fecha(1, 7)
        )
        assertFalse(res.moroso)
        assertFalse(res.morosoPorFecha)
        assertEquals(0.0, res.deuda, 0.0001)
    }

    // 25. Caso 3: el nuevo PAGADO tiene fechaInicio de ayer (retrasada) pero
    //    termina después de fechaAlta -> cuenta como cobertura (filtro por fechaFin).
    @Test
    fun reinscripcion_pagado_con_inicio_retrasado_cuenta_por_fechafin() {
        val antiguo = movimiento(1, 1, fecha(1, 1, 2025), fecha(31, 1, 2025), 30.0, EstadoMovimiento.PAGADO)
        val nuevo = movimiento(2, 1, fecha(30, 6), fecha(31, 7), 40.0, EstadoMovimiento.PAGADO)
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.ACTIVO, listOf(antiguo, nuevo), fecha(10, 7),
            inicioEtapa = fecha(1, 7)
        )
        assertFalse(res.moroso)
        assertFalse(res.morosoPorFecha)
        assertEquals(0.0, res.deuda, 0.0001)
    }

    // 26. Caso 4: PENDIENTE antiguo (anterior a fechaAlta) -> moroso por deuda
    //    (la deuda no depende de etapas ni fechas).
    @Test
    fun reinscripcion_con_pendiente_antiguo_moroso_por_deuda() {
        val pendiente = movimiento(1, 1, fecha(1, 2, 2025), fecha(28, 2, 2025), 25.0, EstadoMovimiento.PENDIENTE)
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.ACTIVO, listOf(pendiente), fecha(10, 7),
            inicioEtapa = fecha(1, 7)
        )
        assertTrue(res.moroso)
        assertTrue(res.morosoPorDeuda)
        assertFalse(res.morosoPorFecha)
        assertEquals(25.0, res.deuda, 0.0001)
    }

    // 27. Caso 5: PAGADO antiguo terminado y ningún movimiento nuevo -> NO moroso
    //    por fecha. Sin inicioEtapa (comportamiento histórico) sí lo sería.
    @Test
    fun reinscripcion_sin_nuevo_movimiento_no_arrastra_periodo_anterior() {
        val antiguo = movimiento(1, 1, fecha(1, 1, 2025), fecha(31, 1, 2025), 30.0, EstadoMovimiento.PAGADO)
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.ACTIVO, listOf(antiguo), fecha(10, 7),
            inicioEtapa = fecha(1, 7)
        )
        assertFalse(res.moroso)
        assertFalse(res.morosoPorFecha)
        assertEquals(0.0, res.deuda, 0.0001)

        val sinCorte = MovimientoMorosidad.resultadoDe(
            EstadoCliente.ACTIVO, listOf(antiguo), fecha(10, 7)
        )
        assertTrue(sinCorte.morosoPorFecha)
    }

    // 28. Caso 6: ACTIVO normal (misma etapa) con período terminado sin renovar
    //    -> SÍ moroso por fecha (se conserva la regla).
    @Test
    fun activo_misma_etapa_periodo_terminado_sigue_moroso_por_fecha() {
        val pagado = movimiento(1, 1, fecha(1, 8), fecha(31, 8), 30.0, EstadoMovimiento.PAGADO)
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.ACTIVO, listOf(pagado), fecha(15, 9),
            inicioEtapa = fecha(1, 1)
        )
        assertTrue(res.moroso)
        assertFalse(res.morosoPorDeuda)
        assertTrue(res.morosoPorFecha)
        assertEquals(0.0, res.deuda, 0.0001)
    }

    // 29. Caso 7: ACTIVO normal con un PENDIENTE aunque el período pagado siga
    //    vigente -> moroso por deuda (independiente de fechas).
    @Test
    fun activo_con_pendiente_y_periodo_vigente_moroso_por_deuda() {
        val pendiente = movimiento(1, 1, fecha(1, 8), fecha(31, 8), 20.0, EstadoMovimiento.PENDIENTE)
        val vigente = movimiento(2, 1, fecha(1, 9), fecha(30, 9), 30.0, EstadoMovimiento.PAGADO)
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.ACTIVO, listOf(pendiente, vigente), fecha(15, 9),
            inicioEtapa = fecha(1, 1)
        )
        assertTrue(res.moroso)
        assertTrue(res.morosoPorDeuda)
        assertFalse(res.morosoPorFecha)
        assertEquals(20.0, res.deuda, 0.0001)
    }

    // 30. Caso 8: reactivar sin deuda y sin haber creado todavía el nuevo
    //    movimiento -> NO moroso y sin fecha de entrada de morosidad.
    @Test
    fun reactivar_sin_movimiento_nuevo_no_entra_en_morosidad() {
        val antiguo = movimiento(1, 1, fecha(1, 1, 2025), fecha(31, 1, 2025), 30.0, EstadoMovimiento.PAGADO)
        val final = MovimientoMorosidad.resultadoFinal(
            EstadoCliente.ACTIVO, listOf(antiguo),
            morosoPrevio = false, fechaEntradaPrevia = null,
            ahora = fecha(10, 7), inicioEtapa = fecha(1, 7)
        )
        assertFalse(final.moroso)
        assertNull(final.fechaEntradaMorosidad)
    }

    // =========================================================
    // FRONTERA = ÚLTIMA FECHA DE BAJA (fechaBaja)
    // =========================================================

    // 31. Cliente sin fechaBaja (nunca de baja) con PAGADO terminado ayer
    //    -> moroso por fecha (regresión 04/09/2026 con fin 03/09/2026).
    @Test
    fun sin_fecha_baja_pagado_terminado_ayer_moroso_por_fecha() {
        val pagado = movimiento(1, 1, fecha(1, 9), fecha(3, 9), 33.0, EstadoMovimiento.PAGADO)
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.ACTIVO, listOf(pagado), fecha(4, 9)
        )
        assertTrue(res.moroso)
        assertFalse(res.morosoPorDeuda)
        assertTrue(res.morosoPorFecha)
        assertEquals(0.0, res.deuda, 0.0001)
    }

    // 32. fechaBaja antigua + PAGADO terminado ANTES de esa baja -> NO moroso.
    @Test
    fun fecha_baja_antigua_pagado_anterior_terminado_no_moroso() {
        val antiguo = movimiento(1, 1, fecha(1, 1, 2025), fecha(31, 1, 2025), 30.0, EstadoMovimiento.PAGADO)
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.ACTIVO, listOf(antiguo), fecha(10, 7),
            inicioEtapa = fecha(1, 7, 2025)
        )
        assertFalse(res.moroso)
        assertFalse(res.morosoPorFecha)
        assertEquals(0.0, res.deuda, 0.0001)
    }

    // 33. fechaBaja antigua + NUEVO PAGADO de la nueva etapa terminado
    //    (fin 03/09/2026, hoy 04/09/2026) -> MOROSO por fecha.
    @Test
    fun fecha_baja_antigua_nuevo_pagado_terminado_moroso() {
        val nuevo = movimiento(1, 1, fecha(1, 8), fecha(3, 9), 33.0, EstadoMovimiento.PAGADO)
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.ACTIVO, listOf(nuevo), fecha(4, 9),
            inicioEtapa = fecha(1, 7, 2025)
        )
        assertTrue(res.moroso)
        assertFalse(res.morosoPorDeuda)
        assertTrue(res.morosoPorFecha)
        assertEquals(0.0, res.deuda, 0.0001)
    }

    // 34. fechaBaja antigua + NUEVO PAGADO vigente (cubre hoy) -> NO moroso.
    @Test
    fun fecha_baja_antigua_nuevo_pagado_vigente_no_moroso() {
        val nuevo = movimiento(1, 1, fecha(1, 9), fecha(30, 9), 33.0, EstadoMovimiento.PAGADO)
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.ACTIVO, listOf(nuevo), fecha(4, 9),
            inicioEtapa = fecha(1, 7, 2025)
        )
        assertFalse(res.moroso)
        assertFalse(res.morosoPorFecha)
        assertEquals(0.0, res.deuda, 0.0001)
    }

    // 35. PENDIENTE anterior a la fechaBaja sigue contando como deuda.
    @Test
    fun pendiente_anterior_a_fecha_baja_sigue_como_deuda() {
        val pendiente = movimiento(1, 1, fecha(1, 2, 2025), fecha(28, 2, 2025), 25.0, EstadoMovimiento.PENDIENTE)
        val res = MovimientoMorosidad.resultadoDe(
            EstadoCliente.ACTIVO, listOf(pendiente), fecha(10, 7),
            inicioEtapa = fecha(1, 7, 2025)
        )
        assertTrue(res.moroso)
        assertTrue(res.morosoPorDeuda)
        assertFalse(res.morosoPorFecha)
        assertEquals(25.0, res.deuda, 0.0001)
    }
}
