package com.roberto.gestorpro.cliente

import com.roberto.gestorpro.cliente.data.firebase.FotoClienteStorage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * FotoClienteStorageTest (appCliente)
 * ----------------------------------
 * Tests puros de los helpers de la foto de perfil del cliente (URL/local y
 * cache-busting). Los flujos de subida y de vinculación con foto requieren
 * Firebase/Storage y se validan de forma manual/instrumentada.
 */
class FotoClienteStorageTest {

    @Test
    fun url_remota_se_detecta_como_url() {
        assertTrue(FotoClienteStorage.esUrlFoto("https://firebasestorage.googleapis.com/x"))
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
    fun cache_busting_anade_rev() {
        assertEquals(
            "https://storage/foto.jpg?rev=1000",
            FotoClienteStorage.conRevision("https://storage/foto.jpg", 1000L)
        )
        assertEquals(
            "https://storage/foto.jpg?token=abc&rev=2000",
            FotoClienteStorage.conRevision("https://storage/foto.jpg?token=abc", 2000L)
        )
    }
}
