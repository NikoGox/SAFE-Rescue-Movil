package com.movil.saferescue.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.movil.saferescue.data.local.foto.FotoDao
import com.movil.saferescue.data.local.foto.FotoEntity
import com.movil.saferescue.data.local.user.UserDao
import com.movil.saferescue.data.local.user.UserEntity
import com.movil.saferescue.data.local.user.UserProfile
import com.movil.saferescue.domain.validation.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// --- 1. LÓGICA DE DATASTORE AÑADIDA ---
// Se crea una extensión de Context para tener una única instancia de DataStore en toda la app.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

// Se definen las claves para acceder a los datos guardados. Es como el nombre de una columna.
private object PreferencesKeys {
    val REMEMBER_ME = booleanPreferencesKey("remember_me_status")
    val SAVED_IDENTIFIER = stringPreferencesKey("saved_user_identifier")
}
// ------------------------------------

// --- 2. CONSTRUCTOR ACTUALIZADO ---
// Ahora necesita el `Context` para poder inicializar y usar DataStore.
class UserRepository(
    private val userDao: UserDao,
    private val fotoDao: FotoDao,
    private val context: Context // Se añade el contexto
) {
    @Volatile
    private var loggedInUserId: Long? = null

    // --- 3. NUEVOS FLUJOS Y FUNCIONES PARA PREFERENCIAS ---

    /**
     * Flujo que emite el último identificador (usuario/email) guardado.
     * El ViewModel lo observará para autocompletar el campo de texto.
     */
    val savedIdentifierFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            // Lee el valor de la clave SAVED_IDENTIFIER. Si no existe, devuelve una cadena vacía.
            preferences[PreferencesKeys.SAVED_IDENTIFIER] ?: ""
        }

    /**
     * Guarda las preferencias de login en DataStore.
     * Esta función se llamará desde la función `login` si las credenciales son correctas.
     */
    private suspend fun saveLoginPreferences(rememberMe: Boolean, identifier: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMEMBER_ME] = rememberMe
            if (rememberMe) {
                // Si el usuario quiere ser recordado, guardamos su identificador.
                preferences[PreferencesKeys.SAVED_IDENTIFIER] = identifier
            } else {
                // Si desmarca la casilla, nos aseguramos de borrar el identificador guardado.
                preferences.remove(PreferencesKeys.SAVED_IDENTIFIER)
            }
        }
    }
    // --------------------------------------------------------

    // --- 4. FUNCIÓN `login` MODIFICADA ---
    // Ahora recibe el estado del checkbox "Recordarme".
    suspend fun login(identifier: String, password: String, rememberMe: Boolean): Result<UserEntity> {
        val user = userDao.getByEmailOrUsername(identifier)
        return if (user != null && user.password == password) {
            setLoggedInUserId(user.id)
            saveLoginPreferences(rememberMe, identifier)
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Credenciales Inválidas"))
        }
    }

    /**
     * Obtiene el usuario logueado y enriquece sus datos con el nombre del rol y la URL de la foto.
     * Devuelve un modelo de dominio `UserProfile` listo para la UI.
     */
    suspend fun getLoggedInUser(): UserProfile? {
        return withContext(Dispatchers.IO) {
            loggedInUserId?.let { id ->
                // Ahora toda la lógica compleja de JOINs está en la DAO
                userDao.getUserProfileById(id)
            }
        }
    }

    suspend fun updateUser(user: UserEntity): Result<Unit> {
        validateNameLettersOnly(user.name)?.let { return Result.failure(IllegalArgumentException(it)) }
        validateUsername(user.username)?.let { return Result.failure(IllegalArgumentException(it)) }
        validatePhoneDigitsOnly(user.phone)?.let { return Result.failure(IllegalArgumentException(it)) }
        validateChileanRUN(user.run, user.dv)?.let { return Result.failure(IllegalArgumentException(it)) }

        return withContext(Dispatchers.IO) {
            try {
                userDao.updateUser(user)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun setLoggedInUserId(userId: Long) {
        loggedInUserId = userId
    }

    // --- 5. FUNCIÓN `clearLoggedInUser` MODIFICADA ---
    // Al cerrar sesión, también borramos el identificador guardado si el usuario no quiere ser recordado.
    fun clearLoggedInUser() {
        loggedInUserId = null
        // Lanzamos una corrutina para actualizar las preferencias en segundo plano.
        // No borramos el identificador si "Recordarme" sigue activo.
        CoroutineScope(Dispatchers.IO).launch {
            val rememberMe = context.dataStore.data.map { it[PreferencesKeys.REMEMBER_ME] ?: false }.first()
            if (!rememberMe) {
                saveLoginPreferences(false, "")
            }
        }
    }

    suspend fun getFotoUrlById(fotoId: Long?): String? {
        if (fotoId == null) return null
        return withContext(Dispatchers.IO) {
            fotoDao.findFotoById(fotoId)?.url
        }
    }

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        pass: String,
        run: String,
        dv: String,
        username: String,
        fotoUrl: String,
        rol_id: Long
    ): Result<Long> {
        if (userDao.getByEmail(email) != null) {
            return Result.failure(IllegalArgumentException("Correo ya registrado"))
        }
        if (userDao.getByUsername(username) != null) {
            return Result.failure(IllegalArgumentException("Nombre de usuario ya en uso"))
        }

        val fotoExistente = fotoDao.getByUrl(fotoUrl)
        val fotoId: Long

        if (fotoExistente != null) {
            fotoId = fotoExistente.id
        } else {
            fotoId = fotoDao.insert(FotoEntity(url = fotoUrl, fechaSubida = System.currentTimeMillis()))
        }

        val nuevoUsuarioId = userDao.insertUsuario(
            UserEntity(
                name = name,
                email = email,
                phone = phone,
                password = pass,
                run = run,
                dv = dv,
                username = username,
                foto_id = fotoId,
                rol_id = rol_id
            )
        )

        return Result.success(nuevoUsuarioId)
    }
}
