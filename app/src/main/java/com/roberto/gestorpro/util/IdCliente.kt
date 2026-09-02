package com.roberto.gestorpro.util

import kotlin.random.Random

/**
 * IdCliente
 * ---------
 * Generador de identificadores de cliente ESTABLES y de ámbito alto, pensado
 * para que el alta del ADMIN no dependa del autoincrement de Room.
 *
 * El autoincrement de Room arranca en 1 en cada instalación/PC: si Firestore ya
 * tiene documentos `clientes/{1,2,...}` de otra instalación del MISMO negocio,
 * un id local pequeño puede colisionar. Por eso los clientes nuevos se crean
 * con un id aleatorio en un rango alto (>= 1.000.000.000), igual que ya hace la
 * VÍA 2 de appCliente. La probabilidad de colisión entre instalaciones es
 * despreciable y, además, la réplica a Firestore se protege contra
 * sobrescrituras de documentos de otro negocio.
 */
object IdCliente {

    /**
     * Rango mínimo: 1.000.000.000. Los ids históricos (autoincrement) quedan
     * por debajo y nunca colisionan con los nuevos.
     */
    const val MINIMO = 1_000_000_000

    /**
     * nuevo
     * -----
     * Devuelve un id candidato nuevo en el rango alto.
     */
    fun nuevo(): Int = Random.nextInt(MINIMO, Int.MAX_VALUE)
}
