package com.roberto.gestorpro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.gestorpro.data.entity.ServicioEntity
import com.roberto.gestorpro.data.entity.SesionEntity
import com.roberto.gestorpro.data.firebase.ReservaRemotoRepository
import com.roberto.gestorpro.data.firebase.SesionRemotoRepository
import com.roberto.gestorpro.data.repository.ReservaRepository
import com.roberto.gestorpro.data.repository.SesionRepository
import com.roberto.gestorpro.model.ReservaConCliente
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/**
 * SesionViewModel
 * ---------------
 * ViewModel de la gestión de sesiones del ADMIN.
 * Una sesión pertenece directamente a un servicio (idServicio).
 * Orquesta la programación (generación/regeneración) y el detalle de reservas.
 */
@HiltViewModel
class SesionViewModel @Inject constructor(
    private val sesionRepository: SesionRepository,
    private val reservaRepository: ReservaRepository,
    private val sesionRemotoRepository: SesionRemotoRepository,
    private val reservaRemotoRepository: ReservaRemotoRepository
) : ViewModel() {

    private val _sesiones = MutableStateFlow<List<SesionEntity>>(emptyList())
    val sesiones: StateFlow<List<SesionEntity>> = _sesiones.asStateFlow()

    private val _sesionDetalle = MutableStateFlow<SesionEntity?>(null)
    val sesionDetalle: StateFlow<SesionEntity?> = _sesionDetalle.asStateFlow()

    private val _reservasDetalle = MutableStateFlow<List<ReservaConCliente>>(emptyList())
    val reservasDetalle: StateFlow<List<ReservaConCliente>> = _reservasDetalle.asStateFlow()

    private val _operando = MutableStateFlow(false)
    val operando: StateFlow<Boolean> = _operando.asStateFlow()

    /**
     * _errorSincronizacion / errorSincronizacion
     * ------------------------------------------
     * Mensaje del último fallo de réplica Room -> Firestore de una sesión.
     */
    private val _errorSincronizacion = MutableStateFlow<String?>(null)
    val errorSincronizacion: StateFlow<String?> = _errorSincronizacion.asStateFlow()

    /**
     * _sesionSinSincronizar / sesionSinSincronizar
     * --------------------------------------------
     * Operación remota pendiente de una sesión tras un fallo de sincronización.
     */
    private val _sesionSinSincronizar = MutableStateFlow<PendienteSesion?>(null)
    val sesionSinSincronizar: StateFlow<PendienteSesion?> = _sesionSinSincronizar.asStateFlow()

    /**
     * cargarSesionesPorServicio
     * -------------------------
     * Observa todas las sesiones de un servicio (pasadas y futuras).
     */
    fun cargarSesionesPorServicio(idServicio: Int) {
        viewModelScope.launch {
            sesionRepository.obtenerSesionesPorServicio(idServicio).collect { _sesiones.value = it }
        }
    }

    /**
     * cargarSesion
     * ------------
     * Carga una sesión por su id para mostrarla en el detalle de reservas.
     */
    fun cargarSesion(idSesion: Int) {
        viewModelScope.launch {
            _sesionDetalle.value = sesionRepository.obtenerSesionPorId(idSesion)
        }
    }

    /**
     * cargarReservasSesion
     * --------------------
     * Carga las reservas (con datos del cliente) de una sesión concreta.
     */
    fun cargarReservasSesion(idSesion: Int) {
        viewModelScope.launch {
            _reservasDetalle.value = reservaRepository.obtenerReservasConCliente(idSesion)
        }
    }

    /**
     * actualizarSesion
     * ----------------
     * Guarda los cambios de una sesión concreta (Ver / editar sesión)
     * y replica a Firestore (write-through).
     */
    fun actualizarSesion(sesion: SesionEntity) {
        viewModelScope.launch {
            sesionRepository.actualizarSesion(sesion)
            replicar(PendienteSesion(OperacionSesion.ACTUALIZAR, sesion = sesion))
        }
    }

    /**
     * reintentarSincronizacion
     * ------------------------
     * Repite la última operación remota pendiente de una sesión.
     */
    fun reintentarSincronizacion() {
        val pendiente = _sesionSinSincronizar.value ?: return
        viewModelScope.launch {
            replicar(pendiente)
        }
    }

    /**
     * replicar
     * --------
     * Réplica write-through de una operación de sesión hacia Firestore.
     * Si falla, no revierte el cambio local y deja la operación preparada
     * para el reintento manual.
     */
    private suspend fun replicar(pendiente: PendienteSesion) {
        _errorSincronizacion.value = null
        _sesionSinSincronizar.value = null

        val resultado = when (pendiente.operacion) {
            OperacionSesion.ACTUALIZAR ->
                sesionRemotoRepository.actualizarSesionRemoto(pendiente.sesion!!)
            OperacionSesion.GENERAR ->
                sesionRemotoRepository.sincronizarSesionesGeneradas(
                    pendiente.servicio!!.idServicio,
                    pendiente.desde!!,
                    pendiente.sesionesNuevas
                )
        }

        if (!resultado.exito) {
            _errorSincronizacion.value =
                "Cambio guardado en el dispositivo, pero no sincronizado con la nube: ${resultado.mensaje}"
            _sesionSinSincronizar.value = pendiente
        }
    }

    /**
     * generarSesiones
     * ---------------
     * Regenera la programación de un servicio: primero elimina las sesiones
     * futuras (y sus reservas en Room) y después genera las nuevas sesiones
     * dentro del intervalo [desde, hasta]. Cada día seleccionado usa su propia
     * hora. plazasDisponibles = capacidad. Las sesiones pasadas se conservan.
     * La programación resultante se sincroniza con Firestore.
     */
    fun generarSesiones(
        servicio: ServicioEntity,
        desde: Long,
        hasta: Long,
        horariosPorDia: Map<DayOfWeek, String>,
        duracionMinutos: Int,
        capacidad: Int
    ) {
        viewModelScope.launch {
            _operando.value = true
            try {
                val inicioHoy = ServicioViewModel.inicioDeHoy()

                val nuevas = generar(
                    servicio = servicio,
                    desde = desde,
                    hasta = hasta,
                    horariosPorDia = horariosPorDia,
                    duracionMinutos = duracionMinutos,
                    capacidad = capacidad
                )

                // Atómico en Room: elimina reservas de las futuras eliminadas,
                // elimina esas sesiones futuras y crea las nuevas.
                reservaRepository.regenerarProgramacion(
                    idServicio = servicio.idServicio,
                    desde = inicioHoy,
                    nuevas = nuevas
                )
                // Remoto: primero reservas de las futuras, después programación.
                val reservas = reservaRemotoRepository
                    .eliminarReservasDeSesionesFuturasDelServicioRemoto(servicio.idServicio, inicioHoy)
                if (reservas.exito) {
                    replicar(
                        PendienteSesion(
                            operacion = OperacionSesion.GENERAR,
                            servicio = servicio,
                            desde = inicioHoy,
                            sesionesNuevas = nuevas
                        )
                    )
                } else {
                    _errorSincronizacion.value =
                        "Cambio guardado en el dispositivo, pero no sincronizado con la nube: ${reservas.mensaje}"
                }
            } finally {
                _operando.value = false
            }
        }
    }

    /**
     * eliminarSesion
     * --------------
     * Elimina una sesión individual de forma atómica en Room (reservas + sesión)
     * y replica a Firestore: primero sus reservas y después la sesión.
     */
    fun eliminarSesion(idSesion: Int) {
        viewModelScope.launch {
            _operando.value = true
            try {
                reservaRepository.eliminarSesionConReservas(idSesion)
                val reservas = reservaRemotoRepository.eliminarReservasDeSesionRemoto(idSesion)
                if (!reservas.exito) {
                    _errorSincronizacion.value =
                        "Cambio guardado en el dispositivo, pero no sincronizado con la nube: ${reservas.mensaje}"
                    return@launch
                }
                val resultado = sesionRemotoRepository.eliminarSesionRemoto(idSesion)
                if (!resultado.exito) {
                    _errorSincronizacion.value =
                        "Cambio guardado en el dispositivo, pero no sincronizado con la nube: ${resultado.mensaje}"
                }
            } finally {
                _operando.value = false
            }
        }
    }

    /**
     * limpiarDetalle
     * --------------
     * Limpia la sesión y reservas del detalle al abandonar la pantalla.
     */
    fun limpiarDetalle() {
        _sesionDetalle.value = null
        _reservasDetalle.value = emptyList()
    }

    /**
     * generar
     * -------
     * Construye la lista de SesionEntity para el intervalo y días indicados.
     */
    private fun generar(
        servicio: ServicioEntity,
        desde: Long,
        hasta: Long,
        horariosPorDia: Map<DayOfWeek, String>,
        duracionMinutos: Int,
        capacidad: Int
    ): List<SesionEntity> {
        val fechaInicio = Instant.ofEpochMilli(desde)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val fechaFin = Instant.ofEpochMilli(hasta)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val sesiones = mutableListOf<SesionEntity>()
        var fecha = fechaInicio
        while (!fecha.isAfter(fechaFin)) {
            val hora = horariosPorDia[fecha.dayOfWeek]
            if (hora != null) {
                sesiones.add(
                    SesionEntity(
                        negocioId = servicio.negocioId,
                        idServicio = servicio.idServicio,
                        fecha = fecha.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        hora = hora,
                        duracionMinutos = duracionMinutos,
                        capacidad = capacidad,
                        plazasDisponibles = capacidad
                    )
                )
            }
            fecha = fecha.plusDays(1)
        }
        return sesiones
    }

    /**
     * OperacionSesion
     * ---------------
     * Operación remota pendiente de una sesión para el reintento manual.
     */
    enum class OperacionSesion {
        ACTUALIZAR, GENERAR
    }

    /**
     * PendienteSesion
     * ---------------
     * Datos de la operación remota pendiente de sincronizar.
     */
    data class PendienteSesion(
        val operacion: OperacionSesion,
        val sesion: SesionEntity? = null,
        val servicio: ServicioEntity? = null,
        val desde: Long? = null,
        val sesionesNuevas: List<SesionEntity> = emptyList()
    )
}
