package com.roberto.clientesgestor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.clientesgestor.data.entity.GastoEntity
import com.roberto.clientesgestor.data.entity.MovimientoEntity
import com.roberto.clientesgestor.data.repository.GastoRepository
import com.roberto.clientesgestor.data.repository.MovimientoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class EconomiaViewModel @Inject constructor(
    private val movimientoRepository: MovimientoRepository,
    private val gastoRepository: GastoRepository
) : ViewModel() {

    private val _movimientos = MutableStateFlow<List<MovimientoEntity>>(emptyList())
    val movimientos = _movimientos.asStateFlow()

    private val _gastos = MutableStateFlow<List<GastoEntity>>(emptyList())
    val gastos = _gastos.asStateFlow()

    fun cargarDatos() {
        viewModelScope.launch {
            movimientoRepository.obtenerTodosLosMovimientos().collect { lista ->
                _movimientos.value = lista
            }
        }
        viewModelScope.launch {
            gastoRepository.obtenerTodosLosGastos().collect { lista ->
                _gastos.value = lista
            }
        }
    }

    fun insertarGasto(gasto: GastoEntity) {
        viewModelScope.launch {
            gastoRepository.insertarGasto(gasto)
        }
    }

    fun actualizarGasto(gasto: GastoEntity) {
        viewModelScope.launch {
            gastoRepository.actualizarGasto(gasto)
        }
    }

    fun eliminarGasto(gasto: GastoEntity) {
        viewModelScope.launch {
            gastoRepository.eliminarGasto(gasto)
        }
    }

    fun obtenerGastoPorId(idGasto: Int, onResultado: (GastoEntity?) -> Unit) {
        viewModelScope.launch {
            val gasto = gastoRepository.obtenerGastoPorId(idGasto)
            onResultado(gasto)
        }
    }
}
