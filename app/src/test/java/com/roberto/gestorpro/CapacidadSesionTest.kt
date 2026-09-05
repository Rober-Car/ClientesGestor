package com.roberto.gestorpro

import com.roberto.gestorpro.util.CapacidadSesion
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * CapacidadSesionTest
 * -------------------
 * Tests de la regla pura que recalcula `plazasDisponibles` al cambiar la
 * CAPACIDAD de una sesión con reservas activas reales:
 *
 *     plazasDisponibles = nuevaCapacidad - inscritos  (si inscritos <= capacidad)
 *     plazasDisponibles = 0                            (regla existente si la
 *                                                       nueva capacidad es menor
 *                                                       que las reservas)
 */
class CapacidadSesionTest {

    @Test
    fun capacidad_2_con_1_inscrito_a_capacidad_1_deja_0_disponibles() {
        assertEquals(
            0,
            CapacidadSesion.plazasDisponiblesTrasCambioCapacidad(nuevaCapacidad = 1, inscritos = 1)
        )
    }

    @Test
    fun capacidad_2_con_0_inscritos_a_capacidad_1_deja_1_disponible() {
        assertEquals(
            1,
            CapacidadSesion.plazasDisponiblesTrasCambioCapacidad(nuevaCapacidad = 1, inscritos = 0)
        )
    }

    @Test
    fun capacidad_20_con_5_inscritos_a_capacidad_10_deja_5_disponibles() {
        assertEquals(
            5,
            CapacidadSesion.plazasDisponiblesTrasCambioCapacidad(nuevaCapacidad = 10, inscritos = 5)
        )
    }

    @Test
    fun nueva_capacidad_igual_a_inscritos_deja_0_disponibles() {
        assertEquals(
            0,
            CapacidadSesion.plazasDisponiblesTrasCambioCapacidad(nuevaCapacidad = 3, inscritos = 3)
        )
    }

    @Test
    fun inscritos_mayores_que_nueva_capacidad_conserva_la_regla_existente_plazas_0() {
        // Caso "5 reservas + nueva capacidad 3": el proyecto actual NO impide
        // guardar ni expulsa ni cancela reservas; simplemente fija plazas = 0.
        // Esta prueba documenta la regla existente (no se inventa una nueva).
        assertEquals(
            0,
            CapacidadSesion.plazasDisponiblesTrasCambioCapacidad(nuevaCapacidad = 3, inscritos = 5)
        )
    }

    @Test
    fun inscritosDesdeDatosLocales_deriva_del_valor_room() {
        // Sesión Room con capacidad 2 y 1 plaza disponible => 1 reserva activa.
        assertEquals(1, CapacidadSesion.inscritosDesdeDatosLocales(2, 1))
        assertEquals(0, CapacidadSesion.inscritosDesdeDatosLocales(2, 2))
        // Nunca negativo aunque Room esté inconsistente.
        assertEquals(0, CapacidadSesion.inscritosDesdeDatosLocales(1, 2))
    }
}
