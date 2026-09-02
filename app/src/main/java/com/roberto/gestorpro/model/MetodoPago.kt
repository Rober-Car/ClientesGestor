package com.roberto.gestorpro.model

/**
 * MetodoPago
 * ----------
 * Método de pago opcional de un movimiento (decisión económica cerrada).
 * Se guarda directamente en MovimientoEntity.metodoPago. Un movimiento
 * histórico sin método registrado guarda null (campo opcional).
 */
enum class MetodoPago {
    EFECTIVO,
    BIZUM,
    TRANSFERENCIA
}
