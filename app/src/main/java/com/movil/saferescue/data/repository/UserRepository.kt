package com.movil.saferescue.data.repository

import com.movil.saferescue.data.local.foto.FotoDao
import com.movil.saferescue.data.local.foto.FotoEntity
import com.movil.saferescue.data.local.storage.UserPreferences
import com.movil.saferescue.data.local.user.UserDao
import com.movil.saferescue.data.local.user.UserEntity
import com.movil.saferescue.data.local.user.UserProfile
import com.movil.saferescue.domain.validation.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repositorio para manejar los datos de los usuarios.
 * Delega la lógica de DataStore a `UserPreferences`.
 *
 * @param userDao DAO para operaciones de usuario en la base de datos.
 * @param fotoDao DAO para operaciones de fotos.
 * @param userPreferences Clase que gestiona las preferencias del usuario en DataStore.
 */
class UserRepository(
    private val userDao: UserDao,
    private val fotoDao: FotoDao,
    private val userPreferences: UserPreferences
) {
    private val _loggedInUserId = MutableStateFlow<Long?>(null)

    val loggedInUserId: StateFlow<Long?> = _loggedInUserId.asStateFlow()

    val savedIdentifierFlow: Flow<String?> = userPreferences.savedIdentifierFlow

    suspend fun login(identifier: String, pass: String, rememberMe: Boolean): Result<UserEntity> {
        return withContext(Dispatchers.IO) {
            val user = userDao.getByEmailOrUsername(identifier)
            if (user != null && BCrypt.checkpw(pass, user.password)) {
                _loggedInUserId.value = user.id

                if (rememberMe) {
                    userPreferences.saveUserIdentifier(identifier)
                } else {
                    userPreferences.clearUserIdentifier()
                }
                Result.success(user)
            } else {
                Result.failure(Exception("Credenciales inválidas"))
            }
        }
    }

    suspend fun tryLoginFromPreferences(): Result<UserEntity?> {
        val savedIdentifier = userPreferences.savedIdentifierFlow.firstOrNull()
            ?: return Result.success(null)

        return withContext(Dispatchers.IO) {
            val user = userDao.getByEmailOrUsername(savedIdentifier)
            if (user != null) {
                _loggedInUserId.value = user.id
                Result.success(user)
            } else {
                userPreferences.clearUserIdentifier()
                Result.failure(Exception("Usuario guardado no encontrado en la base de datos."))
            }
        }
    }

    suspend fun clearLoggedInUser() {
        _loggedInUserId.value = null
        userPreferences.clearUserIdentifier()
    }

    suspend fun getLoggedInUser(): UserProfile? {
        val currentUserId = _loggedInUserId.value ?: return null
        return withContext(Dispatchers.IO) {
            userDao.getUserProfileById(currentUserId)
        }
    }

    suspend fun updateUser(user: UserEntity): Result<Unit> {
        // La validación se hace en el ViewModel, aquí solo se actualiza.
        return withContext(Dispatchers.IO) {
            try {
                userDao.updateUser(user)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // <<< FUNCIÓN NUEVA: Para actualizar solo el ID de la foto del usuario >>>
    /**
     * Actualiza el 'foto_id' de un usuario específico.
     * Esta función es llamada desde el ViewModel después de guardar una nueva imagen.
     */
    suspend fun updateUserPhoto(userId: Long, newPhotoId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                userDao.updateUserPhotoId(userId, newPhotoId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }


    fun setLoggedInUserId(userId: Long) {
        _loggedInUserId.value = userId
    }

    // <<< CÓDIGO OBSOLETO ELIMINADO >>>
    // La función 'getFotoUrlById' ya no es necesaria, ya que 'getLoggedInUser' devuelve
    // un 'UserProfile' que ya contiene la URL de la foto.

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        pass: String,
        run: String,
        dv: String,
        username: String,
        fotoUrl: String, // La URL de la foto de perfil por defecto
        rol_id: Long
    ): Result<Long> {
        return withContext(Dispatchers.IO) {
            if (userDao.getByEmail(email) != null) {
                return@withContext Result.failure(IllegalArgumentException("Correo ya registrado"))
            }
            if (userDao.getByUsername(username) != null) {
                return@withContext Result.failure(IllegalArgumentException("Nombre de usuario ya en uso"))
            }

            // <<< LÓGICA CORREGIDA: Se adapta a la nueva entidad FotoEntity >>>
            val fotoExistente = fotoDao.getByUrl(fotoUrl)
            val fotoId: Long

            if (fotoExistente != null) {
                fotoId = fotoExistente.id
            } else {
                // Si la foto por defecto no existe, la creamos con el nuevo formato
                val fileName = "DEFAULT_${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}.jpg"
                fotoId = fotoDao.insertFoto(
                    FotoEntity(
                        nombre = fileName,
                        url = fotoUrl
                    )
                )
            }

            val hashedPassword = BCrypt.hashpw(pass, BCrypt.gensalt())
            val nuevoUsuarioId = userDao.insertUsuario(
                UserEntity(
                    name = name,
                    email = email,
                    phone = phone,
                    password = hashedPassword,
                    run = run,
                    dv = dv,
                    username = username,
                    foto_id = fotoId,
                    rol_id = rol_id
                )
            )

            Result.success(nuevoUsuarioId)
        }
    }
}
