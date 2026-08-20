package com.roberto.gestorpro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.gestorpro.data.entity.ClaseEntity
import com.roberto.gestorpro.data.entity.ReservaEntity
import com.roberto.gestorpro.data.entity.SesionClaseEntity
import com.roberto.gestorpro.data.repository.ClaseRepository
import com.roberto.gestorpro.data.repository.ClienteRepository
import com.roberto.gestorpro.data.repository.ReservaRepository
import com.roberto.gestorpro.data.repository.SesionClaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

@HiltViewModel
class ClaseViewModel @Inject constructor(
    private val claseRepository: ClaseRepository,
    private val sesionClaseRepository: SesionClaseRepository,
    private val reservaRepository: ReservaRepository,
    private val clienteRepository: ClienteRepository
) : ViewModel() {

    private val _clases = MutableStateFlow<List<ClaseEntity>>(emptyList())
    val clases = _clases.asStateFlow()

    private val _claseSeleccionada = MutableStateFlow<ClaseEntity?>(null)
    val claseSeleccionada = _claseSeleccionada.asStateFlow()

    private val _sesiones = MutableStateFlow<List<SesionClaseEntity>>(emptyList())
    val sesiones = _sesiones.asStateFlow()

    private val _reservasPorSesion = MutableStateFlow<Map<Int, List<ReservaEntity>>>(emptyMap())
    val reservasPorSesion = _reservasPorSesion.asStateFlow()

    private val _clientesMap = MutableStateFlow<Map<Int, String>>(emptyMap())
    val clientesMap = _clientesMap.asStateFlow()

    fun cargarClases() {
        viewModelScope.launch {
            clienteRepository.obtenerClientesRepo().collect { clientes ->
                _clientesMap.value = clientes.associate { it.idCliente to "${it.nombre} ${it.apellidos}" }
            }
        }
        viewModelScope.launch {
            claseRepository.obtenerTodasLasClases().collect { lista ->
                _clases.value = lista
            }
        }
    }

    fun cargarDetalleClase(idClase: Int) {
        viewModelScope.launch {
            _claseSeleccionada.value = claseRepository.obtenerClasePorId(idClase)
        }
        viewModelScope.launch {
            sesionClaseRepository.obtenerSesionesPorClase(idClase).collect { lista ->
                _sesiones.value = lista
                val mapa = mutableMapOf<Int, List<ReservaEntity>>()
                lista.forEach { sesion ->
                    val reservas = reservaRepository.obtenerReservasPorSesionSync(sesion.idSesion)
                    mapa[sesion.idSesion] = reservas
                }
                _reservasPorSesion.value = mapa
            }
        }
    }

    fun crearClase(clase: ClaseEntity) {
        viewModelScope.launch {
            val id = claseRepository.insertarClase(clase)
            generarSesiones(clase.copy(idClase = id.toInt()))
        }
    }

    fun actualizarClase(clase: ClaseEntity) {
        viewModelScope.launch {
            claseRepository.actualizarClase(clase)
            sesionClaseRepository.eliminarSesionesPorClase(clase.idClase)
            generarSesiones(clase)
            _claseSeleccionada.value = clase
        }
    }

    fun eliminarClase(clase: ClaseEntity) {
        viewModelScope.launch {
            sesionClaseRepository.eliminarSesionesPorClase(clase.idClase)
            claseRepository.eliminarClase(clase)
        }
    }

    private suspend fun generarSesiones(clase: ClaseEntity) {
        val dias = parseDiasSemana(clase.diasSemana)
        val fechaInicio = Instant.ofEpochMilli(clase.fechaInicio)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val fechaFin = fechaInicio.plusMonths(clase.mesesDuracion.toLong())
        val sesiones = mutableListOf<SesionClaseEntity>()

        var fechaActual = fechaInicio
        while (fechaActual.isBefore(fechaFin) || fechaActual.isEqual(fechaFin)) {
            if (dias.contains(fechaActual.dayOfWeek)) {
                sesiones.add(
                    SesionClaseEntity(
                        idClase = clase.idClase,
                        fecha = fechaActual.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        plazasDisponibles = clase.capacidadMaxima
                    )
                )
            }
            fechaActual = fechaActual.plusDays(1)
        }

        if (sesiones.isNotEmpty()) {
            sesionClaseRepository.insertarSesiones(sesiones)
        }
    }

    companion object {
        fun parseDiasSemana(dias: String): Set<DayOfWeek> {
            return dias.split(",").mapNotNull {
                when (it.trim().uppercase()) {
                    "LUN" -> DayOfWeek.MONDAY
                    "MAR" -> DayOfWeek.TUESDAY
                    "MIE" -> DayOfWeek.WEDNESDAY
                    "JUE" -> DayOfWeek.THURSDAY
                    "VIE" -> DayOfWeek.FRIDAY
                    "SAB" -> DayOfWeek.SATURDAY
                    "DOM" -> DayOfWeek.SUNDAY
                    else -> null
                }
            }.toSet()
        }

        fun diasSemanaToString(dias: Set<DayOfWeek>): String {
            return dias.sortedBy { it.value }.joinToString(",") { dia ->
                when (dia) {
                    DayOfWeek.MONDAY -> "LUN"
                    DayOfWeek.TUESDAY -> "MAR"
                    DayOfWeek.WEDNESDAY -> "MIE"
                    DayOfWeek.THURSDAY -> "JUE"
                    DayOfWeek.FRIDAY -> "VIE"
                    DayOfWeek.SATURDAY -> "SAB"
                    DayOfWeek.SUNDAY -> "DOM"
                }
            }
        }
    }
}
