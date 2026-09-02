package com.roberto.gestorpro.util

import com.roberto.gestorpro.data.entity.ServicioEntity
import com.roberto.gestorpro.model.EstadoMovimiento
import com.roberto.gestorpro.model.MetodoPago
import java.util.Locale

/**
 * MovimientoPrecio
 * ----------------
 * Funciones PURAS de la FASE 3 de ECONOMÍA (movimientos multi-servicio y
 * propuesta de precio). Viven fuera de la UI para poder testearse de forma
 * unitaria y para que los diálogos de movimiento las reutilicen sin duplicar
 * lógica.
 *
 * Reglas de negocio que encapsulan:
 *  - La propuesta de `precioFinal` es la suma de los precios ACTUALES de los
 *    servicios seleccionados (nunca datos históricos).
 *  - Si el ADMIN modifica manualmente el importe, ese valor manda y no se
 *    sobrescribe de forma automática.
 *  - Solo los servicios ACTIVOS son seleccionables en movimientos nuevos.
 *  - Los servicios ya no activos de un movimiento histórico se conservan
 *    (no se eliminan ni se sustituyen automáticamente).
 */
object MovimientoPrecio {

    /**
     * Suma de los precios actuales de los servicios seleccionados.
     * Es la propuesta inicial de `precioFinal`.
     */
    fun precioSugerido(servicios: List<ServicioEntity>): Double =
        servicios.sumOf { it.precio }

    /**
     * Importe final que debe guardarse:
     *  - si hay una modificación manual (parámetro no nulo) se conserva;
     *  - si no, se propone la suma de los precios actuales de la selección.
     */
    fun precioFinalPropuesto(precioManual: Double?, servicios: List<ServicioEntity>): Double =
        precioManual ?: precioSugerido(servicios)

    /**
     * Filtra SOLO los servicios activos (únicos que pueden seleccionarse en
     * movimientos nuevos).
     */
    fun serviciosSeleccionables(servicios: List<ServicioEntity>): List<ServicioEntity> =
        servicios.filter { it.activo }

    /**
     * De los ids de un movimiento histórico, los que NO son servicios activos
     * actualmente (dados de baja o eliminados). Estos se CONSERVAN al editar:
     * no pueden quitarse por accidente.
     */
    fun idsFijosHistoricos(idsMovimiento: List<Int>, idsActivos: Set<Int>): List<Int> =
        idsMovimiento.filter { it !in idsActivos }

    /**
     * Texto numérico para el campo de precio: "30" (entero) o "12.5" (decimal).
     * No incluye símbolo de moneda para poder volver a parsearse.
     */
    fun precioCampo(valor: Double): String {
        val texto = valor.toString()
        return if (texto.endsWith(".0")) texto.dropLast(2) else texto
    }

    /**
     * Importe legible para mostrar al ADMIN: "30 €" (entero) o "12,50 €".
     */
    fun importeLegible(valor: Double): String =
        if (valor % 1.0 == 0.0) {
            "${valor.toInt()} €"
        } else {
            "${String.format(Locale.ROOT, "%.2f", valor).replace('.', ',')} €"
        }
}

/**
 * DatosPago
 * ---------
 * Resultado de aplicar la transición de estado de pago de un movimiento:
 * estado final + fechaPago final + metodoPago final.
 */
data class DatosPago(
    val estado: EstadoMovimiento,
    val fechaPago: Long?,
    val metodoPago: MetodoPago?
)

/**
 * MovimientoPago
 * --------------
 * Reglas PURAS de la FASE 4 (pago dentro de MovimientoEntity). Testeable sin UI.
 *
 * Comportamiento:
 *  - Un movimiento solo puede estar PENDIENTE o PAGADO (sin pagos parciales).
 *  - Al pasar a PENDIENTE: fechaPago = null y metodoPago = null.
 *  - Al pasar a PAGADO por transición (antes PENDIENTE): fechaPago = ahora salvo
 *    que el ADMIN haya elegido otra fecha; metodoPago = el elegido (opcional).
 *  - Si ya estaba PAGADO y se conserva: se mantiene la fecha elegida/cargada y el
 *    método elegido (nunca se pierde información existente).
 *  - metodoPago NO es obligatorio (null = "Sin especificar").
 */
object MovimientoPago {

    /**
     * MetodoPago a partir de su nombre (null = sin especificar). Tolerante a
     * valores desconocidos.
     */
    fun metodoPagoDe(nombre: String?): MetodoPago? =
        nombre?.let { MetodoPago.entries.firstOrNull { pago -> pago.name == it } }

    /**
     * Texto legible de un método de pago: null → "Sin especificar".
     */
    fun metodoPagoLabel(metodo: MetodoPago?): String =
        metodo?.name ?: "Sin especificar"

    /**
     * Resuelve estado/fecha/método finales según lo marcado por el ADMIN.
     *
     * @param nuevoPagado   si el interruptor "Pago realizado" queda activado.
     * @param eraPagado     estado de PAGADO con el que se abrió el movimiento
     *                      (false en un movimiento NUEVO).
     * @param fechaPagoElegida fecha de pago seleccionada/editada por el ADMIN
     *                      (null si aún no la ha tocado).
     * @param metodoPago    método seleccionado (null = sin especificar).
     * @param ahora         instante actual (fecha por defecto al cobrar).
     */
    fun resolver(
        nuevoPagado: Boolean,
        eraPagado: Boolean,
        fechaPagoElegida: Long?,
        metodoPago: MetodoPago?,
        ahora: Long
    ): DatosPago {
        if (!nuevoPagado) {
            return DatosPago(EstadoMovimiento.PENDIENTE, null, null)
        }
        val fechaPago = when {
            fechaPagoElegida != null -> fechaPagoElegida
            !eraPagado -> ahora // transición PENDIENTE -> PAGADO
            else -> null // seguía PAGADO sin fecha existente (legado: no se inventa)
        }
        return DatosPago(EstadoMovimiento.PAGADO, fechaPago, metodoPago)
    }
}
