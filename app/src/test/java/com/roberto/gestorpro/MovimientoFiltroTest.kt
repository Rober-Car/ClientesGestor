package com.roberto.gestorpro

import com.roberto.gestorpro.data.entity.MovimientoEntity
import com.roberto.gestorpro.model.EstadoMovimiento
import com.roberto.gestorpro.util.MovimientoFiltro
import com.roberto.gestorpro.util.MovimientoMorosidad
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * MovimientoFiltroTest
 * --------------------
 * Tests del filtro visual de fechas de Economía. El filtro se aplica sobre la
 * fechaInicio del movimiento (la "fecha del movimiento", coherente con el
 * orden actual) y SOLO determina qué movimientos se muestran: nunca modifica
 * deuda, morosidad ni los datos subyacentes.
 */
class MovimientoFiltroTest {

    private fun dia(anio: Int, mes: Int, diaMes: Int): Long =
        LocalDate.of(anio, mes, diaMes)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

    private fun movimiento(
        id: Int,
        fechaInicio: Long,
        estado: EstadoMovimiento = EstadoMovimiento.PAGADO,
        precio: Double = 10.0
    ): MovimientoEntity = MovimientoEntity(
        idMovimiento = id,
        idCliente = 1,
        fechaInicio = fechaInicio,
        fechaFin = fechaInicio + 86_400_000L,
        precioFinal = precio,
        estado = estado
    )

    @Test
    fun rango_desde_hasta_incluye_ambos_extremos() {
        val desde = dia(2026, 9, 1)
        val hasta = dia(2026, 9, 10)
        assertTrue(MovimientoFiltro.enRango(dia(2026, 9, 1), desde, hasta))
        assertTrue(MovimientoFiltro.enRango(dia(2026, 9, 10), desde, hasta))
        assertTrue(MovimientoFiltro.enRango(dia(2026, 9, 5), desde, hasta))
        assertFalse(MovimientoFiltro.enRango(dia(2026, 8, 31), desde, hasta))
        assertFalse(MovimientoFiltro.enRango(dia(2026, 9, 11), desde, hasta))
    }

    @Test
    fun solo_desde_permite_movimientos_posteriores() {
        val desde = dia(2026, 9, 1)
        assertTrue(MovimientoFiltro.enRango(dia(2026, 9, 1), desde, null))
        assertTrue(MovimientoFiltro.enRango(dia(2026, 12, 31), desde, null))
        assertFalse(MovimientoFiltro.enRango(dia(2026, 8, 31), desde, null))
    }

    @Test
    fun solo_hasta_permite_movimientos_anteriores() {
        val hasta = dia(2026, 9, 1)
        assertTrue(MovimientoFiltro.enRango(dia(2026, 1, 1), null, hasta))
        assertTrue(MovimientoFiltro.enRango(dia(2026, 9, 1), null, hasta))
        assertFalse(MovimientoFiltro.enRango(dia(2026, 9, 2), null, hasta))
    }

    @Test
    fun sin_filtro_se_muestran_todos() {
        assertTrue(MovimientoFiltro.enRango(dia(2020, 1, 1), null, null))
        assertTrue(MovimientoFiltro.enRango(dia(2030, 1, 1), null, null))
        assertTrue(MovimientoFiltro.rangoValido(null, null))
    }

    @Test
    fun rango_desde_mayor_que_hasta_es_invalido() {
        val desde = dia(2026, 9, 10)
        val hasta = dia(2026, 9, 1)
        assertFalse(MovimientoFiltro.rangoValido(desde, hasta))
    }

    @Test
    fun limpiar_filtro_equivale_a_sin_fechas() {
        // Limpiar pone desde/hasta a null; con null/null se muestran todos.
        assertTrue(MovimientoFiltro.rangoValido(null, null))
        assertTrue(MovimientoFiltro.enRango(dia(2026, 5, 5), null, null))
    }

    @Test
    fun lista_filtrada_no_cambia_la_deuda_real() {
        val ago = dia(2026, 8, 15)
        val sep = dia(2026, 9, 15)
        val oct = dia(2026, 10, 15)

        val pendienteAgosto = movimiento(1, ago, EstadoMovimiento.PENDIENTE, 30.0)
        val pendienteSeptiembre = movimiento(2, sep, EstadoMovimiento.PENDIENTE, 40.0)
        val pendienteOctubre = movimiento(3, oct, EstadoMovimiento.PENDIENTE, 70.0)
        val completa = listOf(pendienteAgosto, pendienteSeptiembre, pendienteOctubre)

        val filtrada = MovimientoFiltro.movimientosEnRango(
            completa,
            desde = dia(2026, 9, 1),
            hasta = dia(2026, 9, 30)
        )

        // El filtro oculta el pendiente de agosto (fuera de rango)...
        assertEquals(1, filtrada.size)
        assertEquals(2, filtrada.single().idMovimiento)

        // ...pero la DEUDA real se calcula sobre el conjunto completo: suma de
        // TODOS los PENDIENTES (30+40+70), sin depender del filtro visual.
        assertEquals(140.0, MovimientoMorosidad.deudaDe(completa), 0.001)
        // Y la lista original no se muta (el filtro es puro).
        assertEquals(3, completa.size)
    }
}
