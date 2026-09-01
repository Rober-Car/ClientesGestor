package com.roberto.gestorpro.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.gestorpro.data.entity.ServicioEntity
import com.roberto.gestorpro.data.entity.SesionEntity
import com.roberto.gestorpro.data.firebase.ReservaRemotoRepository
import com.roberto.gestorpro.data.firebase.ResultadoAutenticacion
import com.roberto.gestorpro.data.firebase.ServicioRemotoRepository
import com.roberto.gestorpro.data.firebase.SesionRemotoRepository
import com.roberto.gestorpro.data.repository.ReservaRepository
import com.roberto.gestorpro.data.repository.SesionRepository
import com.roberto.gestorpro.model.ReservaClienteDetalle
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
    private val reservaRemotoRepository: ReservaRemotoRepository,
    private val servicioRemotoRepository: ServicioRemotoRepository
) : ViewModel() {

    companion object {
        private const val TAG = "DIAG sesiones"
    }

    private val _sesiones = MutableStateFlow<List<SesionEntity>>(emptyList())
    val sesiones: StateFlow<List<SesionEntity>> = _sesiones.asStateFlow()

    private val _sesionDetalle = MutableStateFlow<SesionEntity?>(null)
    val sesionDetalle: StateFlow<SesionEntity?> = _sesionDetalle.asStateFlow()

    private val _reservasDetalle = MutableStateFlow<List<ReservaClienteDetalle>>(emptyList())
    val reservasDetalle: StateFlow<List<ReservaClienteDetalle>> = _reservasDetalle.asStateFlow()

    /**
     * _plazasDisponiblesRemoto / plazasDisponiblesRemoto
     * ---------------------------------------------------
     * Plazas disponibles REALES de una sesión leídas desde Firestore
     * (fuente de verdad de las reservas creadas por appCliente). null =
     * aún sin leer o lectura fallida.
     */
    private val _plazasDisponiblesRemoto = MutableStateFlow<Int?>(null)
    val plazasDisponiblesRemoto: StateFlow<Int?> = _plazasDisponiblesRemoto.asStateFlow()

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
     * _resultadoGeneracion / resultadoGeneracion
     * --------------------------------------------
     * Resultado de la última generación de sesiones: si terminó con éxito
     * (Room insertado + réplica Firestore commit OK) o el mensaje de error
     * real (cascada remota o commit rechazado). null = sin resultado aún.
     */
    private val _resultadoGeneracion = MutableStateFlow<ResultadoGeneracion?>(null)
    val resultadoGeneracion: StateFlow<ResultadoGeneracion?> = _resultadoGeneracion.asStateFlow()

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
     * La fuente de verdad son las reservas creadas por appCliente en
     * Firestore (reservas/{clienteId}_{sesionId}); se consultan
     * directamente desde Firestore, no desde la Room local del Admin.
     */
    fun cargarReservasSesion(idSesion: Int) {
        viewModelScope.launch {
            _reservasDetalle.value = reservaRemotoRepository.obtenerReservasDeSesionRemoto(idSesion)
        }
    }

    /**
     * refrescarPlazasSesion
     * ---------------------
     * Actualiza plazasDisponiblesRemoto con el valor REAL de Firestore
     * (sesiones/{idSesion}) para reflejar las reservas creadas o canceladas
     * por appCliente, que no actualizan la Room local del Admin.
     */
    fun refrescarPlazasSesion(idSesion: Int) {
        viewModelScope.launch {
            _plazasDisponiblesRemoto.value =
                sesionRemotoRepository.obtenerPlazasDisponiblesRemoto(idSesion)
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
     * para el reintento manual. Devuelve el resultado real de la réplica
     * para que el flujo de generación pueda propagar éxito/error a la UI.
     */
    private suspend fun replicar(pendiente: PendienteSesion): ResultadoAutenticacion {
        _errorSincronizacion.value = null
        _sesionSinSincronizar.value = null

        Log.d(TAG, "replicar: operacion=${pendiente.operacion}, " +
            "idServicio=${pendiente.servicio?.idServicio}, desde=${pendiente.desde}, " +
            "sesionesNuevas=${pendiente.sesionesNuevas.size}")

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

        if (resultado.exito) {
            Log.d(TAG, "réplica OK: ${resultado.mensaje}")
        } else {
            Log.e(TAG, "ERROR réplica: exito=${resultado.exito}, mensaje=${resultado.mensaje}")
            _errorSincronizacion.value =
                "Cambio guardado en el dispositivo, pero no sincronizado con la nube: ${resultado.mensaje}"
            _sesionSinSincronizar.value = pendiente
        }

        return resultado
    }

    /**
     * generarSesiones
     * ---------------
     * Regenera la programación de un servicio: primero elimina las sesiones
     * futuras (y sus reservas en Room) y después genera las nuevas sesiones
     * dentro del intervalo [desde, hasta]. Cada día seleccionado usa su propia
     * hora. plazasDisponibles = capacidad. Las sesiones pasadas se conservan.
     * aperturaReservas define la hora desde la que se permite reservar
     * (horaDesdeReserva) para todas las sesiones generadas; null = abierta
     * desde el inicio del día.
     * La programación resultante se sincroniza con Firestore.
     * El resultado (éxito o error real) se publica en resultadoGeneracion
     * para que la pantalla pueda mostrarlo sin depender de Logcat.
     */
    fun generarSesiones(
        servicio: ServicioEntity,
        desde: Long,
        hasta: Long,
        horariosPorDia: Map<DayOfWeek, String>,
        aperturaReservas: String?,
        duracionMinutos: Int,
        capacidad: Int
    ) {
        viewModelScope.launch {
            _operando.value = true
            _errorSincronizacion.value = null
            _sesionSinSincronizar.value = null
            _resultadoGeneracion.value = null
            try {
                val inicioHoy = ServicioViewModel.inicioDeHoy()

                Log.d(TAG, "generarSesiones: idServicio=${servicio.idServicio}, " +
                    "negocioId=${servicio.negocioId}, desde=$desde, hasta=$hasta, " +
                    "dias=${horariosPorDia.keys}, aperturaReservas=$aperturaReservas, " +
                    "duracion=$duracionMinutos, capacidad=$capacidad, inicioHoy=$inicioHoy")

                val nuevas = generar(
                    servicio = servicio,
                    desde = desde,
                    hasta = hasta,
                    horariosPorDia = horariosPorDia,
                    aperturaReservas = aperturaReservas,
                    duracionMinutos = duracionMinutos,
                    capacidad = capacidad
                )

                Log.d(TAG, "sesiones generadas: ${nuevas.size}")
                nuevas.forEach { s ->
                    Log.d(TAG, "  -> idSesion=${s.idSesion}, fecha=${s.fecha}, " +
                        "hora=${s.hora}, apertura=${s.horaDesdeReserva}, " +
                        "idServicio=${s.idServicio}, negocioId=${s.negocioId}")
                }

                Log.d(TAG, "Room: regenerarProgramacion...")
                reservaRepository.regenerarProgramacion(
                    idServicio = servicio.idServicio,
                    desde = inicioHoy,
                    nuevas = nuevas
                )
                Log.d(TAG, "Room OK: ${nuevas.size} sesiones insertadas")

                Log.d(TAG, "Room: leyendo sesiones con IDs reales...")
                val sesionesConIds = sesionRepository.obtenerSesionesFuturasPorServicioSync(
                    servicio.idServicio, inicioHoy
                )
                Log.d(TAG, "Sesiones reales de Room: ${sesionesConIds.size}")
                sesionesConIds.forEach { s ->
                    Log.d(TAG, "  -> idSesion=${s.idSesion}, fecha=${s.fecha}, " +
                        "hora=${s.hora}, apertura=${s.horaDesdeReserva}, " +
                        "idServicio=${s.idServicio}, negocioId=${s.negocioId}")
                }

                if (sesionesConIds.isEmpty() && nuevas.isNotEmpty()) {
                    throw IllegalStateException(
                        "Room no devolvió las sesiones recién generadas (${nuevas.size} esperadas)"
                    )
                }
                if (sesionesConIds.any { it.idSesion <= 0 }) {
                    throw IllegalStateException(
                        "Room devolvió sesiones sin id (idSesion=0); no se replica a sesiones/0"
                    )
                }

                Log.d(TAG, "servicio remoto: asegurando réplica del servicio ${servicio.idServicio}...")
                val servicioRemoto = servicioRemotoRepository.crearServicioRemoto(servicio)
                if (!servicioRemoto.exito) {
                    Log.e(TAG, "ERROR réplica de servicio: ${servicioRemoto.mensaje}")
                    _errorSincronizacion.value =
                        "Cambio guardado en el dispositivo, pero no sincronizado con la nube: ${servicioRemoto.mensaje}"
                    _resultadoGeneracion.value = ResultadoGeneracion(
                        exito = false,
                        mensaje = "No se pudo generar la programación: el servicio no está sincronizado con la nube (${servicioRemoto.mensaje})"
                    )
                    return@launch
                }

                Log.d(TAG, "cascada remota: eliminarSesionesFuturas...")
                val cascada = reservaRemotoRepository
                    .eliminarSesionesFuturasConReservasRemoto(servicio.idServicio, inicioHoy)
                Log.d(TAG, "cascada remota resultado: exito=${cascada.exito}, mensaje=${cascada.mensaje}")

                if (cascada.exito) {
                    Log.d(TAG, "replicar: sincronizarSesionesGeneradas (con IDs reales)...")
                    val replica = replicar(
                        PendienteSesion(
                            operacion = OperacionSesion.GENERAR,
                            servicio = servicio,
                            desde = inicioHoy,
                            sesionesNuevas = sesionesConIds
                        )
                    )
                    _resultadoGeneracion.value = if (replica.exito) {
                        ResultadoGeneracion(
                            exito = true,
                            mensaje = "${sesionesConIds.size} sesiones generadas y sincronizadas con la nube"
                        )
                    } else {
                        ResultadoGeneracion(
                            exito = false,
                            mensaje = "Cambio guardado en el dispositivo, pero no sincronizado con la nube: ${replica.mensaje}"
                        )
                    }
                } else {
                    Log.e(TAG, "ERROR cascada: ${cascada.mensaje}")
                    _errorSincronizacion.value =
                        "Cambio guardado en el dispositivo, pero no sincronizado con la nube: ${cascada.mensaje}"
                    _resultadoGeneracion.value = ResultadoGeneracion(
                        exito = false,
                        mensaje = "No se pudo sincronizar la programación: ${cascada.mensaje}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "ERROR generarSesiones: ${e.message}", e)
                _resultadoGeneracion.value = ResultadoGeneracion(
                    exito = false,
                    mensaje = "Error al generar las sesiones: ${e.message ?: "error desconocido"}"
                )
            } finally {
                _operando.value = false
            }
        }
    }

    /**
     * eliminarSesion
     * --------------
     * Elimina una sesión individual de forma atómica en Room (reservas + sesión)
     * y replica a Firestore con una sola operación remota que borra la sesión
     * junto con todas sus reservas (eliminarSesionConReservasRemoto).
     */
    fun eliminarSesion(idSesion: Int) {
        viewModelScope.launch {
            _operando.value = true
            try {
                reservaRepository.eliminarSesionConReservas(idSesion)
                val resultado = reservaRemotoRepository.eliminarSesionConReservasRemoto(idSesion)
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
     * limpiarResultadoGeneracion
     * ---------------------------
     * Limpia el resultado de la última generación al abandonar la pantalla
     * de programación, para que una reentrada no reaccione a un resultado viejo.
     */
    fun limpiarResultadoGeneracion() {
        _resultadoGeneracion.value = null
    }

    /**
     * generar
     * -------
     * Construye la lista de SesionEntity para el intervalo y días indicados.
     * aperturaReservas aporta el valor de horaDesdeReserva para todas las sesiones
     * generadas (null = abierta desde el inicio del día).
     */
    private fun generar(
        servicio: ServicioEntity,
        desde: Long,
        hasta: Long,
        horariosPorDia: Map<DayOfWeek, String>,
        aperturaReservas: String?,
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
                        plazasDisponibles = capacidad,
                        horaDesdeReserva = aperturaReservas
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

    /**
     * ResultadoGeneracion
     * --------------------
     * Resultado visible de la última generación de sesiones.
     * exito = true si Room insertó y el commit de Firestore terminó bien.
     */
    data class ResultadoGeneracion(
        val exito: Boolean,
        val mensaje: String
    )
}
