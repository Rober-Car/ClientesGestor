package com.roberto.clientesgestor

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * ExampleInstrumentedTest
 * -----------------------
 * ✔ TIPO: clase de prueba instrumentada (AndroidJUnit4)
 * Es la clase que contiene pruebas instrumentadas que se ejecutan en un dispositivo o emulador Android.
 * Sirve para comprobar el comportamiento de la app en un entorno real.
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    /**
     * useAppContext
     * -------------
     * ✔ TIPO: método de prueba (JUnit @Test)
     * Es una prueba que comprueba que el contexto de la aplicación es correcto.
     * Sirve para verificar que el paquete de la app es com.roberto.clientesgestor.
     */
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.roberto.clientesgestor", appContext.packageName)
    }
}