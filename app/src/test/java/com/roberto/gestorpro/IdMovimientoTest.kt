package com.roberto.gestorpro

import com.roberto.gestorpro.util.IdMovimiento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * IdMovimientoTest
 * ----------------
 * Tests del generador de IDs globales de movimiento (patrón IdCliente).
 * El objetivo es evitar colisiones entre dispositivos Admin: el id debe caer
 * en el rango alto reservado (>= 1.000.000.000) y, estadísticamente, no
 * repetirse entre varias generaciones seguidas.
 */
class IdMovimientoTest {

    @Test
    fun ids_estan_en_rango_alto() {
        repeat(200) {
            val id = IdMovimiento.nuevo()
            assertTrue("id $id fuera de rango", id in IdMovimiento.MINIMO until Int.MAX_VALUE)
        }
    }

    @Test
    fun generaciones_repetidas_son_distintas() {
        val ids = (1..200).map { IdMovimiento.nuevo() }
        assertEquals("no debe repetirse en 200 generaciones", ids.size, ids.distinct().size)
    }

    @Test
    fun minimo_es_suficientemente_alto() {
        // El rango alto debe estar por encima de los ids históricos (autoincrement).
        assertTrue(IdMovimiento.MINIMO > 1_000_000)
    }
}
