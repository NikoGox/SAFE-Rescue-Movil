package com.movil.saferescue.data.local.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para crear una instancia de DataStore a nivel de la aplicación
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Clase dedicada a gestionar las preferencias del usuario y la sesión persistente.
 *
 * @param context El contexto de la aplicación, necesario para inicializar DataStore.
 */
class UserPreferences(private val context: Context) {

    private object PreferencesKeys {
        // --- Claves existentes y nuevas ---
        val SAVED_USER_IDENTIFIER = stringPreferencesKey("saved_user_identifier")
        val ACTIVE_USER_ID = longPreferencesKey("active_user_id")
        val SESSION_PERSISTS = booleanPreferencesKey("session_persists")
    }

    // --- Lógica para recordar el identificador (existente) ---

    /**
     * Un Flow que emite el identificador del usuario guardado cada vez que cambia.
     */
    val savedIdentifierFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SAVED_USER_IDENTIFIER]
        }

    /**
     * Guarda el identificador del usuario (email o username) en DataStore.
     */
    suspend fun saveUserIdentifier(identifier: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SAVED_USER_IDENTIFIER] = identifier
        }
    }

    /**
     * Flow que emite el ID del usuario activo (si existe una sesión)
     */
    val activeUserIdFlow: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ACTIVE_USER_ID]
        }

    /**
     * Guarda el ID del usuario en la sesión
     */
    suspend fun saveUserSession(userId: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_USER_ID] = userId
            preferences[PreferencesKeys.SESSION_PERSISTS] = true
        }
    }

    /**
     * Limpia la sesión del usuario
     */
    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.ACTIVE_USER_ID)
            preferences.remove(PreferencesKeys.SESSION_PERSISTS)
        }
    }

    /**
     * Limpia el identificador guardado
     */
    suspend fun clearUserIdentifier() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.SAVED_USER_IDENTIFIER)
        }
    }
}
