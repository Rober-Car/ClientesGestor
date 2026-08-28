package com.roberto.gestorpro.cliente.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.roberto.gestorpro.cliente.data.firebase.AutenticacionRepository
import com.roberto.gestorpro.cliente.data.firebase.ClienteRepository
import com.roberto.gestorpro.cliente.data.firebase.NegocioRepository
import com.roberto.gestorpro.cliente.data.firebase.PerfilPendiente
import com.roberto.gestorpro.cliente.data.firebase.PerfilPendienteRepository
import com.roberto.gestorpro.cliente.data.firebase.VinculacionRepository
import com.roberto.gestorpro.cliente.data.firebase.esperar
import com.roberto.gestorpro.cliente.data.repository.PreferencesRepository
import com.roberto.gestorpro.cliente.model.Cliente
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
    private val clienteRepository: ClienteRepository
) : ViewModel() {

    private val _autenticando = MutableStateFlow(false)
    val autenticando: StateFlow<Boolean> = _autenticando.asStateFlow()

    private val _operandoRemoto = MutableStateFlow(false)
    val operandoRemoto: StateFlow<Boolean> = _operandoRemoto.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _cliente = MutableStateFlow<Cliente?>(null)
    val cliente: StateFlow<Cliente?> = _cliente.asStateFlow()

    private val _perfilPendiente = MutableStateFlow<PerfilPendiente?>(null)
    val perfilPendiente: StateFlow<PerfilPendiente?> = _perfilPendiente.asStateFlow()

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
     * Decide la pantalla inicial: sin sesión → Login; con sesión y sin ficha
     * vinculada → Inicio (código+DNI); con ficha → Home.
     */
    suspend fun destinoInicial(): String {
        if (!autenticacionRepository.haySesionActiva()) {
            return Routes.LOGIN
        }
        val id = preferencesRepository.idCliente.first()
        return if (id == null) Routes.INICIO else Routes.HOME
    }

    /**
     * destinoTrasAutenticar
     * ---------------------
     * Tras login/registro: si ya tiene ficha → Home; si no → Inicio.
     */
    suspend fun destinoTrasAutenticar(): String {
        val id = preferencesRepository.idCliente.first()
        return if (id == null) Routes.INICIO else Routes.HOME
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
     * DataStore, cargando la ficha si está vinculado.
     */
    private suspend fun cargarEstadoLocal() {
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
            _cliente.value = clienteRepository.leerFicha(clienteId)
        } else {
            preferencesRepository.borrarIdCliente()
            _cliente.value = null
        }
        if (negocio != null) {
            preferencesRepository.setNegocioId(negocio)
            val nombre = negocioRepository.obtenerNombreNegocio(negocio)
            preferencesRepository.setNombreNegocio(nombre ?: "")
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
        if (perfil.telefono.isNotBlank() && !perfil.telefono.matches(Regex("[6789]\\d{8}"))) {
            return "El teléfono debe tener 9 dígitos empezando por 6, 7, 8 o 9"
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
     * Ejecuta la vinculación por código maestro + DNI (VÍA 1 o VÍA 2).
     */
    suspend fun vincularConCodigoYDNI(codigoMaestro: String, dni: String): String? {
        if (codigoMaestro.isBlank()) return "Introduce el código maestro"
        if (!dni.matches(Regex("\\d{8}[A-Za-z]"))) {
            return "El DNI debe tener 8 dígitos y una letra"
        }

        _operandoRemoto.value = true
        try {
            val perfil = _perfilPendiente.value
            val resultado = vinculacionRepository.vincularConCodigoYDNI(
                codigoMaestro,
                dni,
                perfil
            )
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            // En todos los casos (éxito o rechazo) el perfil pendiente temporal
            // de VÍA 1/VÍA 2 se borra en el repositorio; aquí limpiamos el estado
            // local para que no quede "perfil completado" de un intento anterior.
            if (uid != null) {
                perfilPendienteRepository.borrar(uid)
                _perfilPendiente.value = null
            }

            if (!resultado.exito) return resultado.mensaje

            resultado.clienteId?.let { preferencesRepository.setIdCliente(it) }
            resultado.negocioId?.let {
                preferencesRepository.setNegocioId(it)
                val nombre = negocioRepository.obtenerNombreNegocio(it)
                preferencesRepository.setNombreNegocio(nombre ?: "")
            }
            preferencesRepository.borrarDniPendiente()

            _cliente.value = resultado.clienteId?.let { clienteRepository.leerFicha(it) }
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
     * cargarMiFicha
     * -------------
     * Carga la ficha del cliente vinculado en _cliente.
     */
    suspend fun cargarMiFicha() {
        val id = preferencesRepository.idCliente.first() ?: return
        _cliente.value = clienteRepository.leerFicha(id)
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
}
