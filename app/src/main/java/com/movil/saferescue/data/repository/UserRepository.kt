package com.movil.saferescue.data.repository

import android.net.Uri
import com.movil.saferescue.data.local.foto.FotoDao
import com.movil.saferescue.data.local.foto.FotoEntity
import com.movil.saferescue.data.local.storage.UserPreferences
import com.movil.saferescue.data.local.user.UserDao
import com.movil.saferescue.data.local.user.UserEntity
import com.movil.saferescue.data.local.user.UserProfile
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

class UserRepository(
    private val userDao: UserDao,
    private val fotoDao: FotoDao,
    private val userPreferences: UserPreferences
) {
    private val _loggedInUserId = MutableStateFlow<Long?>(null)

    val loggedInUserId: StateFlow<Long?> = _loggedInUserId.asStateFlow()

    val savedIdentifierFlow: Flow<String?> = userPreferences.savedIdentifierFlow

    suspend fun tryLoginFromPreferences(): Result<UserEntity?> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = userPreferences.activeUserIdFlow.firstOrNull()
                if (userId == null) {
                    return@withContext Result.success(null)
                }

                val user = userDao.getUserById(userId)
                if (user != null) {
                    _loggedInUserId.value = userId
                    Result.success(user)
                } else {
                    userPreferences.clearUserSession()
                    userPreferences.clearUserIdentifier()
                    Result.failure(Exception("Usuario no encontrado en la base de datos"))
                }
            } catch (e: Exception) {
                userPreferences.clearUserSession()
                userPreferences.clearUserIdentifier()
                Result.failure(e)
            }
        }
    }

    suspend fun login(identifier: String, pass: String, rememberMe: Boolean): Result<UserEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val user = userDao.getByEmail(identifier) ?: userDao.getByUsername(identifier)
                    ?: return@withContext Result.failure(Exception("Usuario no encontrado"))

                if (!BCrypt.checkpw(pass, user.password)) {
                    return@withContext Result.failure(Exception("Contraseña incorrecta"))
                }

                _loggedInUserId.value = user.id

                if (rememberMe) {
                    userPreferences.saveUserSession(user.id)
                } else {
                    userPreferences.clearUserSession()
                }

                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun clearLoggedInUser() {
        _loggedInUserId.value = null
    }

    suspend fun getLoggedInUser(): UserProfile? {
        val currentUserId = _loggedInUserId.value ?: return null
        return withContext(Dispatchers.IO) {
            userDao.getUserProfileById(currentUserId)
        }
    }

    suspend fun updateUser(user: UserEntity): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                userDao.updateUser(user)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

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

    suspend fun changeUserProfilePicture(userId: Long, imageUri: Uri): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "IMG_${timeStamp}.jpg"
                val newFoto = FotoEntity(
                    nombre = fileName,
                    url = imageUri.toString()
                )

                val newPhotoId = fotoDao.insertFoto(newFoto)

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
        return withContext(Dispatchers.IO) {
            if (userDao.getByEmail(email) != null) {
                return@withContext Result.failure(IllegalArgumentException("Correo ya registrado"))
            }
            if (userDao.getByUsername(username) != null) {
                return@withContext Result.failure(IllegalArgumentException("Nombre de usuario ya en uso"))
            }

            val fotoExistente = fotoDao.getByUrl(fotoUrl)
            val fotoId: Long

            if (fotoExistente != null) {
                fotoId = fotoExistente.id
            } else {
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

    suspend fun getUserByUsername(username: String): UserEntity? {
        return withContext(Dispatchers.IO) {
            userDao.getByUsername(username)
        }
    }

    suspend fun getByEmailOrUsername(identifier: String): UserEntity? {
        return withContext(Dispatchers.IO) {
            userDao.getByEmail(identifier) ?: userDao.getByUsername(identifier)
        }
    }

    suspend fun getUserById(userId: Long): UserEntity? {
        return withContext(Dispatchers.IO) {
            userDao.getUserById(userId)
        }
    }

    suspend fun createUser(user: UserEntity): Result<UserEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = userDao.insertUsuario(user)
                val createdUser = userDao.getUserById(userId)
                    ?: throw Exception("Error al crear el usuario")
                Result.success(createdUser)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
