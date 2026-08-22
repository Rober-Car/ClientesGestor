package com.roberto.gestorpro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.gestorpro.data.repository.PreferencesRepository
import com.roberto.gestorpro.model.TipoUsuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

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
