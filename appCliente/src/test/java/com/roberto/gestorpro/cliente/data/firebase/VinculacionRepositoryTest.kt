package com.roberto.gestorpro.cliente.data.firebase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VinculacionRepositoryTest {

    @Test
    fun indiceInexistenteRechazaLaVinculacionViaA() {
        val resultado = VinculacionRepository.resultadoCuandoIndiceNoExiste()

        assertFalse(resultado.exito)
        assertEquals("No existe ningún cliente registrado con ese DNI.", resultado.mensaje)
        assertNull(resultado.clienteId)
        assertNull(resultado.negocioId)
        assertFalse(resultado.requiereCompletarPerfil)
    }

    @Test
    fun sinFichaYsinPerfilPideCompletarElRegistro() {
        val resultado = VinculacionRepository.resultadoNoHayFichaParaRegistro()

        assertFalse(resultado.exito)
        assertTrue(resultado.requiereCompletarPerfil)
        assertEquals(
            "No encontramos una ficha con este DNI. Puedes registrarte ahora " +
                "y después te vincularemos automáticamente a este centro.",
            resultado.mensaje
        )
        assertNull(resultado.clienteId)
        assertNull(resultado.negocioId)
    }
}
