package com.roberto.gestorpro.data.converter

import androidx.room.TypeConverter

/**
 * StringListConverter.kt
 * ----------------------
 * ✔ TIPO: archivo de código fuente Kotlin (conversor de datos)
 * Es el archivo que define el conversor entre List<String> y String.
 * Sirve para que Room pueda guardar campos de tipo lista de textos,
 * como ClienteEntity.serviciosContratados, en una sola columna de texto.
 */

/**
 * StringListConverter
 * -------------------
 * ✔ TIPO: clase (conversor de Room con anotación @TypeConverter)
 * Es la clase que convierte una lista de textos en un único String separado por comas.
 * Sirve para persistir listas simples sin crear tablas adicionales; los elementos no
 * deben contener comas, condición que cumplen los nombres de servicios del proyecto.
 */
class StringListConverter {

    /**
     * fromStringList
     * --------------
     * ✔ TIPO: método (fun) → String
     * Es la función que convierte una List<String> en un texto separado por comas.
     * Sirve para guardar la lista en la base de datos (una lista vacía se guarda como "").
     */
    @TypeConverter
    fun fromStringList(lista: List<String>): String {
        return lista.joinToString(separator = ",")
    }

    /**
     * toStringList
     * ------------
     * ✔ TIPO: método (fun) → List<String>
     * Es la función que reconstruye la List<String> a partir del texto guardado.
     * Sirve para recuperar la lista original al leer la fila de la base de datos;
     * si la columna está vacía devuelve una lista vacía y no una lista con un texto vacío.
     */
    @TypeConverter
    fun toStringList(valor: String): List<String> {
        return if (valor.isBlank()) emptyList() else valor.split(",")
    }
}
