package com.roberto.gestorpro.cliente.model

/**
 * Estados que puede representar el indicador del Home.
 *
 * Los dos últimos estados pertenecen a la ficha, no a un periodo de pago.
 */
enum class EstadoIndicadorCliente {
    ACTIVO,
    PAGO_VENCIDO,
    BAJA,
    REGISTRADO,
    ARCHIVADO
}

/**
 * Estado preparado por el ViewModel para que Home solo tenga que representarlo.
 */
data class EstadoHomeCliente(
    val estado: EstadoIndicadorCliente? = null,
    val fechaRelevante: Long? = null,
    val cargando: Boolean = false,
    val error: String? = null
)
