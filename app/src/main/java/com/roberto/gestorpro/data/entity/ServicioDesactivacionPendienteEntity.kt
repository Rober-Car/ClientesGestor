package com.roberto.gestorpro.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ServicioDesactivacionPendienteEntity
 * ------------------------------------
 * Persistencia MÍNIMA y DURABLE de la desactivación (baja) de un servicio cuya
 * cascada remota (eliminar sesiones futuras + reservas y dejar el servicio
 * inactivo) NO se confirmó en Firestore. Es el análogo a
 * `eliminacion_pendiente` (movimientos) para el caso de ACTIVIDADES.
 *
 * Room sigue siendo la fuente de verdad: el servicio ya está `activo=false`
 * localmente y sus sesiones futuras ya se eliminaron en Room, pero en la nube
 * puede quedar el estado antiguo hasta que el reintento converja.
 *
 * NO es una cola de sincronización general: cubre únicamente la operación de
 * desactivación para no perder el reintento al reiniciar la app.
 */
@Entity(tableName = "servicio_desactivacion_pendiente")
data class ServicioDesactivacionPendienteEntity(

    /**
     * idServicio: id del servicio/actividad desactivada (PK). Es el documentId
     * de `servicios/{idServicio}` cuyo estado remoto está pendiente de converger.
     */
    @PrimaryKey
    val idServicio: Int,

    /**
     * desde: frontera temporal ORIGINAL de la baja (inicio del día de la baja,
     * en milisegundos). El reintento elimina SOLO las sesiones que eran futuras
     * respecto a esta frontera, nunca las que eran pasadas en el momento de la
     * baja. NO se recalcula con "ahora" en cada reintento.
     */
    val desde: Long
)
