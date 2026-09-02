package com.roberto.gestorpro.util

import com.roberto.gestorpro.data.entity.MovimientoEntity
import com.roberto.gestorpro.model.EstadoCliente
import com.roberto.gestorpro.model.EstadoMovimiento

/**
 * ResultadoMorosidad
 * ------------------
 * Resultado de la detección de morosidad de un cliente en un instante dado.
 */
data class ResultadoMorosidad(
    val moroso: Boolean,
    val deuda: Double,
    val fechaEntradaSugerida: Long?
)

/**
 * EstadoFinalMorosidad
 * --------------------
 * Valores finales a persistir en ClienteEntity tras aplicar el recálculo
 * (conserva la fecha de entrada previa mientras el cliente siga moroso).
 */
data class EstadoFinalMorosidad(
    val moroso: Boolean,
    val fechaEntradaMorosidad: Long?
)

/**
 * MovimientoMorosidad
 * -------------------
 * ÚNICA fuente de lógica de morosidad y deuda del ADMIN (FASE 5). Funciones
 * PURAS, sin acceso a base de datos ni UI, para poder testearse fácilmente.
 *
 * Reglas de negocio cerradas:
 *  - La deuda es la suma de los movimientos PENDIENTES ya EXIGIBLES (su
 *    periodo terminó). Un PENDIENTE futuro NO es deuda.
 *  - La morosidad es INDEPENDIENTE del estado administrativo (ACTIVO/BAJA):
 *    se guarda como un flag `moroso`.
 *  - ACTIVO: es moroso si tiene deuda exigible O si su cobertura PAGADA ya
 *    terminó y no existe un periodo PAGADO que cubra la fecha actual (aunque
 *    el ADMIN no haya creado todavía el siguiente movimiento).
 *  - BAJA: solo es moroso por deuda real (la ausencia de nuevo periodo no
 *    genera morosidad).
 *  - fechaEntradaMorosidad = fechaFin del periodo que provoca la entrada; se
 *    conserva mientras siga moroso y solo se restablece al volver a entrar.
 */
object MovimientoMorosidad {

    /**
     * Movimiento pendiente exigible: periodo ya vencido (fechaFin <= ahora).
     */
    fun esExigible(movimiento: MovimientoEntity, ahora: Long): Boolean =
        movimiento.estado == EstadoMovimiento.PENDIENTE &&
            movimiento.fechaFin <= ahora

    /**
     * Deuda actual: suma de precioFinal de los PENDIENTES exigibles.
     * No cuenta PAGADOS ni PENDIENTES futuros.
     */
    fun deudaDe(movimientos: List<MovimientoEntity>, ahora: Long): Double =
        movimientos
            .filter { esExigible(it, ahora) }
            .sumOf { it.precioFinal }

    /**
     * ¿Existe un movimiento PAGADO cuyo periodo cubre la fecha actual?
     */
    fun tieneCoberturaPagadaActual(movimientos: List<MovimientoEntity>, ahora: Long): Boolean =
        movimientos.any {
            it.estado == EstadoMovimiento.PAGADO &&
                it.fechaInicio <= ahora &&
                ahora <= it.fechaFin
        }

    /**
     * ¿La cobertura pagada ya se perdió? (hubo un PAGADO que terminó en el
     * pasado y no hay cobertura actual).
     */
    fun continuidadPagadaPerdida(movimientos: List<MovimientoEntity>, ahora: Long): Boolean {
        if (tieneCoberturaPagadaActual(movimientos, ahora)) return false
        return movimientos.any { it.estado == EstadoMovimiento.PAGADO && it.fechaFin < ahora }
    }

    /**
     * Fecha sugerida de entrada en morosidad (fechaFin del periodo que la
     * provoca). Si existen varios disparadores se usa el más temprano.
     */
    fun fechaEntradaSugerida(movimientos: List<MovimientoEntity>, ahora: Long): Long? {
        val porDeuda = movimientos
            .filter { esExigible(it, ahora) }
            .minOfOrNull { it.fechaFin }
        val porContinuidad = if (continuidadPagadaPerdida(movimientos, ahora)) {
            movimientos
                .filter { it.estado == EstadoMovimiento.PAGADO && it.fechaFin < ahora }
                .maxOfOrNull { it.fechaFin }
        } else {
            null
        }
        return listOfNotNull(porDeuda, porContinuidad).minOrNull()
    }

    /**
     * Detección de morosidad según el estado administrativo del cliente.
     */
    fun resultadoDe(
        estado: EstadoCliente,
        movimientos: List<MovimientoEntity>,
        ahora: Long
    ): ResultadoMorosidad {
        val deuda = deudaDe(movimientos, ahora)
        val moroso = when (estado) {
            EstadoCliente.ACTIVO ->
                deuda > 0.0 || continuidadPagadaPerdida(movimientos, ahora)
            EstadoCliente.BAJA -> deuda > 0.0
            // REGISTRADO / ARCHIVADO / MOROSO: sin regla de morosidad propia.
            else -> false
        }
        val fechaSugerida = if (moroso) {
            fechaEntradaSugerida(movimientos, ahora)
        } else {
            null
        }
        return ResultadoMorosidad(moroso, deuda, fechaSugerida)
    }

    /**
     * Valores finales a persistir. Si el cliente YA era moroso se conserva su
     * fechaEntradaMorosidad (no se reinicia en cada recálculo); si sale de
     * morosidad se limpia; si vuelve a entrar se propone una nueva fecha.
     */
    fun resultadoFinal(
        estado: EstadoCliente,
        movimientos: List<MovimientoEntity>,
        morosoPrevio: Boolean,
        fechaEntradaPrevia: Long?,
        ahora: Long
    ): EstadoFinalMorosidad {
        val resultado = resultadoDe(estado, movimientos, ahora)
        val fechaFinal = if (resultado.moroso) {
            if (morosoPrevio && fechaEntradaPrevia != null) {
                fechaEntradaPrevia
            } else {
                resultado.fechaEntradaSugerida
            }
        } else {
            null
        }
        return EstadoFinalMorosidad(resultado.moroso, fechaFinal)
    }
}
