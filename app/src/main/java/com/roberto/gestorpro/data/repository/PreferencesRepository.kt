package com.roberto.gestorpro.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.roberto.gestorpro.model.TipoUsuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferencias")

@Singleton
class PreferencesRepository @Inject constructor(
    private val context: Context
) {

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        const val THEME_CLARO = "claro"
        const val THEME_OSCURO = "oscuro"
        const val THEME_SISTEMA = "sistema"

        /**
         * TIPO_USUARIO_KEY
         * ----------------
         * ✔ TIPO: propiedad (private val) → Preferences.Key<String>
         * Es la clave bajo la que se guarda el tipo de usuario elegido en DataStore.
         * Sirve para recordar si la app se usa como Administrador o Cliente;
         * si la clave no existe, es que el usuario aún no ha hecho la elección.
         */
        private val TIPO_USUARIO_KEY = stringPreferencesKey("tipo_usuario")

        /**
         * NOMBRE_NEGOCIO_KEY
         * ------------------
         * ✔ TIPO: propiedad (private val) → Preferences.Key<String>
         * Es la clave bajo la que se guarda el nombre del negocio en DataStore.
         * Sirve para personalizar la app mostrando ese nombre en Home y Login;
         * si está vacío, se muestra "GestorPro" como valor por defecto.
         */
        private val NOMBRE_NEGOCIO_KEY = stringPreferencesKey("nombre_negocio")

        /**
         * LOGO_NEGOCIO_KEY
         * ------------------
         * ✔ TIPO: propiedad (private val) → Preferences.Key<String>
         * Es la clave bajo la que se guarda la ruta del logo del negocio en DataStore.
         * Sirve para mostrar el logo configurado en Home y Login; si está vacía,
         * se muestra el icono por defecto de GestorPro.
         */
        private val LOGO_NEGOCIO_KEY = stringPreferencesKey("logo_negocio")

        /**
         * ID_CLIENTE_SESION_KEY
         * ---------------------
         * ✔ TIPO: propiedad (private val) → Preferences.Key<Int>
         * Es la clave bajo la que se guarda el id del cliente registrado desde este dispositivo.
         * Sirve para vincular el registro hecho por un usuario con perfil CLIENTE
         * (pantalla Mi perfil) con su fila en la tabla cliente; si no existe la clave,
         * es que ese dispositivo aún no tiene registro de cliente y debe mostrarse
         * el formulario de alta.
         */
        private val ID_CLIENTE_SESION_KEY = intPreferencesKey("id_cliente_sesion")
    }

    /**
     * themeMode
     * ---------
     * ✔ TIPO: propiedad (val) → Flow<String>
     * Es el flujo que emite el modo de tema guardado.
     * Sirve para aplicar claro/oscuro/sistema en toda la app.
     */
    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: THEME_SISTEMA
    }

    /**
     * setThemeMode
     * ------------
     * ✔ TIPO: método (fun) suspend de Kotlin
     * Es la operación que guarda el modo de tema en DataStore.
     * Sirve para persistir la preferencia elegida en PreferenciasScreen.
     */
    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }

    /**
     * tipoUsuario
     * -----------
     * ✔ TIPO: propiedad (val) → Flow<TipoUsuario?> (nullable)
     * Es el flujo que emite el tipo de usuario guardado, o null si nunca se eligió.
     * Sirve para decidir al arrancar si mostrar la pantalla de selección
     * ("¿Cómo vas a utilizar GestorPro?") o ir directo al Login.
     * Si el texto guardado no corresponde a ningún valor del enum, devuelve null
     * para que el usuario vuelva a elegir (evita fallos por datos corruptos).
     */
    val tipoUsuario: Flow<TipoUsuario?> = context.dataStore.data.map { preferences ->
        preferences[TIPO_USUARIO_KEY]?.let { guardado ->
            try {
                TipoUsuario.valueOf(guardado)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }

    /**
     * setTipoUsuario
     * --------------
     * ✔ TIPO: método (fun) suspend de Kotlin
     * Es la operación que guarda el tipo de usuario elegido en DataStore.
     * Sirve para persistir la elección hecha en SeleccionTipoUsuarioScreen
     * y no volver a preguntarla en futuras aperturas de la app.
     */
    suspend fun setTipoUsuario(tipo: TipoUsuario) {
        context.dataStore.edit { preferences ->
            preferences[TIPO_USUARIO_KEY] = tipo.name
        }
    }

    /**
     * restablecerTipoUsuario
     * ----------------------
     * ✔ TIPO: método (fun) suspend de Kotlin
     * Es la operación que borra la clave de tipo de usuario de DataStore.
     * Sirve para que, al cambiar el tipo desde Configuración > Cuenta,
     * la pantalla de selección vuelva a mostrarse en el próximo arranque.
     */
    suspend fun restablecerTipoUsuario() {
        context.dataStore.edit { preferences ->
            preferences.remove(TIPO_USUARIO_KEY)
        }
    }

    /**
     * idClienteSesion
     * ---------------
     * ✔ TIPO: propiedad (val) → Flow<Int?> (nullable)
     * Es el flujo que emite el id del cliente registrado en este dispositivo,
     * o null si todavía no se ha registrado ninguno.
     * Sirve para que las pantallas del perfil de cliente reaccionen en tiempo real:
     * si hay id muestran la ficha con sus datos y si no lo hay ofrecen registrarse.
     */
    val idClienteSesion: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[ID_CLIENTE_SESION_KEY]
    }

    /**
     * obtenerIdClienteSesion
     * ----------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → Int?
     * Es la lectura única (sin observar) del id del cliente registrado en este dispositivo.
     * Sirve para comprobaciones puntuales dentro de los ViewModels
     * (por ejemplo al guardar el registro o al entrar en Mi perfil)
     * siguiendo el mismo patrón que obtenerTipoUsuario().
     */
    suspend fun obtenerIdClienteSesion(): Int? {
        return context.dataStore.data.map { preferences ->
            preferences[ID_CLIENTE_SESION_KEY]
        }.first()
    }

    /**
     * setIdClienteSesion
     * ------------------
     * ✔ TIPO: método (fun) suspend de Kotlin
     * Es la operación que guarda el id del cliente recién registrado en DataStore.
     * Sirve para que, tras completar el alta desde la pantalla Mi perfil,
     * el dispositivo recuerde qué fila de la tabla cliente pertenece a este usuario
     * y pueda abrirla directamente en los siguientes accesos.
     */
    suspend fun setIdClienteSesion(id: Int) {
        context.dataStore.edit { preferences ->
            preferences[ID_CLIENTE_SESION_KEY] = id
        }
    }

    /**
     * borrarIdClienteSesion
     * ---------------------
     * ✔ TIPO: método (fun) suspend de Kotlin
     * Es la operación que borra el id del cliente guardado en DataStore.
     * Sirve para volver al estado "sin registro" cuando el administrador elimina
     * al cliente de la base de datos, obligando a registrarse de nuevo
     * la próxima vez que entre en Mi perfil.
     */
    suspend fun borrarIdClienteSesion() {
        context.dataStore.edit { preferences ->
            preferences.remove(ID_CLIENTE_SESION_KEY)
        }
    }

    /**
     * nombreNegocio
     * -------------
     * ✔ TIPO: propiedad (val) → Flow<String>
     * Es el flujo que emite el nombre del negocio guardado (vacío si no se configuró).
     * Sirve para mostrar el nombre en las cabeceras de Home y Login;
     * cuando está vacío, las pantallas muestran "GestorPro" por defecto.
     */
    val nombreNegocio: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[NOMBRE_NEGOCIO_KEY] ?: ""
    }

    /**
     * logoNegocio
     * -----------
     * ✔ TIPO: propiedad (val) → Flow<String>
     * Es el flujo que emite la ruta del archivo del logo del negocio (vacía si no hay logo).
     * Sirve para cargar el logo con Coil en Home y Login; cuando está vacía,
     * esas pantallas muestran el icono por defecto de GestorPro.
     */
    val logoNegocio: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LOGO_NEGOCIO_KEY] ?: ""
    }

    /**
     * setNombreNegocio
     * ----------------
     * ✔ TIPO: método (fun) suspend de Kotlin
     * Es la operación que guarda el nombre del negocio en DataStore.
     * Sirve para persistir lo escrito en MiNegocioScreen y verlo reflejado
     * en Home y Login inmediatamente.
     */
    suspend fun setNombreNegocio(nombre: String) {
        context.dataStore.edit { preferences ->
            preferences[NOMBRE_NEGOCIO_KEY] = nombre.trim()
        }
    }

    /**
     * setLogoNegocio
     * --------------
     * ✔ TIPO: método (fun) suspend de Kotlin
     * Es la operación que guarda la ruta del logo del negocio en DataStore.
     * Sirve para persistir el logo elegido en MiNegocioScreen (o vaciarlo
     * al quitarlo) y verlo reflejado en Home y Login inmediatamente.
     */
    suspend fun setLogoNegocio(ruta: String) {
        context.dataStore.edit { preferences ->
            preferences[LOGO_NEGOCIO_KEY] = ruta
        }
    }
}
