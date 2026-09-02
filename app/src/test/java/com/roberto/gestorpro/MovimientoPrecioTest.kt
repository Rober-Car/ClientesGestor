package com.roberto.gestorpro

import com.roberto.gestorpro.data.entity.ServicioEntity
import com.roberto.gestorpro.util.MovimientoPrecio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * MovimientoPrecioTest
 * --------------------
 * Tests unitarios de la FASE 3 de ECONOMÃA (cÃ¡lculo de la propuesta de precio
 * y reglas puras de selecciÃ³n de servicios en movimientos).
 */
class MovimientoPrecioTest {

    private fun servicio(id: Int, precio: Double, activo: Boolean = true): ServicioEntity =
        ServicioEntity(
            idServicio = id,
            negocioId = "",
            nombre = "Servicio $id",
            descripcion = "",
            activo = activo,
            precio = precio
        )

    @Test
    fun unServicio_seleccionado_suma_su_precio() {
        val sugerido = MovimientoPrecio.precioSugerido(listOf(servicio(1, 20.0)))
        assertEquals(20.0, sugerido, 0.0001)
    }

    @Test
    fun variosServicios_suman_todos_los_precios() {
        val seleccion = listOf(
            servicio(1, 20.0),
            servicio(2, 30.0),
            servicio(3, 5.0)
        )
        assertEquals(55.0, MovimientoPrecio.precioSugerido(seleccion), 0.0001)
    }

    @Test
    fun servicioConPrecioCero_seCalculaCorrectamente() {
        val sugerido = MovimientoPrecio.precioSugerido(
            listOf(servicio(1, 0.0), servicio(2, 30.0))
        )
        assertEquals(30.0, sugerido, 0.0001)
    }

    @Test
    fun servicioInactivo_noAparece_comoSeleccionable() {
        val todos = listOf(
            servicio(1, 20.0, activo = true),
            servicio(2, 30.0, activo = false)
        )
        val seleccionables = MovimientoPrecio.serviciosSeleccionables(todos)
        assertEquals(listOf(1), seleccionables.map { it.idServicio })
    }

    @Test
    fun precioFinal_mantenido_si_hay_modificacion_manual() {
        // Suma 20+30=50 pero el ADMIN fija 45 manualmente.
        val precio = MovimientoPrecio.precioFinalPropuesto(
            precioManual = 45.0,
            servicios = listOf(servicio(1, 20.0), servicio(2, 30.0))
        )
        assertEquals(45.0, precio, 0.0001)
    }

    @Test
    fun precioFinal_propuesto_suma_si_no_hay_modificacion_manual() {
        val precio = MovimientoPrecio.precioFinalPropuesto(
            precioManual = null,
            servicios = listOf(servicio(1, 20.0), servicio(2, 30.0))
        )
        assertEquals(50.0, precio, 0.0001)
    }

    @Test
    fun precioCero_es_valido_como_precioFinal() {
        val precio = MovimientoPrecio.precioFinalPropuesto(
            precioManual = 0.0,
            servicios = listOf(servicio(1, 20.0))
        )
        assertEquals(0.0, precio, 0.0001)
    }

    @Test
    fun movimientoHistorico_conServicioInactivo_loConservaComoFijo() {
        val idsMovimiento = listOf(1, 2) // 1 activo, 2 dado de baja
        val fijos = MovimientoPrecio.idsFijosHistoricos(idsMovimiento, idsActivos = setOf(1))
        assertEquals(listOf(2), fijos)
    }

    @Test
    fun movimientoAntiguo_sinServicios_noSeRompe() {
        val fijos = MovimientoPrecio.idsFijosHistoricos(emptyList(), idsActivos = setOf(1, 2))
        assertTrue(fijos.isEmpty())
        assertEquals(0.0, MovimientoPrecio.precioSugerido(emptyList()), 0.0001)
    }

    @Test
    fun renovar_conserva_listaDeServicios_y_precioFinal() {
        // Renovar copia la entidad completa: la lista de servicios y el precio
        // final deben conservarse en la copia (idMovimiento = 0).
        val original = com.roberto.gestorpro.data.entity.MovimientoEntity(
            idMovimiento = 7,
            idCliente = 3,
            servicios = listOf(1, 2),
            fechaInicio = 1000L,
            fechaFin = 2000L,
            precioFinal = 45.0,
            estado = com.roberto.gestorpro.model.EstadoMovimiento.PAGADO,
            fechaPago = 1500L,
            metodoPago = null,
            observaciones = null
        )
        val renovado = original.copy(idMovimiento = 0)
        assertEquals(listOf(1, 2), renovado.servicios)
        assertEquals(45.0, renovado.precioFinal, 0.0001)
    }
}
