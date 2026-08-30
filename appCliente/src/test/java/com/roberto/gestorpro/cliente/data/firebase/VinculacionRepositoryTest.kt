package com.roberto.gestorpro.cliente.data.firebase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class VinculacionRepositoryTest {

    @Test
    fun indiceInexistenteRechazaLaVinculacionViaA() {
        val resultado = VinculacionRepository.resultadoCuandoIndiceNoExiste()

        assertFalse(resultado.exito)
        assertEquals("No existe ningún cliente registrado con ese DNI.", resultado.mensaje)
        assertNull(resultado.clienteId)
        assertNull(resultado.negocioId)
    }
}
