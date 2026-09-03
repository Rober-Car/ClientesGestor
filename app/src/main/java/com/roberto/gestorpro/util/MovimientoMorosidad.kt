package com.roberto.gestorpro.util

import com.roberto.gestorpro.data.entity.MovimientoEntity
import com.roberto.gestorpro.model.EstadoCliente
import com.roberto.gestorpro.model.EstadoMovimiento

/**
 * ResultadoMorosidad
 * ------------------
 * Resultado de la detección de morosidad de un cliente en un instante dado,
 * con la distinción conceptual de las DOS causas:
 *  - morosoPorDeuda : existe al menos un movimiento PENDIENTE.
 *  - morosoPorFecha : ACTIVO cuyo período pagado terminó sin nueva cobertura.
 */
data class ResultadoMorosidad(
    val moroso: Boolean,
    val deuda: Double,
    val morosoPorDeuda: Boolean = false,
    val morosoPorFecha: Boolean = false
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
 * ÚNICA fuente de lógica de morosidad y deuda del ADMIN (F2). Funciones PURAS,
 * sin acceso a base de datos ni UI, para poder testearse fácilmente.
 *
 * Reglas de negocio cerradas (modelo económico definitivo):
 *  - La deuda es la suma de TODOS los movimientos PENDIENTES. NO se filtra por
 *    fechaFin: un PENDIENTE (aunque su período aún no haya terminado) ya es deuda.
 *  - Morosidad por DEUDA: existe al menos un movimiento PENDIENTE.
 *  - Morosidad por FECHA (solo ACTIVO): la cobertura PAGADA ha terminado y no
 *    existe una nueva cobertura PAGADA que cubra la fecha actual.
 *  - BAJA: solo moroso por deuda (nunca por fecha). La deuda no se elimina.
 *  - REGISTRADO / ARCHIVADO / MOROSO (legacy): sin morosidad propia.
 *  - exentoMorosidad = true: moroso = false y fechaEntradaMorosidad = null,
 *    pero la DEUDA se sigue calculando (valor real). No toca los movimientos.
 *  - fechaEntradaMorosidad = fecha ACTUAL (ahora) de detección de la entrada en
 *    morosidad; NO se usa fechaFin como fecha de entrada automática. Se conserva
 *    mientras siga moroso; se limpia al salir; se renueva al volver a entrar.
 */
object MovimientoMorosidad {

    /**
     * Deuda actual: suma de precioFinal de TODOS los movimientos PENDIENTE.
     * No cuenta los PAGADOS. No depende de fechaFin ni de la fecha actual.
     */
    fun deudaDe(movimientos: List<MovimientoEntity>): Double =
        movimientos
            .filter { it.estado == EstadoMovimiento.PENDIENTE }
            .sumOf { it.precioFinal }

    /**
     * ¿Existe al menos un movimiento PENDIENTE? (morosidad por deuda).
     */
    fun morosidadPorDeuda(movimientos: List<MovimientoEntity>): Boolean =
        movimientos.any { it.estado == EstadoMovimiento.PENDIENTE }

    /**
     * ¿Existe un movimiento PAGADO cuyo período cubre la fecha actual?
     */
    fun tieneCoberturaPagadaActual(movimientos: List<MovimientoEntity>, ahora: Long): Boolean =
        movimientos.any {
            it.estado == EstadoMovimiento.PAGADO &&
                it.fechaInicio <= ahora &&
                ahora <= it.fechaFin
        }

    /**
     * ¿La cobertura PAGADA ya terminó y no hay una nueva que cubra `ahora`?
     * Es la condición de "morosidad por fecha": hubo un período PAGADO que
     * finalizó en el pasado y no existe un período vigente para la fecha actual.
     */
    fun coberturaPagadaTerminada(movimientos: List<MovimientoEntity>, ahora: Long): Boolean {
        if (tieneCoberturaPagadaActual(movimientos, ahora)) return false
        return movimientos.any {
            it.estado == EstadoMovimiento.PAGADO && it.fechaFin < ahora
        }
    }

    /**
     * Detección de morosidad según el estado administrativo del cliente y la
     * excepción manual `exentoMorosidad`. Devuelve también la deuda real.
     */
    fun resultadoDe(
        estado: EstadoCliente,
        movimientos: List<MovimientoEntity>,
        ahora: Long,
        exentoMorosidad: Boolean = false
    ): ResultadoMorosidad {
        val deuda = deudaDe(movimientos)
        val porDeuda = morosidadPorDeuda(movimientos)
        val porFecha = estado == EstadoCliente.ACTIVO &&
            coberturaPagadaTerminada(movimientos, ahora)
        val morosoCalculado = when (estado) {
            EstadoCliente.ACTIVO -> porDeuda || porFecha
            EstadoCliente.BAJA -> porDeuda
            // REGISTRADO / ARCHIVADO / MOROSO (legacy): sin morosidad propia.
            else -> false
        }
        val moroso = if (exentoMorosidad) false else morosoCalculado
        return ResultadoMorosidad(
            moroso = moroso,
            deuda = deuda,
            morosoPorDeuda = porDeuda,
            morosoPorFecha = porFecha
        )
    }

    /**
     * Valores finales a persistir. fechaEntradaMorosidad:
     *  - Si el cliente YA era moroso: se conserva su fecha de entrada previa.
     *  - Si pasa de no moroso a moroso (o vuelve a entrar): `ahora` (fecha de
     *    detección del inicio de la situación). NO se usa fechaFin.
     *  - Si deja de ser moroso: null.
     *  - Si exentoMorosidad = true: moroso = false y fechaEntradaMorosidad = null.
     */
    fun resultadoFinal(
        estado: EstadoCliente,
        movimientos: List<MovimientoEntity>,
        morosoPrevio: Boolean,
        fechaEntradaPrevia: Long?,
        ahora: Long,
        exentoMorosidad: Boolean = false
    ): EstadoFinalMorosidad {
        val resultado = resultadoDe(estado, movimientos, ahora, exentoMorosidad)
        val fechaFinal = when {
            exentoMorosidad -> null
            !resultado.moroso -> null
            morosoPrevio && fechaEntradaPrevia != null -> fechaEntradaPrevia
            else -> ahora
        }
        return EstadoFinalMorosidad(resultado.moroso, fechaFinal)
    }
}
