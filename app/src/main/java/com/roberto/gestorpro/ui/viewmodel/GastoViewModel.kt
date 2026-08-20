package com.roberto.gestorpro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.gestorpro.data.entity.GastoEntity
import com.roberto.gestorpro.data.repository.GastoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class GastoViewModel @Inject constructor(
    private val gastoRepository: GastoRepository
) : ViewModel() {

    private val _gastos = MutableStateFlow<List<GastoEntity>>(emptyList())
    val gastos = _gastos.asStateFlow()

    fun cargarGastos() {
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
