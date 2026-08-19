package com.roberto.clientesgestor.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
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
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: THEME_SISTEMA
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }
}
