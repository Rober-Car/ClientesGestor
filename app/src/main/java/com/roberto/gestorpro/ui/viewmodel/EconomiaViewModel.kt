package com.roberto.gestorpro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.gestorpro.data.entity.GastoEntity
import com.roberto.gestorpro.data.entity.MovimientoEntity
import com.roberto.gestorpro.data.repository.ClienteRepository
import com.roberto.gestorpro.data.repository.GastoRepository
import com.roberto.gestorpro.data.repository.MovimientoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class EconomiaViewModel @Inject constructor(
    private val movimientoRepository: MovimientoRepository,
    private val gastoRepository: GastoRepository,
    private val clienteRepository: ClienteRepository
) : ViewModel() {

    private val _movimientos = MutableStateFlow<List<MovimientoEntity>>(emptyList())
    val movimientos = _movimientos.asStateFlow()

    private val _gastos = MutableStateFlow<List<GastoEntity>>(emptyList())
    val gastos = _gastos.asStateFlow()

    private val _clientesMap = MutableStateFlow<Map<Int, String>>(emptyMap())
    val clientesMap = _clientesMap.asStateFlow()

    fun cargarDatos() {
        viewModelScope.launch {
            clienteRepository.obtenerClientesRepo().collect { clientes ->
                _clientesMap.value = clientes.associate { it.idCliente to "${it.nombre} ${it.apellidos}" }
            }
        }
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
