package com.roberto.clientesgestor

import org.junit.Test

import org.junit.Assert.*

/**
 * ExampleUnitTest
 * ---------------
 * ✔ TIPO: clase de prueba unitaria (JUnit)
 * Es la clase que contiene pruebas unitarias locales que se ejecutan en la máquina de desarrollo.
 * Sirve para comprobar la lógica de la aplicación sin necesidad de un dispositivo Android.
 */
class ExampleUnitTest {

    /**
     * addition_isCorrect
     * ------------------
     * ✔ TIPO: método de prueba (JUnit @Test)
     * Es una prueba sencilla que verifica que la suma 2 + 2 es 4.
     * Sirve para comprobar que el entorno de pruebas funciona correctamente.
     */
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}