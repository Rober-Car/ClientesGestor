package com.roberto.gestorpro.util

/**
 * CapacidadSesion
 * ---------------
 * Regla pura del ADMIN al cambiar la CAPACIDAD de una sesión con reservas
 * activas. Mantiene la invariante:
 *
 *     inscritos (reservas activas) + plazasDisponibles = capacidad
 *
 * siempre que el número de reservas no supere la nueva capacidad. Si la nueva
 * capacidad es inferior al número de reservas existentes, se conserva el
 * comportamiento ACTUAL del proyecto (no se inventa una regla nueva): se
 * permite guardar con `plazasDisponibles = 0` (no se impide, no se expulsa ni
 * se cancelan reservas). Las reservas se cuentan por sus documentos reales en
 * Firestore (no por un valor Room potencialmente desactualizado).
 */
object CapacidadSesion {

    /**
     * Plazas disponibles que debe tener la sesión tras fijar la nueva
     * capacidad, dados los inscritos (reservas activas REALES).
     *
     *  - inscritos <= nuevaCapacidad -> plazas = nuevaCapacidad - inscritos
     *  - inscritos >  nuevaCapacidad -> plazas = 0 (regla existente, sin
     *    inventar una nueva decisión de negocio)
     */
    fun plazasDisponiblesTrasCambioCapacidad(
        nuevaCapacidad: Int,
        inscritos: Int
    ): Int = (nuevaCapacidad - inscritos).coerceAtLeast(0)

    /**
     * Número de reservas activas derivado de una sesión Room (capacidad y
     * plazas locales). Solo se usa como RESPALDO cuando no se puede leer el
     * conteo remoto real (p. ej. sin conexión), nunca como fuente principal.
     */
    fun inscritosDesdeDatosLocales(
        capacidadLocal: Int,
        plazasDisponiblesLocal: Int
    ): Int = (capacidadLocal - plazasDisponiblesLocal).coerceAtLeast(0)
}
