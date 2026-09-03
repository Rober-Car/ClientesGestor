package com.roberto.gestorpro.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.gestorpro.data.firebase.AutenticacionRepository
import com.roberto.gestorpro.data.firebase.NegocioRepository
import com.roberto.gestorpro.data.repository.MovimientoRepository
import com.roberto.gestorpro.data.repository.PreferencesRepository
import com.roberto.gestorpro.model.TipoUsuario
import com.roberto.gestorpro.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val autenticacionRepository: AutenticacionRepository,
    private val negocioRepository: NegocioRepository,
    private val movimientoRepository: MovimientoRepository
) : ViewModel() {

    /**
     * _autenticando / autenticando
     * ----------------------------
     * Estado que indica si hay una operación de autenticación en curso.
     */
    private val _autenticando = MutableStateFlow(false)
    val autenticando: StateFlow<Boolean> = _autenticando.asStateFlow()

    /**
     * _operandoRemoto / operandoRemoto
     * --------------------------------
     * Estado que indica si hay una operación remota de negocio en curso.
     */
    private val _operandoRemoto = MutableStateFlow(false)
    val operandoRemoto: StateFlow<Boolean> = _operandoRemoto.asStateFlow()

    /**
     * themeMode
     * ---------
     * Modo de tema guardado (claro/oscuro/sistema).
     */
    val themeMode = preferencesRepository.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PreferencesRepository.THEME_SISTEMA
    )

    /**
     * setThemeMode
     * ------------
     * Persiste el modo de tema elegido.
     */
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    /**
     * obtenerTipoUsuario
     * ------------------
     * GestorPro Admin es exclusivamente para administradores: devuelve siempre
     * ADMINISTRADOR, sin depender de la elección previa de DataStore.
     */
    suspend fun obtenerTipoUsuario(): TipoUsuario {
        return TipoUsuario.ADMINISTRADOR
    }

    /**
     * destinoSegunTipo
     * ----------------
     * GestorPro Admin siempre navega al Home del administrador.
     */
    suspend fun destinoSegunTipo(): String {
        return Routes.HOME
    }

    /**
     * destinoInicialSegunSesion
     * -------------------------
     * Pantalla inicial combinando DataStore y Firebase: sin sesión → Login;
     * con sesión restaurada → Home del administrador.
     */
    suspend fun destinoInicialSegunSesion(): String {
        return if (autenticacionRepository.haySesionActiva()) {
            lanzarReintentoDeEliminacionesPendientes()
            Routes.HOME
        } else {
            Routes.LOGIN
        }
    }

    /**
     * lanzarReintentoDeEliminacionesPendientes
     * ----------------------------------------
     * Reintenta al ARRANQUE (o tras autenticarse) los borrados remotos de
     * movimientos que quedaron pendientes persistidos en Room, con
     * independencia de qué pantalla abra después el ADMIN (AJUSTE 1).
     * Es un no-op si no hay sesión o si no hay pendientes.
     */
    private fun lanzarReintentoDeEliminacionesPendientes() {
        viewModelScope.launch {
            movimientoRepository.reintentarEliminacionesPendientesGlobal()
        }
    }

    /**
     * haySesionActiva
     * ---------------
     * Indica si hay sesión de Firebase restaurada en el dispositivo.
     */
    fun haySesionActiva(): Boolean {
        return autenticacionRepository.haySesionActiva()
    }

    /**
     * iniciarSesion
     * -------------
     * Inicia sesión real con email y contraseña.
     */
    suspend fun iniciarSesion(email: String, contrasena: String): String? {
        _autenticando.value = true
        try {
            val resultado = autenticacionRepository.iniciarSesion(email, contrasena)
            return if (resultado.exito) {
                lanzarReintentoDeEliminacionesPendientes()
                null
            } else {
                resultado.mensaje
            }
        } finally {
            _autenticando.value = false
        }
    }

    /**
     * enviarCorreoRecuperacion
     * ------------------------
     * Valida el email antes de llamar a Firebase y delega el envío del correo
     * de restablecimiento.
     */
    suspend fun enviarCorreoRecuperacion(email: String): String? {
        if (email.isBlank()) {
            return "Introduce tu email"
        }
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

    /**
     * registrarse
     * -----------
     * Crea la cuenta real en Firebase Authentication y el documento usuarios/{uid}
     * con el rol ADMIN (GestorPro Admin es exclusivo de administradores).
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
            val resultado = autenticacionRepository.registrar(
                email,
                contrasena,
                AutenticacionRepository.ROL_ADMIN
            )
            return if (resultado.exito) {
                lanzarReintentoDeEliminacionesPendientes()
                null
            } else {
                resultado.mensaje
            }
        } finally {
            _autenticando.value = false
        }
    }

    /**
     * cerrarSesion
     * ------------
     * Cierra la sesión de Firebase Authentication sin borrar DataStore.
     */
    fun cerrarSesion() {
        viewModelScope.launch {
            autenticacionRepository.cerrarSesion()
        }
    }

    /**
     * existeNegocioPropio
     * -------------------
     * Indica si el ADMIN autenticado ya creó su negocio remoto.
     */
    suspend fun existeNegocioPropio(): Boolean {
        return negocioRepository.existeNegocioPropio()
    }

    /**
     * obtenerCodigoMaestroRemoto
     * --------------------------
     * Lee el código maestro actual del negocio propio en Firestore.
     */
    suspend fun obtenerCodigoMaestroRemoto(): String? {
        return negocioRepository.obtenerCodigoMaestro()
    }

    /**
     * crearNegocio
     * ------------
     * Crea el negocio remoto (negocios + negocios_publicos + usuarios/{uid})
     * y guarda además el nombre en DataStore para la identidad local.
     */
    suspend fun crearNegocio(nombre: String, codigoMaestro: String): String? {
        if (nombre.isBlank()) return "El nombre del negocio no puede estar vacío"
        if (codigoMaestro.isBlank()) return "El código maestro no puede estar vacío"

        _operandoRemoto.value = true
        try {
            val resultado = negocioRepository.crearNegocio(nombre.trim(), codigoMaestro.trim())
            if (resultado.exito) {
                preferencesRepository.setNombreNegocio(nombre.trim())
                return null
            }
            return resultado.mensaje
        } finally {
            _operandoRemoto.value = false
        }
    }

    /**
     * guardarCodigoMaestro
     * --------------------
     * Actualiza el código maestro del negocio en los dos documentos remotos.
     */
    suspend fun guardarCodigoMaestro(codigoMaestro: String): String? {
        if (codigoMaestro.isBlank()) return "El código maestro no puede estar vacío"

        _operandoRemoto.value = true
        try {
            val resultado = negocioRepository.guardarCodigoMaestro(codigoMaestro.trim())
            return if (resultado.exito) null else resultado.mensaje
        } finally {
            _operandoRemoto.value = false
        }
    }

    /**
     * nombreNegocio
     * -------------
     * Nombre del negocio guardado en DataStore.
     */
    val nombreNegocio = preferencesRepository.nombreNegocio.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    /**
     * logoNegocio
     * -----------
     * Ruta del logo del negocio guardado en DataStore.
     */
    val logoNegocio = preferencesRepository.logoNegocio.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    /**
     * guardarNombreNegocio
     * --------------------
     * Guarda el nombre del negocio en DataStore.
     */
    fun guardarNombreNegocio(nombre: String) {
        viewModelScope.launch {
            preferencesRepository.setNombreNegocio(nombre)
        }
    }

    /**
     * sincronizarNombreNegocio
     * ------------------------
     * Guarda el nombre del negocio en DataStore (identidad local/offline) y lo
     * sincroniza en Firestore (negocios/{id} y negocios_publicos/{id} en el
     * mismo Batch). Devuelve el error o null si se sincronizó correctamente.
     * Es el método que usa MiNegocioScreen cuando el negocio ya existe en la
     * nube, para que la app Cliente vea el nuevo nombre.
     */
    suspend fun sincronizarNombreNegocio(nombre: String): String? {
        if (nombre.isBlank()) return "El nombre del negocio no puede estar vacío"

        _operandoRemoto.value = true
        try {
            preferencesRepository.setNombreNegocio(nombre.trim())
            val resultado = negocioRepository.guardarNombreNegocio(nombre.trim())
            return if (resultado.exito) null else resultado.mensaje
        } finally {
            _operandoRemoto.value = false
        }
    }

    /**
     * sincronizarLogoNegocio
     * ----------------------
     * Sube el logo local a Firebase Storage, guarda la URL remota en Firestore
     * (negocios/{id} y negocios_publicos/{id}) y actualiza DataStore como caché.
     * Si la subida o el Batch fallan, NO se borra el logo local y se devuelve el
     * error para que la UI lo muestre sin dar la operación por exitosa.
     */
    suspend fun sincronizarLogoNegocio(rutaLocal: String): String? {
        if (rutaLocal.isBlank()) return "No hay logo para subir"

        _operandoRemoto.value = true
        try {
            val resultado = negocioRepository.guardarLogoRemoto(rutaLocal)
            if (!resultado.exito) return resultado.mensaje
            preferencesRepository.setLogoNegocio(resultado.url ?: rutaLocal)
            return null
        } finally {
            _operandoRemoto.value = false
        }
    }

    /**
     * guardarLogoNegocio
     * ------------------
     * Guarda la ruta del logo del negocio en DataStore.
     */
    fun guardarLogoNegocio(ruta: String) {
        viewModelScope.launch {
            preferencesRepository.setLogoNegocio(ruta)
        }
    }

    /**
     * idClienteSesion
     * ---------------
     * Identificador del cliente registrado en este dispositivo (de uso local).
     */
    val idClienteSesion = preferencesRepository.idClienteSesion.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    /**
     * guardarIdClienteSesion
     * ----------------------
     * Guarda el id del cliente recién registrado.
     */
    fun guardarIdClienteSesion(id: Int) {
        viewModelScope.launch {
            preferencesRepository.setIdClienteSesion(id)
        }
    }

    /**
     * borrarIdClienteSesion
     * ---------------------
     * Borra el id del cliente guardado en DataStore.
     */
    fun borrarIdClienteSesion() {
        viewModelScope.launch {
            preferencesRepository.borrarIdClienteSesion()
        }
    }
}
