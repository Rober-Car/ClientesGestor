package com.roberto.gestorpro.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.gestorpro.data.dao.ClienteDao
import com.roberto.gestorpro.data.dao.GastoDao
import com.roberto.gestorpro.data.dao.MovimientoDao
import com.roberto.gestorpro.data.export.ExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DatosViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clienteDao: ClienteDao,
    private val movimientoDao: MovimientoDao,
    private val gastoDao: GastoDao
) : ViewModel() {

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    private val _mostrarDialogoRestaurar = MutableStateFlow(false)
    val mostrarDialogoRestaurar: StateFlow<Boolean> = _mostrarDialogoRestaurar

    private var uriPendienteRestaurar: Uri? = null

    fun exportarDatos(uri: Uri) {
        viewModelScope.launch {
            val resultado = ExportManager.exportarDatos(context, uri, clienteDao, movimientoDao, gastoDao)
            _mensaje.value = if (resultado) "Datos exportados correctamente" else "Error al exportar"
        }
    }

    fun importarDatos(uri: Uri) {
        viewModelScope.launch {
            val resultado = ExportManager.importarDatos(context, uri, clienteDao, movimientoDao, gastoDao)
            _mensaje.value = if (resultado) "Datos importados correctamente" else "Error al importar"
        }
    }

    fun solicitarRestaurar(uri: Uri) {
        uriPendienteRestaurar = uri
        _mostrarDialogoRestaurar.value = true
    }

    fun confirmarRestaurar() {
        _mostrarDialogoRestaurar.value = false
        val uri = uriPendienteRestaurar ?: return
        uriPendienteRestaurar = null
        viewModelScope.launch {
            val resultado = ExportManager.restaurarDatos(context, uri, clienteDao, movimientoDao, gastoDao)
            _mensaje.value = if (resultado) "Copia restaurada correctamente" else "Error al restaurar"
        }
    }

    fun cancelarRestaurar() {
        _mostrarDialogoRestaurar.value = false
        uriPendienteRestaurar = null
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
