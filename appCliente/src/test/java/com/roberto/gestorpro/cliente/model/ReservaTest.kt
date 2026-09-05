package com.roberto.gestorpro.cliente.model

import com.roberto.gestorpro.cliente.data.firebase.ReservaRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReservaTest {

    @Test
    fun reservaIdEsDeterministaParaClienteYSesion() {
        assertEquals("12_34", ReservaRepository.reservaId(12, 34))
    }

    @Test
    fun estadoReservaPriorizaLaReservaSobreLasPlazas() {
        assertEquals(
            EstadoReserva.RESERVADA,
            EstadoReserva.de(reservadaPorMi = true, plazasDisponibles = 0)
        )
    }

    @Test
    fun estadoReservaDistingueCompletaYDisponible() {
        assertEquals(
            EstadoReserva.COMPLETA,
            EstadoReserva.de(reservadaPorMi = false, plazasDisponibles = 0)
        )
        assertEquals(
            EstadoReserva.RESERVAR,
            EstadoReserva.de(reservadaPorMi = false, plazasDisponibles = 1)
        )
    }

    // Regla de negocio de cancelación: si el cliente YA tiene la sesión
    // reservada, el estado visual es SIEMPRE RESERVADA (cancelable) aunque no
    // queden plazas. La disponibilidad solo limita NUEVAS reservas.

    @Test
    fun reservado_con_cero_plazas_es_reservada_cancelable() {
        assertEquals(
            EstadoReserva.RESERVADA,
            EstadoReserva.de(reservadaPorMi = true, plazasDisponibles = 0)
        )
    }

    @Test
    fun reservado_con_plazas_disponibles_es_reservada_cancelable() {
        assertEquals(
            EstadoReserva.RESERVADA,
            EstadoReserva.de(reservadaPorMi = true, plazasDisponibles = 2)
        )
    }

    @Test
    fun no_reservado_con_cero_plazas_es_completa() {
        assertEquals(
            EstadoReserva.COMPLETA,
            EstadoReserva.de(reservadaPorMi = false, plazasDisponibles = 0)
        )
    }

    @Test
    fun no_reservado_con_plazas_disponibles_es_reservable() {
        assertEquals(
            EstadoReserva.RESERVAR,
            EstadoReserva.de(reservadaPorMi = false, plazasDisponibles = 2)
        )
    }

    @Test
    fun aperturaNullAbreReservasDesdeElInicioDelDia() {
        assertTrue(ReservaRepository.aperturaAlcanzada(fecha = 0L, horaDesdeReserva = null))
    }

    @Test
    fun aperturaEnElPasadoPermiteReservar() {
        val fecha = System.currentTimeMillis() - 86400000L
        assertTrue(ReservaRepository.aperturaAlcanzada(fecha, "00:00"))
    }

    @Test
    fun aperturaFuturaImpideReservar() {
        val fecha = System.currentTimeMillis() + 86400000L
        assertFalse(ReservaRepository.aperturaAlcanzada(fecha, "00:00"))
    }

    @Test
    fun aperturaMismoDiaConHoraPosteriorImpideReservar() {
        val fecha = java.time.LocalDate.now()
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        // Hora 23:59 de hoy: en general aún no ha llegado (salvo que se ejecute
        // exactamente en el último minuto del día; el emulador de Rules cubre el
        // caso determinista con fechas futuras).
        assertFalse(ReservaRepository.aperturaAlcanzada(fecha, "23:59"))
    }

    @Test
    fun aperturaConFormatoInvalidoSeTrataComoAbierta() {
        assertTrue(ReservaRepository.aperturaAlcanzada(fecha = 0L, horaDesdeReserva = "abc"))
    }

    // Carrera por la última plaza: cuando la Transaction es rechazada por Rules
    // (PERMISSION_DENIED) pero la sesión ya no tiene plazas, el mensaje real es
    // "no quedan plazas", no "no tienes permisos".

    @Test
    fun mensajeSinPlazasSiProcede_con_cero_plazas_devuelve_no_quedan() {
        assertEquals(
            "No quedan plazas disponibles.",
            ReservaRepository.mensajeSinPlazasSiProcede(plazasActuales = 0)
        )
    }

    @Test
    fun mensajeSinPlazasSiProcede_con_plazas_no_transforma_el_error() {
        assertNull(ReservaRepository.mensajeSinPlazasSiProcede(plazasActuales = 1))
    }

    @Test
    fun mensajeSinPlazasSiProcede_sin_lectura_no_transforma_el_error() {
        // No se puede comprobar el estado (null): no se debe convertir un
        // PERMISSION_DENIED real en "no quedan plazas".
        assertNull(ReservaRepository.mensajeSinPlazasSiProcede(plazasActuales = null))
    }
}
