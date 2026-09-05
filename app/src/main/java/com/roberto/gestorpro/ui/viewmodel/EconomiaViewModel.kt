package com.roberto.gestorpro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.gestorpro.data.entity.GastoEntity
import com.roberto.gestorpro.data.entity.MovimientoEntity
import com.roberto.gestorpro.data.entity.ServicioEntity
import com.roberto.gestorpro.data.repository.ClienteRepository
import com.roberto.gestorpro.data.repository.GastoRepository
import com.roberto.gestorpro.data.repository.MovimientoRepository
import com.roberto.gestorpro.data.repository.ServicioRepository
import com.roberto.gestorpro.model.MetodoPago
import com.roberto.gestorpro.util.MovimientoPago
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class EconomiaViewModel @Inject constructor(
    private val movimientoRepository: MovimientoRepository,
    private val gastoRepository: GastoRepository,
    private val clienteRepository: ClienteRepository,
    private val servicioRepository: ServicioRepository
) : ViewModel() {

    private val _movimientos = MutableStateFlow<List<MovimientoEntity>>(emptyList())
    val movimientos = _movimientos.asStateFlow()

    private val _gastos = MutableStateFlow<List<GastoEntity>>(emptyList())
    val gastos = _gastos.asStateFlow()

    private val _clientesMap = MutableStateFlow<Map<Int, String>>(emptyMap())
    val clientesMap = _clientesMap.asStateFlow()

    private val _serviciosMap = MutableStateFlow<Map<Int, String>>(emptyMap())
    val serviciosMap = _serviciosMap.asStateFlow()

    /** Servicios ACTIVOS del catálogo (para el editor compartido de movimientos). */
    private val _serviciosActivos = MutableStateFlow<List<ServicioEntity>>(emptyList())
    val serviciosActivos = _serviciosActivos.asStateFlow()

    // ---- Selección múltiple de movimientos (solo INGRESOS en Economía) ----
    private val _modoSeleccion = MutableStateFlow(false)
    val modoSeleccion = _modoSeleccion.asStateFlow()

    private val _seleccionadas = MutableStateFlow<Set<Int>>(emptySet())
    val seleccionadas: StateFlow<Set<Int>> = _seleccionadas.asStateFlow()

    // Feedback de sincronización: reutiliza los StateFlow del repositorio F2.
    val errorSincronizacion: StateFlow<String?> = movimientoRepository.errorSincronizacion
    val periodosPendientes: StateFlow<Set<Int>> = movimientoRepository.periodosPendientes

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
        viewModelScope.launch {
            servicioRepository.obtenerTodosLosServicios().collect { lista ->
                _serviciosMap.value = lista.associate { it.idServicio to it.nombre }
                _serviciosActivos.value = lista.filter { it.activo }
            }
        }
    }

    // ---- Acciones del modo selección ----

    fun entrarEnSeleccion(idMovimiento: Int) {
        _modoSeleccion.value = true
        _seleccionadas.value = setOf(idMovimiento)
    }

    fun alternarSeleccion(idMovimiento: Int) {
        _seleccionadas.value = if (idMovimiento in _seleccionadas.value) {
            _seleccionadas.value - idMovimiento
        } else {
            _seleccionadas.value + idMovimiento
        }
    }

    fun salirDeSeleccion() {
        _modoSeleccion.value = false
        _seleccionadas.value = emptySet()
    }

    fun limpiarSeleccion() {
        _seleccionadas.value = emptySet()
    }

    // ---- Operaciones de movimientos (siempre vía MovimientoRepository) ----

    /**
     * Edita UN movimiento reutilizando la lógica individual existente
     * (Room → recálculo morosidad/deuda → réplica → resumen remoto).
     */
    fun editarMovimiento(movimiento: MovimientoEntity) {
        viewModelScope.launch {
            movimientoRepository.actualizarMovimiento(movimiento)
            salirDeSeleccion()
        }
    }

    /**
     * Cambio de estado individual o MASIVO. Construye las entidades con
     * `MovimientoPago.resolverLote` (que respeta la lógica individual de la F4:
     * PAGADO fija fechaPago + método opcional; PENDIENTE limpia fecha/método;
     * los que ya están en el estado objetivo se conservan sin sobrescribir) y
     * delega en `MovimientoRepository.actualizarMovimientos`.
     */
    fun cambiarEstadoMovimientos(
        movimientos: List<MovimientoEntity>,
        pagar: Boolean,
        metodoPago: MetodoPago?
    ) {
        viewModelScope.launch {
            val aCambiar = MovimientoPago.resolverLote(
                movimientos = movimientos,
                pagar = pagar,
                metodoPago = metodoPago,
                ahora = System.currentTimeMillis()
            )
            if (aCambiar.isNotEmpty()) {
                movimientoRepository.actualizarMovimientos(aCambiar)
            }
            salirDeSeleccion()
        }
    }

    /**
     * Eliminación individual o MASIVA delegando exclusivamente en
     * MovimientoRepository (conserva Room, eliminación_pendiente, recálculo,
     * sincronización y reintento offline). Nunca hay delete directo desde la UI.
     */
    fun eliminarMovimientos(movimientos: List<MovimientoEntity>) {
        viewModelScope.launch {
            if (movimientos.isNotEmpty()) {
                movimientoRepository.eliminarMovimientos(movimientos)
            }
            salirDeSeleccion()
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
