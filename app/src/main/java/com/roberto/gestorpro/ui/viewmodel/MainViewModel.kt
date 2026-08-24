package com.roberto.gestorpro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.gestorpro.data.firebase.AutenticacionRepository
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
    private val autenticacionRepository: AutenticacionRepository
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
        return if (obtenerTipoUsuario() == TipoUsuario.CLIENTE) {
            Routes.HOME_CLIENTE
        } else {
            Routes.HOME
        }
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
