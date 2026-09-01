package com.roberto.gestorpro.cliente.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.roberto.gestorpro.cliente.data.firebase.AutenticacionRepository
import com.roberto.gestorpro.cliente.data.firebase.ClienteRepository
import com.roberto.gestorpro.cliente.data.firebase.DispositivoRepository
import com.roberto.gestorpro.cliente.data.firebase.NegocioRepository
import com.roberto.gestorpro.cliente.data.firebase.PerfilPendiente
import com.roberto.gestorpro.cliente.data.firebase.PerfilPendienteRepository
import com.roberto.gestorpro.cliente.data.firebase.SolicitudRepository
import com.roberto.gestorpro.cliente.data.firebase.VinculacionRepository
import com.roberto.gestorpro.cliente.data.firebase.esperar
import com.roberto.gestorpro.cliente.data.repository.PreferencesRepository
import com.roberto.gestorpro.cliente.model.Cliente
import com.roberto.gestorpro.cliente.model.EstadoCliente
import com.roberto.gestorpro.cliente.model.EstadoHomeCliente
import com.roberto.gestorpro.cliente.model.EstadoIndicadorCliente
import com.roberto.gestorpro.cliente.model.SolicitudBaja
import com.roberto.gestorpro.cliente.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * MainViewModel
 * -------------
 * ✔ TIPO: ViewModel de Hilt
 * Orquesta el flujo de GestorPro Cliente: autenticación, perfil pendiente,
 * vinculación por código+DNI y edición del propio perfil.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val autenticacionRepository: AutenticacionRepository,
    private val negocioRepository: NegocioRepository,
    private val perfilPendienteRepository: PerfilPendienteRepository,
    private val vinculacionRepository: VinculacionRepository,
    private val clienteRepository: ClienteRepository,
    private val dispositivoRepository: DispositivoRepository,
    private val solicitudRepository: SolicitudRepository
) : ViewModel() {

    private val _autenticando = MutableStateFlow(false)
    val autenticando: StateFlow<Boolean> = _autenticando.asStateFlow()

    private val _operandoRemoto = MutableStateFlow(false)
    val operandoRemoto: StateFlow<Boolean> = _operandoRemoto.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _cliente = MutableStateFlow<Cliente?>(null)
    val cliente: StateFlow<Cliente?> = _cliente.asStateFlow()

    private val _estadoHome = MutableStateFlow(EstadoHomeCliente())
    val estadoHome: StateFlow<EstadoHomeCliente> = _estadoHome.asStateFlow()

    private val _perfilPendiente = MutableStateFlow<PerfilPendiente?>(null)
    val perfilPendiente: StateFlow<PerfilPendiente?> = _perfilPendiente.asStateFlow()

    private val _solicitudesBaja = MutableStateFlow<List<SolicitudBaja>>(emptyList())
    val solicitudesBaja: StateFlow<List<SolicitudBaja>> = _solicitudesBaja.asStateFlow()

    private val _cargandoSolicitudesBaja = MutableStateFlow(false)
    val cargandoSolicitudesBaja: StateFlow<Boolean> = _cargandoSolicitudesBaja.asStateFlow()

    private val _operandoSolicitudBaja = MutableStateFlow(false)
    val operandoSolicitudBaja: StateFlow<Boolean> = _operandoSolicitudBaja.asStateFlow()

    private val _errorSolicitudBaja = MutableStateFlow<String?>(null)
    val errorSolicitudBaja: StateFlow<String?> = _errorSolicitudBaja.asStateFlow()

    val themeMode = preferencesRepository.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PreferencesRepository.THEME_SISTEMA
    )

    val idCliente = preferencesRepository.idCliente.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val negocioId = preferencesRepository.negocioId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val dniPendiente = preferencesRepository.dniPendiente.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val nombreNegocio = preferencesRepository.nombreNegocio.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    val logoNegocio = preferencesRepository.logoNegocio.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    /**
     * destinoInicial
     * --------------
     * Decide la pantalla inicial: sin sesión → Login; con sesión y con ficha
     * vinculada → Home; con sesión y perfil pendiente guardado → Home sin
     * vincular; sin perfil → pantalla inicial de vinculación.
     * Con sesión ya existente refresca los datos públicos del negocio desde
     * Firestore antes de decidir (si la lectura falla se mantiene la caché).
     */
    suspend fun destinoInicial(): String {
        if (!autenticacionRepository.haySesionActiva()) {
            return Routes.LOGIN
        }
        cargarEstadoLocal()
        return destinoTrasAutenticar()
    }

    /**
     * destinoTrasAutenticar
     * ---------------------
     * Tras login/registro: si ya tiene ficha → Home; si tiene perfil pendiente
     * guardado → Home sin vincular (no debe volver a pedir el formulario); si
     * aún no tiene perfil → pantalla inicial de vinculación.
     */
    suspend fun destinoTrasAutenticar(): String {
        val id = preferencesRepository.idCliente.first()
        if (id != null) return Routes.HOME
        val dni = preferencesRepository.dniPendiente.first()
        return if (dni == null) Routes.INICIO else Routes.HOME
    }

    /**
     * iniciarSesion
     * -------------
     * Inicia sesión y recarga la ficha vinculada.
     */
    suspend fun iniciarSesion(email: String, contrasena: String): String? {
        _autenticando.value = true
        try {
            val resultado = autenticacionRepository.iniciarSesion(email, contrasena)
            if (!resultado.exito) return resultado.mensaje
            cargarEstadoLocal()
            return null
        } finally {
            _autenticando.value = false
        }
    }

    /**
     * registrarse
     * -----------
     * Crea la cuenta CLIENTE y recarga el estado local.
     */
    suspend fun registrarse(
        email: String,
        contrasena: String,
        contrasenaRepetida: String
    ): String? {
        if (contrasena.length < 6) {
            return "La contraseña debe tener al menos 6 caracteres"
        }
        if (contrasena != contrasenaRepetida) {
            return "Las contraseñas no coinciden"
        }

        _autenticando.value = true
        try {
            val resultado = autenticacionRepository.registrar(email, contrasena)
            if (!resultado.exito) return resultado.mensaje
            cargarEstadoLocal()
            return null
        } finally {
            _autenticando.value = false
        }
    }

    suspend fun enviarCorreoRecuperacion(email: String): String? {
        if (email.isBlank()) return "Introduce tu email"
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "El email no tiene un formato válido"
        }
        _autenticando.value = true
        try {
            val resultado = autenticacionRepository.enviarCorreoRecuperacion(email.trim())
            return if (resultado.exito) null else resultado.mensaje
        } finally {
            _autenticando.value = false
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            autenticacionRepository.cerrarSesion()
        }
    }

    /**
     * cargarEstadoLocal
     * -----------------
     * Recarga idCliente/negocioId desde Firestore (usuarios/{uid}) y sincroniza
     * DataStore, cargando la ficha si está vinculado y refrescando el nombre del
     * negocio desde negocios_publicos (fuente de verdad). Si la lectura falla
     * (sin conexión, error de red…) se conservan los valores ya guardados en
     * DataStore como caché.
     */
    private suspend fun cargarEstadoLocal() {
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val usuarioDoc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .get()
                .esperar()

            val clienteId = usuarioDoc.getLong("clienteId")?.toInt()
            val negocio = usuarioDoc.getString("negocioId")

            if (clienteId != null) {
                preferencesRepository.setIdCliente(clienteId)
                val ficha = clienteRepository.leerFicha(clienteId)
                _cliente.value = ficha
                if (ficha != null) {
                    _estadoHome.value = estadoHomeDe(ficha)
                }
                // Registra el token FCM si hay cliente vinculado (arranque).
                dispositivoRepository.registrarTokenActual()
            } else {
                preferencesRepository.borrarIdCliente()
                _cliente.value = null
                _estadoHome.value = EstadoHomeCliente()
            }
            if (negocio != null) {
                preferencesRepository.setNegocioId(negocio)
                val datos = negocioRepository.obtenerDatosPublicosNegocio(negocio)
                if (datos != null) {
                    preferencesRepository.setNombreNegocio(datos.nombre)
                    preferencesRepository.setLogoNegocio(datos.logo)
                }
            }
        } catch (_: Exception) {
            // Sin conexión o error de lectura: se mantiene la caché de DataStore.
        }
    }

    /**
     * guardarPerfilPendiente
     * ----------------------
     * Guarda el perfil del CLIENTE aún sin negocio en perfiles_pendientes/{uid}
     * y en DataStore (dni pendiente). Devuelve el error o null.
     */
    suspend fun guardarPerfilPendiente(perfil: PerfilPendiente): String? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return "Sin sesión"
        if (perfil.nombre.isBlank() || perfil.apellidos.isBlank()) {
            return "Completa nombre y apellidos"
        }
        if (!perfil.dni.matches(Regex("\\d{8}[A-Za-z]"))) {
            return "El DNI debe tener 8 dígitos y una letra"
        }
        if (perfil.telefono.isBlank()) {
            return "Completa el teléfono"
        }
        if (!perfil.telefono.matches(Regex("[6789]\\d{8}"))) {
            return "El teléfono debe tener 9 dígitos empezando por 6, 7, 8 o 9"
        }
        if (perfil.fechaNacimiento <= 0L) {
            return "Selecciona la fecha de nacimiento"
        }
        if (perfil.fechaNacimiento > System.currentTimeMillis()) {
            return "La fecha de nacimiento no puede ser futura"
        }
        if (perfil.foto.isBlank()) {
            return "Añade una foto del rostro"
        }

        _operandoRemoto.value = true
        try {
            val resultado = perfilPendienteRepository.guardar(uid, perfil)
            if (!resultado.exito) return resultado.mensaje
            preferencesRepository.setDniPendiente(perfil.dni)
            _perfilPendiente.value = perfil
            return null
        } finally {
            _operandoRemoto.value = false
        }
    }

    /**
     * vincularConCodigoYDNI
     * ---------------------
     * Ejecuta la vinculación por código maestro + DNI (VÍA A sobre una ficha
     * creada previamente por el ADMIN).
     * El perfil pendiente es la fuente de verdad en Firestore: el repositorio
     * lo lee directamente. Ante un fallo NO se borra el perfil pendiente (ni en
     * Firestore ni en el estado local) para no perder los datos del usuario.
     */
    suspend fun vincularConCodigoYDNI(codigoMaestro: String, dni: String): String? {
        if (codigoMaestro.isBlank()) return "Introduce el código maestro"
        if (!dni.matches(Regex("\\d{8}[A-Za-z]"))) {
            return "El DNI debe tener 8 dígitos y una letra"
        }

        _operandoRemoto.value = true
        try {
            val resultado = vinculacionRepository.vincularConCodigoYDNI(codigoMaestro, dni)
            if (!resultado.exito) return resultado.mensaje

            resultado.clienteId?.let { preferencesRepository.setIdCliente(it) }
            resultado.negocioId?.let {
                preferencesRepository.setNegocioId(it)
                val datos = negocioRepository.obtenerDatosPublicosNegocio(it)
                if (datos != null) {
                    preferencesRepository.setNombreNegocio(datos.nombre)
                    preferencesRepository.setLogoNegocio(datos.logo)
                }
            }
            preferencesRepository.borrarDniPendiente()
            _perfilPendiente.value = null
            _cliente.value = resultado.clienteId?.let { clienteRepository.leerFicha(it) }
            // Tras vincularse, registra el token FCM del dispositivo.
            dispositivoRepository.registrarTokenActual()
            return null
        } finally {
            _operandoRemoto.value = false
        }
    }

    /**
     * cargarPerfilPendiente
     * ---------------------
     * Carga el perfil pendiente existente (si volvió antes de completar la
     * vinculación) o el dni guardado en DataStore.
     */
    suspend fun cargarPerfilPendiente() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        _perfilPendiente.value = perfilPendienteRepository.leer(uid)
    }

    /**
     * cargarPerfilVista
     * -----------------
     * Carga el perfil a mostrar en Mi perfil / Editar perfil según el estado:
     *   - vinculado (idCliente != null) → lee clientes/{idCliente};
     *   - sin vincular → lee el perfil pendiente de perfiles_pendientes/{uid}.
     */
    suspend fun cargarPerfilVista() {
        val id = preferencesRepository.idCliente.first()
        if (id != null) {
            val ficha = clienteRepository.leerFicha(id)
            _cliente.value = ficha
            if (ficha != null) {
                _estadoHome.value = estadoHomeDe(ficha)
            }
        } else {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            _perfilPendiente.value = perfilPendienteRepository.leer(uid)
        }
    }

    /**
     * cargarMiFicha
     * -------------
     * Carga la ficha del cliente vinculado en _cliente.
     */
    suspend fun cargarMiFicha() {
        val id = preferencesRepository.idCliente.first() ?: return
        val ficha = clienteRepository.leerFicha(id)
        _cliente.value = ficha
        if (ficha != null) {
            _estadoHome.value = estadoHomeDe(ficha)
        }
    }

    /**
     * Refresca la ficha que alimenta el indicador del Home.
     * La pantalla solo inicia esta operación al entrar o volver a primer plano.
     */
    fun refrescarEstadoHome() {
        viewModelScope.launch {
            val id = preferencesRepository.idCliente.first()
            if (id == null) {
                _estadoHome.value = EstadoHomeCliente()
                return@launch
            }

            val anterior = _estadoHome.value
            _estadoHome.value = anterior.copy(cargando = true, error = null)
            try {
                val ficha = clienteRepository.leerFicha(id)
                if (ficha == null) {
                    _estadoHome.value = anterior.copy(
                        cargando = false,
                        error = "No se encontró la ficha del cliente"
                    )
                } else {
                    _cliente.value = ficha
                    _estadoHome.value = estadoHomeDe(ficha)
                }
            } catch (e: Exception) {
                _estadoHome.value = anterior.copy(
                    cargando = false,
                    error = mensajeErrorFicha(e)
                )
            }
        }
    }

    private fun estadoHomeDe(cliente: Cliente): EstadoHomeCliente {
        val ahora = System.currentTimeMillis()
        return when (cliente.estado) {
            EstadoCliente.ACTIVO -> {
                val vencida = cliente.fechaFinActual?.let { it < ahora } == true
                EstadoHomeCliente(
                    estado = if (vencida) {
                        EstadoIndicadorCliente.PAGO_VENCIDO
                    } else {
                        EstadoIndicadorCliente.ACTIVO
                    },
                    fechaRelevante = cliente.fechaFinActual
                )
            }
            EstadoCliente.BAJA -> EstadoHomeCliente(
                estado = EstadoIndicadorCliente.BAJA,
                fechaRelevante = cliente.fechaBaja
            )
            EstadoCliente.REGISTRADO -> EstadoHomeCliente(
                estado = EstadoIndicadorCliente.REGISTRADO
            )
            EstadoCliente.ARCHIVADO -> EstadoHomeCliente(
                estado = EstadoIndicadorCliente.ARCHIVADO
            )
            // MOROSO no forma parte del estado remoto oficial. Se deja sin
            // representación visual para no convertirlo ni falsear el estado.
            EstadoCliente.MOROSO -> EstadoHomeCliente(
                error = "La ficha contiene un estado remoto no compatible"
            )
        }
    }

    private fun mensajeErrorFicha(e: Exception): String = when (e) {
        is FirebaseNetworkException ->
            "No hay conexión con Firebase. Comprueba tu conexión a Internet"
        is FirebaseFirestoreException -> when (e.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                "No tienes permisos para consultar la ficha del cliente"
            else -> "No se pudo actualizar la ficha del cliente"
        }
        else -> "No se pudo actualizar la ficha del cliente"
    }

    /**
     * actualizarMisDatosPersonales
     * ----------------------------
     * Edita solo los campos personales de la propia ficha.
     */
    suspend fun actualizarMisDatosPersonales(
        nombre: String,
        apellidos: String,
        telefono: String,
        email: String?,
        foto: String,
        fechaNacimiento: Long
    ): String? {
        val id = preferencesRepository.idCliente.first() ?: return "Sin ficha vinculada"
        if (nombre.isBlank() || apellidos.isBlank()) {
            return "Completa nombre y apellidos"
        }
        if (telefono.isBlank()) {
            return "Completa el teléfono"
        }
        if (!telefono.matches(Regex("[6789]\\d{8}"))) {
            return "El teléfono debe tener 9 dígitos empezando por 6, 7, 8 o 9"
        }
        if (fechaNacimiento > System.currentTimeMillis()) {
            return "La fecha de nacimiento no puede ser futura"
        }

        _operandoRemoto.value = true
        try {
            val resultado = clienteRepository.actualizarDatosPersonales(
                id, nombre, apellidos, telefono, email, foto, fechaNacimiento
            )
            if (!resultado.exito) return resultado.mensaje
            _cliente.value = clienteRepository.leerFicha(id)
            return null
        } finally {
            _operandoRemoto.value = false
        }
    }

    /**
     * cargarSolicitudesBaja
     * ---------------------
     * Carga las solicitudes de baja propias del cliente vinculado (Firestore).
     */
    fun cargarSolicitudesBaja() {
        viewModelScope.launch {
            val id = preferencesRepository.idCliente.first() ?: return@launch
            val negocio = preferencesRepository.negocioId.first()
                ?.takeIf { it.isNotBlank() } ?: return@launch
            _cargandoSolicitudesBaja.value = true
            _errorSolicitudBaja.value = null
            try {
                _solicitudesBaja.value = solicitudRepository.obtenerSolicitudes(id, negocio)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorSolicitudBaja.value =
                    e.message ?: "No se pudieron cargar las solicitudes"
            } finally {
                _cargandoSolicitudesBaja.value = false
            }
        }
    }

    /**
     * solicitarBaja
     * -------------
     * Crea una solicitud de baja PENDIENTE. Devuelve un error (String?) o null
     * si se creó correctamente. El repositorio rechaza el alta si ya existe una
     * solicitud PENDIENTE, así que no se puede duplicar.
     */
    suspend fun solicitarBaja(motivo: String?): String? {
        val id = preferencesRepository.idCliente.first() ?: return "Sin ficha vinculada"
        val negocio = preferencesRepository.negocioId.first()
            ?.takeIf { it.isNotBlank() } ?: return "Sin negocio vinculado"
        _operandoSolicitudBaja.value = true
        try {
            val resultado = solicitudRepository.crearSolicitudBaja(id, negocio, motivo)
            if (!resultado.exito) return resultado.mensaje
            _mensaje.value = resultado.mensaje
            cargarSolicitudesBaja()
            return null
        } finally {
            _operandoSolicitudBaja.value = false
        }
    }
}
