package com.roberto.gestorpro.data.converter

import androidx.room.TypeConverter

/**
 * IntListConverter
 * ----------------
 * Conversor de Room entre List<Int> y String.
 * Sirve para guardar campos de tipo lista de enteros (como
 * ClienteEntity.serviciosContratados) en una sola columna de texto,
 * separando cada id por comas.
 */
class IntListConverter {

    /**
     * fromIntList
     * -----------
     * Convierte una List<Int> en un texto separado por comas.
     * Una lista vacía se guarda como "".
     */
    @TypeConverter
    fun fromIntList(lista: List<Int>): String {
        return lista.joinToString(separator = ",")
    }

    /**
     * toIntList
     * ---------
     * Reconstruye la List<Int> a partir del texto guardado.
     * Si la columna está vacía o contiene valores no numéricos
     * devuelve una lista vacía en lugar de romper la conversión.
     */
    @TypeConverter
    fun toIntList(valor: String): List<Int> {
        return if (valor.isBlank()) {
            emptyList()
        } else {
            valor.split(",").mapNotNull { it.toIntOrNull() }
        }
    }
}
