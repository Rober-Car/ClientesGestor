package com.roberto.gestorpro.cliente.model

/**
 * EstadoCliente
 * -------------
 * ✔ TIPO: enum
 * Estados de la ficha del cliente (mismos nombres que el contrato remoto).
 * MOROSO se calcula y nunca se almacena.
 */
enum class EstadoCliente {
    ACTIVO,
    MOROSO,
    BAJA,
    ARCHIVADO,
    REGISTRADO
}
