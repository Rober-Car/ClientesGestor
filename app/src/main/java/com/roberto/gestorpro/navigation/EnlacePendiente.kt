package com.roberto.gestorpro.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * EnlacePendiente
 * ---------------
 * ✔ TIPO: object (objeto singleton de Kotlin)
 * Es el almacén en memoria del token recibido por deep link
 * (gestorpro://vincular/{token}) mientras el usuario todavía no puede
 * reclamar su ficha (por ejemplo, porque aún no ha iniciado sesión).
 * Sirve para que la reclamación se complete automáticamente justo después
 * de autenticarse, sin obligar a volver a pegar el código.
 */
object EnlacePendiente {

    /**
     * codigo
     * ------
     * ✔ TIPO: propiedad observable (var by mutableStateOf) → String? (nullable)
     * Es el token extraído del deep link pendiente de consumir.
     * Sirve a MainViewModel y AppNavigation para decidir el destino tras
     * el login o registro; vale null cuando no hay ningún enlace pendiente.
     */
    var codigo: String? by mutableStateOf(null)

    /**
     * limpiar
     * -------
     * ✔ TIPO: método (fun) de Kotlin
     * Descarta el código pendiente. Sirve para llamarlo cuando la vinculación
     * terminó con éxito o el usuario la canceló.
     */
    fun limpiar() {
        codigo = null
    }
}
