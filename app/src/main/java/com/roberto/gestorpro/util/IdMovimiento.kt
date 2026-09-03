package com.roberto.gestorpro.util

import kotlin.random.Random

/**
 * IdMovimiento
 * ------------
 * Generador de identificadores de movimiento GLOBALES y de ámbito alto, pensado
 * para que dos dispositivos Admin del MISMO negocio NUNCA generen el mismo
 * `movimientos/{id}` (el autoincrement local de Room arranca en 1 por
 * instalación y colisionaría entre dispositivos).
 *
 * Mismo patrón que `IdCliente`: se asigna ANTES de insertar en Room (el
 * movimiento guarda el id global como PK) y Firestore usa ese mismo id como
 * documentId. Los ids históricos (autoincrement) quedan por debajo de
 * `MINIMO` y nunca colisionan con los nuevos.
 */
object IdMovimiento {

    /**
     * Rango mínimo: 1.000.000.000. Los movimientos históricos (autoincrement)
     * quedan por debajo y nunca colisionan con los nuevos.
     */
    const val MINIMO = 1_000_000_000

    /**
     * nuevo
     * -----
     * Devuelve un id candidato nuevo en el rango alto.
     */
    fun nuevo(): Int = Random.nextInt(MINIMO, Int.MAX_VALUE)
}
