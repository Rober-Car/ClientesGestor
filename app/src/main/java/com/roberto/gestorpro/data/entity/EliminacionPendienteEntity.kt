package com.roberto.gestorpro.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * EliminacionPendienteEntity
 * --------------------------
 * Persistencia MÍNIMA de las eliminaciones de movimiento cuyo `delete` remoto
 * en Firestore falló. Room es la fuente de verdad: el movimiento ya se borró
 * localmente, pero `movimientos/{idMovimiento}` aún existe en la nube hasta que
 * se confirme el borrado remoto.
 *
 * NO es una cola de sincronización general: solo cubre el caso de
 * eliminaciones de movimientos para no perder el reintento al reiniciar la app.
 */
@Entity(tableName = "eliminacion_pendiente")
data class EliminacionPendienteEntity(

    /**
     * idMovimiento: id GLOBAL del movimiento (PK). Es el documentId de
     * `movimientos/{idMovimiento}` cuyo borrado remoto está pendiente.
     */
    @PrimaryKey
    val idMovimiento: Int,

    /**
     * idCliente: cliente al que pertenecía el movimiento (para reintentar por
     * cliente o reconciliar su resumen).
     */
    val idCliente: Int
)
