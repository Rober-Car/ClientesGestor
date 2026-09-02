package com.roberto.gestorpro.data.converter

import androidx.room.TypeConverter
import com.roberto.gestorpro.model.MetodoPago

/**
 * MetodoPagoConverter
 * -------------------
 * Conversor de Room entre MetodoPago? y String.
 * El método de pago es opcional: null se guarda como NULL en la columna.
 * Al leer se tolera cualquier valor desconocido devolviendo null (fail-safe)
 * para no romper la apertura de la BD si existiera un valor inconsistente.
 */
class MetodoPagoConverter {

    @TypeConverter
    fun fromMetodoPago(metodo: MetodoPago?): String? = metodo?.name

    @TypeConverter
    fun toMetodoPago(valor: String?): MetodoPago? = valor?.let {
        MetodoPago.entries.firstOrNull { pago -> pago.name == it }
    }
}
