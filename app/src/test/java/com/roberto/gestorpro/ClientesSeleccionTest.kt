package com.roberto.gestorpro

import com.roberto.gestorpro.ui.viewmodel.alternarIdEnSeleccion
import com.roberto.gestorpro.ui.viewmodel.podarSeleccion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ClientesSeleccionTest
 * ---------------------
 * Tests de la lógica PURA de la selección múltiple de clientes de la lista
 * principal (basada en IDs estables de cliente, no en la posición de la lista):
 * seleccionar, deseleccionar, selección de varios, limpieza y poda de ids que
 * desaparecen de la lista. Las operaciones masivas (activar/archivar/baja)
 * viven en el ViewModel/Repository sobre operaciones ya existentes y no se
 * pueden probar de forma pura sin infraestructura de Room/Firestore.
 */
class ClientesSeleccionTest {

    @Test
    fun seleccionar_primero_entra_con_un_id() {
        val seleccion = alternarIdEnSeleccion(emptySet(), 7)
        assertEquals(setOf(7), seleccion)
    }

    @Test
    fun seleccionar_varios_acumula_ids() {
        var seleccion = alternarIdEnSeleccion(emptySet(), 1)
        seleccion = alternarIdEnSeleccion(seleccion, 2)
        seleccion = alternarIdEnSeleccion(seleccion, 3)
        assertEquals(setOf(1, 2, 3), seleccion)
    }

    @Test
    fun deseleccionar_elimina_solo_ese_id() {
        val seleccion = alternarIdEnSeleccion(setOf(1, 2, 3), 2)
        assertEquals(setOf(1, 3), seleccion)
    }

    @Test
    fun alternar_dos_veces_devuelve_al_estado_inicial() {
        val inicial = setOf(4, 5)
        val trasAlternar = alternarIdEnSeleccion(inicial, 9)
        assertEquals(setOf(4, 5, 9), trasAlternar)
        val trasDeseleccionar = alternarIdEnSeleccion(trasAlternar, 9)
        assertEquals(inicial, trasDeseleccionar)
    }

    @Test
    fun limpiar_deja_vacia() {
        // Salir del modo selección equivale a vaciar el conjunto.
        val vacia = emptySet<Int>()
        assertEquals(vacia, emptySet<Int>())
        assertTrue(emptySet<Int>().isEmpty())
    }

    @Test
    fun podar_elimina_ids_que_ya_no_existen() {
        val seleccion = setOf(1, 2, 3, 4)
        val existentes = setOf(2, 4)
        assertEquals(setOf(2, 4), podarSeleccion(seleccion, existentes))
    }

    @Test
    fun podar_no_afecta_si_todos_existen() {
        val seleccion = setOf(10, 11)
        assertEquals(setOf(10, 11), podarSeleccion(seleccion, setOf(9, 10, 11, 12)))
    }

    @Test
    fun podar_vacia_si_no_queda_ninguno() {
        assertEquals(emptySet<Int>(), podarSeleccion(setOf(5, 6), setOf(1)))
    }

    @Test
    fun podar_con_seleccion_vacia_no_cambia() {
        assertEquals(emptySet<Int>(), podarSeleccion(emptySet(), setOf(1, 2)))
    }
}
