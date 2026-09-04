package com.roberto.gestorpro.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.gestorpro.data.database.ClientesDatabase
import com.roberto.gestorpro.data.export.ExportManager
import com.roberto.gestorpro.data.firebase.NegocioRepository
import com.roberto.gestorpro.data.repository.MovimientoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * DatosViewModel
 * --------------
 * ViewModel de la gestión de datos (Exportar / Importar / Restaurar backup v1).
 *
 * Orquesta las operaciones del nuevo motor de backup de ExportManager:
 *   - Exportar: genera el ZIP del negocio actual (Room) con el negocioId de la
 *     cuenta (usuarios/{uid}) y fotografías opcionales.
 *   - Importar (merge): incorpora/actualiza un backup compatible del MISMO
 *     negocio sin borrar lo existente (validado y atómico).
 *   - Restaurar: reemplaza por completo la caché local por un backup compatible
 *     del mismo negocio (confirmación fuerte + atómico).
 *
 * El ViewModel nunca modifica uid_propietario_datos_locales ni ejecuta nada
 * remoto distinto del resumen económico de los clientes afectados (mecanismo
 * existente de MovimientoRepository).
 */
@HiltViewModel
class DatosViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: ClientesDatabase,
    private val movimientoRepository: MovimientoRepository,
    private val negocioRepository: NegocioRepository
) : ViewModel() {

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    private val _esError = MutableStateFlow(false)
    val esError: StateFlow<Boolean> = _esError

    private val _mostrarDialogoImportar = MutableStateFlow(false)
    val mostrarDialogoImportar: StateFlow<Boolean> = _mostrarDialogoImportar

    private val _mostrarDialogoRestaurar = MutableStateFlow(false)
    val mostrarDialogoRestaurar: StateFlow<Boolean> = _mostrarDialogoRestaurar

    private var uriPendienteImportar: Uri? = null
    private var uriPendienteRestaurar: Uri? = null

    /**
     * exportarDatos
     * -------------
     * Exporta el backup ZIP del negocio actual. Requiere que la cuenta tenga
     * negocio creado (se lee de usuarios/{uid}); si no, informa del error.
     */
    fun exportarDatos(uri: Uri, incluirFotos: Boolean) {
        viewModelScope.launch {
            _mensaje.value = null
            val negocioId = negocioRepository.obtenerNegocioIdCuenta()
            val resultado = ExportManager.exportarBackup(
                context = context,
                uri = uri,
                negocioId = negocioId ?: "",
                incluirFotos = incluirFotos,
                db = db
            )
            publicar(resultado)
        }
    }

    /**
     * solicitarImportar
     * -----------------
     * Guarda el URI elegido y muestra la confirmación de importación (merge).
     */
    fun solicitarImportar(uri: Uri) {
        uriPendienteImportar = uri
        _mostrarDialogoImportar.value = true
    }

    /**
     * confirmarImportar
     * -----------------
     * Ejecuta la importación (merge) del backup tras la confirmación. El motor
     * valida la identidad del negocio y aplica todo o nada; si falla la
     * validación no se modifica nada.
     */
    fun confirmarImportar() {
        _mostrarDialogoImportar.value = false
        val uri = uriPendienteImportar ?: return
        uriPendienteImportar = null
        viewModelScope.launch {
            _mensaje.value = null
            val negocioId = negocioRepository.obtenerNegocioIdCuenta()
            val resultado = ExportManager.importarBackup(
                context = context,
                uri = uri,
                negocioIdCuenta = negocioId,
                db = db,
                movimientoRepository = movimientoRepository
            )
            publicar(resultado)
        }
    }

    fun cancelarImportar() {
        _mostrarDialogoImportar.value = false
        uriPendienteImportar = null
    }

    /**
     * solicitarRestaurar
     * ------------------
     * Guarda el URI elegido para restaurar y muestra la confirmación fuerte.
     */
    fun solicitarRestaurar(uri: Uri) {
        uriPendienteRestaurar = uri
        _mostrarDialogoRestaurar.value = true
    }

    /**
     * confirmarRestaurar
     * ------------------
     * Ejecuta la restauración completa tras la confirmación fuerte. El motor
     * valida la identidad del negocio y aplica borrado+inserción de forma
     * atómica; si la validación falla, Room queda intacta.
     */
    fun confirmarRestaurar() {
        _mostrarDialogoRestaurar.value = false
        val uri = uriPendienteRestaurar ?: return
        uriPendienteRestaurar = null
        viewModelScope.launch {
            _mensaje.value = null
            val negocioId = negocioRepository.obtenerNegocioIdCuenta()
            val resultado = ExportManager.restaurarBackup(
                context = context,
                uri = uri,
                negocioIdCuenta = negocioId,
                db = db,
                movimientoRepository = movimientoRepository
            )
            publicar(resultado)
        }
    }

    fun cancelarRestaurar() {
        _mostrarDialogoRestaurar.value = false
        uriPendienteRestaurar = null
    }

    fun limpiarMensaje() {
        _mensaje.value = null
        _esError.value = false
    }

    private fun publicar(resultado: ExportManager.Resultado) {
        _esError.value = !resultado.exito
        _mensaje.value = resultado.mensaje
    }
}
