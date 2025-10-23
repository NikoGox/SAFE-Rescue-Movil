package com.movil.saferescue.data.local.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para crear una instancia de DataStore a nivel de la aplicación
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Clase dedicada a gestionar las preferencias del usuario guardadas en DataStore.
 *
 * @param context El contexto de la aplicación, necesario para inicializar DataStore.
 */
class UserPreferences(private val context: Context) {

    // Define la clave para guardar el identificador del usuario.
    private object PreferencesKeys {
        val SAVED_USER_IDENTIFIER = stringPreferencesKey("saved_user_identifier")
    }

    /**
     * Un Flow que emite el identificador del usuario guardado cada vez que cambia.
     * Emitirá null si no hay ningún identificador guardado.
     */
    val savedIdentifierFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SAVED_USER_IDENTIFIER]
        }

    /**
     * Guarda el identificador del usuario (email o username) en DataStore.
     *
     * @param identifier El identificador a guardar.
     */
    suspend fun saveUserIdentifier(identifier: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SAVED_USER_IDENTIFIER] = identifier
        }
    }

    /**
     * Limpia el identificador del usuario guardado en DataStore.
     * Se usa al cerrar sesión o si el usuario desmarca "Recordarme".
     */
    suspend fun clearUserIdentifier() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.SAVED_USER_IDENTIFIER)
        }
    }
}
