package com.roberto.gestorpro.cliente.model

import com.roberto.gestorpro.cliente.data.firebase.ReservaRepository
import org.junit.Assert.assertEquals
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
}
