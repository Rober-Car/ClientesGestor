package com.roberto.gestorpro.cliente.model

import com.roberto.gestorpro.cliente.data.firebase.ReservaRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
}
