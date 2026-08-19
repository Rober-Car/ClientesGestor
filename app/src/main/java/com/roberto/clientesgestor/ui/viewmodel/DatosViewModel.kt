package com.roberto.clientesgestor.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.clientesgestor.data.dao.ClienteDao
import com.roberto.clientesgestor.data.dao.GastoDao
import com.roberto.clientesgestor.data.dao.MovimientoDao
import com.roberto.clientesgestor.data.export.ExportManager
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

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
