package com.roberto.gestorpro.util

import com.roberto.gestorpro.data.entity.MovimientoEntity

/**
 * MovimientoFiltro
 * ----------------
 * Filtro visual de fechas para los movimientos de Economía (puro, testeable sin
 * UI ni base de datos). Se aplica EXCLUSIVAMENTE en memoria sobre la lista ya
 * cargada y actúa sobre la **fechaInicio** del movimiento (la "fecha del
 * movimiento"), que es la misma fecha que usa el orden actual:
 *  - `ItemEconomia.fecha()` devuelve `movimiento.fechaInicio`;
 *  - el DAO ordena por `ORDER BY fechaInicio`.
 *
 * El filtro SOLO determina qué movimientos se muestran. No modifica deuda,
 * morosidad, totales, Room ni Firestore: "Deuda total" se sigue calculando
 * sobre el conjunto completo, no sobre la lista filtrada.
 */
object MovimientoFiltro {

    /**
     * Un rango es válido si no hay fechas o si `desde <= hasta`.
     * `desde > hasta` NO es un rango válido (la UI lo trata como estado inválido).
     */
    fun rangoValido(desde: Long?, hasta: Long?): Boolean =
        desde == null || hasta == null || desde <= hasta

    /**
     * ¿Está `fechaInicio` dentro del rango [desde, hasta] (ambos inclusivos)?
     * - solo Desde  → fechaInicio >= desde
     * - solo Hasta  → fechaInicio <= hasta
     * - sin fechas  → siempre true
     */
    fun enRango(fechaInicio: Long, desde: Long?, hasta: Long?): Boolean {
        if (desde != null && fechaInicio < desde) return false
        if (hasta != null && fechaInicio > hasta) return false
        return true
    }

    /**
     * Devuelve solo los movimientos cuya `fechaInicio` está dentro del rango.
     * No muta la lista recibida.
     */
    fun movimientosEnRango(
        movimientos: List<MovimientoEntity>,
        desde: Long?,
        hasta: Long?
    ): List<MovimientoEntity> =
        movimientos.filter { enRango(it.fechaInicio, desde, hasta) }
}
