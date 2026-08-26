package com.roberto.gestorpro.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.gestorpro.data.firebase.AutenticacionRepository
import com.roberto.gestorpro.data.firebase.NegocioRepository
import com.roberto.gestorpro.data.firebase.VinculacionRepository
import com.roberto.gestorpro.data.repository.PreferencesRepository
import com.roberto.gestorpro.model.TipoUsuario
import com.roberto.gestorpro.navigation.EnlacePendiente
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
    private val vinculacionRepository: VinculacionRepository
) : ViewModel() {

    /**
     * _autenticando / autenticando
     * ----------------------------
     * ✔ TIPO: propiedad (private val) → MutableStateFlow<Boolean> y (val) → StateFlow<Boolean>
     * Es el estado que indica si hay una operación de autenticación en curso.
     * Sirve para que Login y Registro desactiven el botón y muestren carga
     * mientras se comunica con Firebase.
     */
    private val _autenticando = MutableStateFlow(false)
    val autenticando: StateFlow<Boolean> = _autenticando.asStateFlow()

    /**
     * _operandoRemoto / operandoRemoto
     * --------------------------------
     * ✔ TIPO: propiedad (private val) → MutableStateFlow<Boolean> y (val) → StateFlow<Boolean>
     * Es el estado que indica si hay una operación remota de negocio o
     * vinculación en curso. Sirve para que CrearNegocioScreen, MiNegocioScreen
     * y VincularClienteScreen desactiven botones y muestren carga.
     */
    private val _operandoRemoto = MutableStateFlow(false)
    val operandoRemoto: StateFlow<Boolean> = _operandoRemoto.asStateFlow()

    /**
     * themeMode
     * ---------
     * ✔ TIPO: propiedad (val) → StateFlow<String>
     * Es el estado observable del modo de tema guardado.
     * Sirve para que MainActivity aplique el tema claro/oscuro/sistema en toda la app.
     */
    val themeMode = preferencesRepository.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PreferencesRepository.THEME_SISTEMA
    )

    /**
     * setThemeMode
     * ------------
     * ✔ TIPO: método (fun) de Kotlin (lanza corrutina)
     * Es la operación que guarda el modo de tema elegido.
     * Sirve para persistir la preferencia desde PreferenciasScreen.
     */
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    /**
     * obtenerTipoUsuario
     * ------------------
     * ✔ TIPO: método (fun) suspend de Kotlin
     * Es la lectura única del tipo de usuario guardado en DataStore.
     * Sirve a AppNavigation para decidir la pantalla inicial al arrancar:
     * null → primera vez (mostrar selección); no null → ir directo al Login.
     */
    suspend fun obtenerTipoUsuario(): TipoUsuario? {
        return preferencesRepository.tipoUsuario.first()
    }

    /**
     * destinoSegunTipo
     * ----------------
     * ✔ TIPO: método (fun) suspend de Kotlin → String
     * Es la ruta del menú principal correspondiente al tipo guardado.
     * Sirve para que Login y Registro naveguen al Home correcto una vez
     * autenticado el usuario.
     */
    suspend fun destinoSegunTipo(): String {
        if (obtenerTipoUsuario() == TipoUsuario.CLIENTE) {
            // Reclamación automática: si llegó un deep link pendiente,
            // el cliente va directo a reclamar su ficha tras autenticarse.
            EnlacePendiente.codigo?.let { token ->
                return Routes.VINCULAR_CLIENTE.replace("{codigo}", token)
            }
            return Routes.HOME_CLIENTE
        }
        return Routes.HOME
    }

    /**
     * destinoInicialSegunSesion
     * -------------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → String
     * Calcula la pantalla inicial combinando DataStore y Firebase:
     * sin tipo → selección; tipo con sesión Firebase activa → Home directo;
     * tipo sin sesión → Login. Sirve a AppNavigation para arrancar en el sitio
     * correcto cuando el SDK restaura automáticamente la sesión.
     */
    suspend fun destinoInicialSegunSesion(): String {
        val tipo = obtenerTipoUsuario()
        return when {
            tipo == null -> Routes.SELECCION_TIPO_USUARIO
            autenticacionRepository.haySesionActiva() -> destinoSegunTipo()
            else -> Routes.LOGIN
        }
    }

    /**
     * haySesionActiva
     * ---------------
     * ✔ TIPO: método (fun) de Kotlin → Boolean
     * Indica si hay sesión de Firebase restaurada en el dispositivo.
     * Sirve a AppNavigation para decidir si un deep link pendiente puede
     * abrirse directamente o debe esperar al login.
     */
    fun haySesionActiva(): Boolean {
        return autenticacionRepository.haySesionActiva()
    }

    /**
     * iniciarSesion
     * -------------
     * ✔ TIPO: método (fun) suspend de Kotlin → String?
     * Delegará en AutenticacionRepository el acceso real con email y contraseña.
     * Devuelve null si todo fue bien o el mensaje de error para mostrar en UI.
     * Sirve como entrada real de la app respetando el campo activo remoto.
     */
    suspend fun iniciarSesion(email: String, contrasena: String): String? {
        _autenticando.value = true
        try {
            val resultado = autenticacionRepository.iniciarSesion(email, contrasena)
            return if (resultado.exito) {
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
     * ✔ TIPO: método (fun) suspend de Kotlin → String?
     * Valida el email antes de llamar a Firebase y delega el envío del correo
     * de restablecimiento en AutenticacionRepository, reutilizando el estado
     * _autenticando para el indicador de carga. Devuelve null si todo fue bien
     * o el mensaje de error para mostrar en la UI.
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
     * ✔ TIPO: método (fun) suspend de Kotlin → String?
     * Crea la cuenta real en Firebase Authentication y el documento usuarios/{uid}
     * con el rol derivado del tipo guardado (ADMINISTRADOR → ADMIN, CLIENTE → CLIENTE).
     * Valida contraseñas antes de llamar al repositorio. Devuelve null si todo fue
     * bien o el mensaje de error para mostrar en UI.
     */
    suspend fun registrarse(
        email: String,
        contrasena: String,
        contrasenaRepetida: String
    ): String? {
        val tipo = obtenerTipoUsuario()
            ?: return "Falta elegir el tipo de usuario"
        if (contrasena.length < 6) {
            return "La contraseña debe tener al menos 6 caracteres"
        }
        if (contrasena != contrasenaRepetida) {
            return "Las contraseñas no coinciden"
        }

        val rol = if (tipo == TipoUsuario.CLIENTE) {
            AutenticacionRepository.ROL_CLIENTE
        } else {
            AutenticacionRepository.ROL_ADMIN
        }

        _autenticando.value = true
        try {
            val resultado = autenticacionRepository.registrar(email, contrasena, rol)
            return if (resultado.exito) {
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
     * ✔ TIPO: método (fun) de Kotlin (lanza corrutina)
     * Cierra la sesión de Firebase Authentication sin borrar DataStore.
     * Sirve a las opciones "Cerrar sesión" de Cuenta y Preferencias.
     */
    fun cerrarSesion() {
        viewModelScope.launch {
            autenticacionRepository.cerrarSesion()
        }
    }

    /**
     * existeNegocioPropio
     * -------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → Boolean
     * Indica si el ADMIN autenticado ya creó su negocio remoto.
     * Sirve a MiNegocioScreen para elegir entre modo alta y modo edición.
     */
    suspend fun existeNegocioPropio(): Boolean {
        return negocioRepository.existeNegocioPropio()
    }

    /**
     * obtenerCodigoMaestroRemoto
     * --------------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → String?
     * Lee el código maestro actual del negocio propio en Firestore.
     * Sirve para precargar el campo en MiNegocioScreen.
     */
    suspend fun obtenerCodigoMaestroRemoto(): String? {
        return negocioRepository.obtenerCodigoMaestro()
    }

    /**
     * crearNegocio
     * ------------
     * ✔ TIPO: método (fun) suspend de Kotlin → String?
     * Crea el negocio remoto (negocios + negocios_publicos + usuarios/{uid})
     * y guarda además el nombre en DataStore para la identidad local.
     * Devuelve null si todo fue bien o el mensaje de error para la UI.
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
     * ✔ TIPO: método (fun) suspend de Kotlin → String?
     * Actualiza el código maestro del negocio en los dos documentos remotos.
     * Cambiarlo no afecta a clientes ya vinculados. Devuelve null si todo fue
     * bien o el mensaje de error para la UI.
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
     * clienteYaVinculado
     * ------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → Boolean
     * Indica si el CLIENTE autenticado ya tiene ficha asignada en Firestore.
     * Un CLIENTE solo puede vincularse una vez. Sirve a VincularClienteScreen.
     */
    suspend fun clienteYaVinculado(): Boolean {
        return vinculacionRepository.estaVinculado()
    }

    /**
     * vincularConCodigoMaestro
     * ------------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → String?
     * Vía A: busca el negocio por su código maestro, crea la ficha propia del
     * cliente con un idCliente aleatorio único y actualiza usuarios/{uid} en
     * una única Transaction. Devuelve null si todo fue bien o el error.
     */
    suspend fun vincularConCodigoMaestro(codigoMaestro: String): String? {
        if (codigoMaestro.isBlank()) return "Introduce el código maestro del negocio"

        _operandoRemoto.value = true
        try {
            val resultado = vinculacionRepository.vincularConCodigoMaestro(codigoMaestro.trim())
            return if (resultado.exito) null else resultado.mensaje
        } finally {
            _operandoRemoto.value = false
        }
    }

    /**
     * reclamarFichaConEnlace
     * ----------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → String?
     * Vía B: reclama la ficha creada por el ADMIN a través de su enlace
     * individual, consumiendo vinculaciones/{codigo} atómicamente.
     * Devuelve null si todo fue bien o el mensaje de error para la UI.
     */
    suspend fun reclamarFichaConEnlace(codigo: String): String? {
        if (codigo.isBlank()) return "Introduce el código del enlace recibido"

        _operandoRemoto.value = true
        try {
            val resultado = vinculacionRepository.reclamarFichaConEnlace(codigo.trim())
            return if (resultado.exito) null else resultado.mensaje
        } finally {
            _operandoRemoto.value = false
        }
    }

    /**
     * guardarTipoUsuario
     * ------------------
     * ✔ TIPO: método (fun) de Kotlin (lanza corrutina)
     * Es la operación que guarda el tipo de usuario elegido.
     * Sirve para persistir la elección hecha en SeleccionTipoUsuarioScreen
     * y no volver a preguntarla en futuras aperturas de la app.
     */
    fun guardarTipoUsuario(tipo: TipoUsuario) {
        viewModelScope.launch {
            preferencesRepository.setTipoUsuario(tipo)
        }
    }

    /**
     * restablecerTipoUsuario
     * ----------------------
     * ✔ TIPO: método (fun) de Kotlin (lanza corrutina)
     * Es la operación que borra el tipo de usuario guardado.
     * Sirve para la opción "Cambiar tipo de usuario" de Configuración > Cuenta:
     * tras borrarla, la pantalla de selección vuelve a mostrarse.
     */
    fun restablecerTipoUsuario() {
        viewModelScope.launch {
            preferencesRepository.restablecerTipoUsuario()
        }
    }

    /**
     * nombreNegocio
     * -------------
     * ✔ TIPO: propiedad (val) → StateFlow<String>
     * Es el estado observable del nombre del negocio guardado.
     * Sirve para que Home y Login muestren el nombre configurado
     * (o "GestorPro" cuando está vacío).
     */
    val nombreNegocio = preferencesRepository.nombreNegocio.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    /**
     * logoNegocio
     * -----------
     * ✔ TIPO: propiedad (val) → StateFlow<String>
     * Es el estado observable de la ruta del logo del negocio.
     * Sirve para que Home y Login carguen el logo con Coil
     * (o muestren el icono por defecto cuando está vacía).
     */
    val logoNegocio = preferencesRepository.logoNegocio.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    /**
     * guardarNombreNegocio
     * --------------------
     * ✔ TIPO: método (fun) de Kotlin (lanza corrutina)
     * Es la operación que guarda el nombre del negocio.
     * Sirve para persistir lo escrito en MiNegocioScreen y verlo
     * reflejado en Home y Login inmediatamente.
     */
    fun guardarNombreNegocio(nombre: String) {
        viewModelScope.launch {
            preferencesRepository.setNombreNegocio(nombre)
        }
    }

    /**
     * guardarLogoNegocio
     * ------------------
     * ✔ TIPO: método (fun) de Kotlin (lanza corrutina)
     * Es la operación que guarda la ruta del logo del negocio.
     * Sirve para persistir el logo elegido en MiNegocioScreen (o vaciarlo
     * al quitarlo) y verlo reflejado en Home y Login inmediatamente.
     */
    fun guardarLogoNegocio(ruta: String) {
        viewModelScope.launch {
            preferencesRepository.setLogoNegocio(ruta)
        }
    }

    /**
     * idClienteSesion
     * ---------------
     * ✔ TIPO: propiedad (val) → StateFlow<Int?> (nullable)
     * Es el estado observable del id del cliente registrado en este dispositivo.
     * Sirve para que Mi perfil reaccione en tiempo real: si hay id muestra la ficha
     * con sus datos y si no lo hay ofrece registrarse.
     */
    val idClienteSesion = preferencesRepository.idClienteSesion.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    /**
     * guardarIdClienteSesion
     * ----------------------
     * ✔ TIPO: método (fun) de Kotlin (lanza corrutina)
     * Es la operación que guarda el id del cliente recién registrado.
     * Sirve para que, tras completar el alta desde Mi perfil, el dispositivo
     * recuerde qué fila de la tabla cliente pertenece a este usuario.
     */
    fun guardarIdClienteSesion(id: Int) {
        viewModelScope.launch {
            preferencesRepository.setIdClienteSesion(id)
        }
    }

    /**
     * borrarIdClienteSesion
     * ---------------------
     * ✔ TIPO: método (fun) de Kotlin (lanza corrutina)
     * Es la operación que borra el id del cliente guardado en DataStore.
     * Sirve para volver al estado "sin registro" cuando el administrador eliminó
     * al cliente, obligando a registrarse de nuevo en el próximo acceso a Mi perfil.
     */
    fun borrarIdClienteSesion() {
        viewModelScope.launch {
            preferencesRepository.borrarIdClienteSesion()
        }
    }
}
