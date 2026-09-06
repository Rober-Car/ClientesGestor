package com.roberto.gestorpro

import com.roberto.gestorpro.data.firebase.FotoClienteStorage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * FotoClienteStorageTest
 * ----------------------
 * Tests puros de los helpers de la foto de cliente (detección URL/local y
 * cache-busting). Los flujos completos (subida real, vinculación con foto,
 * cambio/eliminación cruzada) requieren Firebase/Storage y se validan de forma
 * manual/instrumentada.
 */
class FotoClienteStorageTest {

    @Test
    fun url_remota_se_detecta_como_url() {
        assertTrue(FotoClienteStorage.esUrlFoto("https://firebasestorage.googleapis.com/x"))
        assertTrue(FotoClienteStorage.esUrlFoto("http://dominio/foto.jpg"))
    }

    @Test
    fun ruta_local_o_vacio_no_se_detecta_como_url() {
        assertFalse(FotoClienteStorage.esUrlFoto("/data/user/0/app/files/fotos/a.jpg"))
        assertFalse(FotoClienteStorage.esUrlFoto(""))
        assertFalse(FotoClienteStorage.esUrlFoto(null))
    }

    @Test
    fun ruta_estable_de_la_foto_del_cliente() {
        assertEquals("clientes/123/foto.jpg", FotoClienteStorage.rutaCliente(123))
    }

    @Test
    fun cache_busting_anade_parametro_rev() {
        val base = "https://storage/foto.jpg"
        val resultado = FotoClienteStorage.conRevision(base, 1000L)
        assertTrue(resultado.startsWith("$base?rev=1000"))
    }

    @Test
    fun cache_busting_respeta_parametros_existentes() {
        val base = "https://storage/foto.jpg?token=abc"
        val resultado = FotoClienteStorage.conRevision(base, 2000L)
        assertEquals("$base&rev=2000", resultado)
    }
}
