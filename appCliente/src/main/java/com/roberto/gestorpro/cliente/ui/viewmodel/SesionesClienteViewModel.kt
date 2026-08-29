package com.roberto.gestorpro.cliente.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.gestorpro.cliente.data.firebase.ClienteRepository
import com.roberto.gestorpro.cliente.data.firebase.SesionRepository
import com.roberto.gestorpro.cliente.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * SesionesClienteViewModel
 * ------------------------
 * Orquesta la consulta de las sesiones del DÍA ACTUAL del CLIENTE.
 *
 * Flujo (compatible con las Security Rules):
 *   1. idCliente desde DataStore (null -> cliente no vinculado);
 *   2. ficha clientes/{idCliente} (fuente de verdad de serviciosContratados);
 *   3. por cada servicio contratado: getDoc servicios/{id} para confirmar que
 *      está ACTIVO (si no, se omite);
 *   4. sesiones del servicio con where("idServicio","==",id);
 *   5. filtrar en memoria sesión.fecha == inicio del día actual;
 *   6. ordenar por hora ascendente.
 */
@HiltViewModel
class SesionesClienteViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val clienteRepository: ClienteRepository,
    private val sesionRepository: SesionRepository
) : ViewModel() {

    private val _cargando = MutableStateFlow(true)
    val cargando = _cargando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _noVinculado = MutableStateFlow(false)
    val noVinculado = _noVinculado.asStateFlow()

    private val _sinServicios = MutableStateFlow(false)
    val sinServicios = _sinServicios.asStateFlow()

    private val _sinSesionesHoy = MutableStateFlow(false)
    val sinSesionesHoy = _sinSesionesHoy.asStateFlow()

    private val _sesiones = MutableStateFlow<List<SesionVisible>>(emptyList())
    val sesiones = _sesiones.asStateFlow()

    /**
     * cargar
     * ------
     * (Re)lanza la consulta completa de las clases de hoy.
     */
    fun cargar() {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            _noVinculado.value = false
            _sinServicios.value = false
            _sinSesionesHoy.value = false
            _sesiones.value = emptyList()
            try {
                val idCliente = preferencesRepository.idCliente.first()
                if (idCliente == null) {
                    _noVinculado.value = true
                    return@launch
                }

                // La ficha es la fuente de verdad de los servicios contratados.
                val ficha = clienteRepository.leerFicha(idCliente)
                if (ficha == null) {
                    _error.value = "No se pudieron cargar tus clases de hoy"
                    return@launch
                }

                val contratados = ficha.serviciosContratados.distinct()
                if (contratados.isEmpty()) {
                    _sinServicios.value = true
                    return@launch
                }

                val inicioHoy = inicioDeHoy()
                val visibles = mutableListOf<SesionVisible>()
                for (idServicio in contratados) {
                    // Solo servicios ACTIVOS (un servicio inactivo/eliminado no es legible).
                    val servicio = sesionRepository.obtenerServicioActivo(idServicio) ?: continue
                    val delDia = sesionRepository.obtenerSesionesPorServicio(idServicio)
                        .filter { it.fecha == inicioHoy }
                        .sortedBy { it.hora }
                    delDia.forEach { sesion ->
                        visibles.add(
                            SesionVisible(
                                idSesion = sesion.idSesion,
                                idServicio = sesion.idServicio,
                                nombreServicio = servicio.nombre,
                                hora = sesion.hora,
                                duracionMinutos = sesion.duracionMinutos,
                                capacidad = sesion.capacidad,
                                plazasDisponibles = sesion.plazasDisponibles
                            )
                        )
                    }
                }

                visibles.sortBy { it.hora }
                if (visibles.isEmpty()) {
                    _sinSesionesHoy.value = true
                } else {
                    _sesiones.value = visibles
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "No se pudieron cargar tus clases de hoy"
            } finally {
                _cargando.value = false
            }
        }
    }

    /**
     * reintentar
     * ----------
     * Reintenta la carga de las clases de hoy (p. ej. tras un error de red).
     */
    fun reintentar() {
        cargar()
    }

    companion object {
        /**
         * inicioDeHoy
         * -----------
         * Epoch millis de la medianoche del día actual en la zona local del
         * dispositivo. Usa el MISMO criterio que el Admin
         * (ServicioViewModel.inicioDeHoy) para comparar con sesiones.fecha.
         */
        fun inicioDeHoy(): Long =
            LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}

/**
 * SesionVisible
 * -------------
 * Sesión del día actual lista para mostrarse en la pantalla del CLIENTE.
 * reservadaPorMi queda preparado para la futura fase de reservas (sin uso aún).
 */
data class SesionVisible(
    val idSesion: Int,
    val idServicio: Int,
    val nombreServicio: String,
    val hora: String,
    val duracionMinutos: Int,
    val capacidad: Int,
    val plazasDisponibles: Int,
    val reservadaPorMi: Boolean = false
)
