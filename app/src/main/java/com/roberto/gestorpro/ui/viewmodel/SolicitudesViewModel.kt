package com.roberto.gestorpro.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.gestorpro.data.firebase.NotificacionRemotoRepository
import com.roberto.gestorpro.data.firebase.SolicitudRemotoRepository
import com.roberto.gestorpro.data.repository.ClienteRepository
import com.roberto.gestorpro.model.DestinatarioResuelto
import com.roberto.gestorpro.model.EstadoCliente
import com.roberto.gestorpro.model.SolicitudBaja
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * SolicitudesViewModel
 * --------------------
 * ViewModel de las solicitudes de baja del ADMIN.
 *
 * Lista las solicitudes del negocio, acepta (solicitud -> ACEPTADA y cliente
 * -> BAJA de forma atómica en Firestore + actualización local en Room) y
 * rechaza (solicitud -> RECHAZADA, cliente intacto). Al aceptar, si la
 * configuración de notificaciones lo permite, genera la notificación
 * BAJA_CONFIRMADA con la infraestructura existente (Cloud Functions hará el
 * FCM real en el futuro).
 */
@HiltViewModel
class SolicitudesViewModel @Inject constructor(
    private val solicitudRemotoRepository: SolicitudRemotoRepository,
    private val clienteRepository: ClienteRepository,
    private val notificacionRemotoRepository: NotificacionRemotoRepository
) : ViewModel() {

    private val _cargando = MutableStateFlow(false)
    val cargando = _cargando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _solicitudes = MutableStateFlow<List<SolicitudBaja>>(emptyList())
    val solicitudes = _solicitudes.asStateFlow()

    private val _errorSincronizacion = MutableStateFlow<String?>(null)
    val errorSincronizacion = _errorSincronizacion.asStateFlow()

    private val _solicitudSinSincronizar = MutableStateFlow<SolicitudBaja?>(null)
    val solicitudSinSincronizar = _solicitudSinSincronizar.asStateFlow()

    private val _mensajeExito = MutableStateFlow<String?>(null)
    val mensajeExito = _mensajeExito.asStateFlow()

    private var pendienteEsAceptar = false

    companion object {
        private const val TAG = "SolicitudesViewModel"
    }

    /**
     * cargarSolicitudes
     * -----------------
     * Carga las solicitudes del negocio del ADMIN autenticado.
     */
    fun cargarSolicitudes() {
        viewModelScope.launch {
            val negocioId = solicitudRemotoRepository.negocioIdActual()
                ?: run {
                    _error.value = "No hay ninguna sesión activa"
                    return@launch
                }
            _cargando.value = true
            _error.value = null
            try {
                _solicitudes.value = solicitudRemotoRepository.obtenerSolicitudes(negocioId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = e.message ?: "No se pudieron cargar las solicitudes"
            } finally {
                _cargando.value = false
            }
        }
    }

    /**
     * aceptar
     * -------
     * Acepta la solicitud: en Firestore la solicitud pasa a ACEPTADA y el
     * cliente a BAJA (Transaction atómica); después actualiza la ficha local
     * (Room) para mantener la lista coherente y, si la configuración lo
     * permite, crea la notificación BAJA_CONFIRMADA.
     */
    fun aceptar(solicitud: SolicitudBaja) {
        viewModelScope.launch {
            _errorSincronizacion.value = null
            _solicitudSinSincronizar.value = null
            pendienteEsAceptar = true

            val fechaBaja = System.currentTimeMillis()
            val resultado = solicitudRemotoRepository.aceptarBaja(solicitud, fechaBaja)
            if (resultado.exito) {
                actualizarFichaLocal(solicitud, fechaBaja)
                dispararBajaConfirmada(solicitud, fechaBaja)
                _mensajeExito.value = resultado.mensaje
                cargarSolicitudes()
            } else {
                _errorSincronizacion.value = resultado.mensaje
                _solicitudSinSincronizar.value = solicitud
            }
        }
    }

    /**
     * rechazar
     * --------
     * Rechaza la solicitud (solicitud -> RECHAZADA). El cliente permanece como
     * estaba (ACTIVO).
     */
    fun rechazar(solicitud: SolicitudBaja) {
        viewModelScope.launch {
            _errorSincronizacion.value = null
            _solicitudSinSincronizar.value = null
            pendienteEsAceptar = false

            val resultado = solicitudRemotoRepository.rechazarSolicitud(solicitud)
            if (resultado.exito) {
                _mensajeExito.value = resultado.mensaje
                cargarSolicitudes()
            } else {
                _errorSincronizacion.value = resultado.mensaje
                _solicitudSinSincronizar.value = solicitud
            }
        }
    }

    /**
     * reintentarSincronizacion
     * ------------------------
     * Repite la última resolución (aceptar o rechazar) que falló.
     */
    fun reintentarSincronizacion() {
        val pendiente = _solicitudSinSincronizar.value ?: return
        if (pendienteEsAceptar) {
            aceptar(pendiente)
        } else {
            rechazar(pendiente)
        }
    }

    /**
     * consumirMensajeExito
     * --------------------
     * Limpia el mensaje de éxito tras mostrarlo.
     */
    fun consumirMensajeExito() {
        _mensajeExito.value = null
    }

    /**
     * actualizarFichaLocal
     * --------------------
     * Refleja en Room el cambio de estado del cliente al aceptar la baja.
     */
    private suspend fun actualizarFichaLocal(solicitud: SolicitudBaja, fechaBaja: Long) {
        try {
            clienteRepository.obtenerClientePorIdRepo(solicitud.idCliente)?.let { entidad ->
                clienteRepository.actualizarClienteRepo(
                    entidad.copy(
                        estado = EstadoCliente.BAJA,
                        fechaBaja = fechaBaja
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo actualizar la ficha local del cliente ${solicitud.idCliente}", e)
        }
    }

    /**
     * dispararBajaConfirmada
     * ----------------------
     * Si configuracion_notificaciones/{negocioId}.bajaConfirmada.activa está
     * activada, crea la notificación BAJA_CONFIRMADA con la infraestructura
     * existente (reutiliza NotificacionRemotoRepository). Usa el ID determinista
     * que Cloud Functions también usaría (baja_confirmada_{clienteId}_{fechaBaja})
     * para que nunca haya una notificación duplicada. El FCM real lo hará Cloud
     * Functions.
     */
    private suspend fun dispararBajaConfirmada(solicitud: SolicitudBaja, fechaBaja: Long) {
        try {
            val config = notificacionRemotoRepository.obtenerConfiguracion(solicitud.negocioId)
            val firebaseUid = solicitud.firebaseUid?.takeIf { it.isNotBlank() }
            if (config?.bajaConfirmadaActiva != true || firebaseUid == null) return

            val notificacionId = "baja_confirmada_${solicitud.idCliente}_$fechaBaja"
            if (notificacionRemotoRepository.existeNotificacionFinalizada(notificacionId)) return

            notificacionRemotoRepository.crearNotificacion(
                negocioId = solicitud.negocioId,
                titulo = "Baja confirmada",
                mensaje = "Tu baja en el gimnasio ha sido confirmada.",
                modoDestino = "INDIVIDUAL",
                clienteId = solicitud.idCliente,
                destinatarios = listOf(DestinatarioResuelto(solicitud.idCliente, firebaseUid)),
                idsObjetivo = listOf(solicitud.idCliente),
                programada = false,
                fechaProgramada = null,
                tipo = "BAJA_CONFIRMADA",
                origen = "PRECONFIGURADA",
                notificacionId = notificacionId
            )
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo crear la notificación de baja confirmada", e)
        }
    }
}
