package com.movil.saferescue.data.repository

import androidx.compose.ui.semantics.password
import com.movil.saferescue.data.local.foto.FotoDao
import com.movil.saferescue.data.local.foto.FotoEntity
import com.movil.saferescue.data.local.user.UserDao
import com.movil.saferescue.data.local.user.UserEntity
// --- CAMBIO 1: Importar los validadores ---
import com.movil.saferescue.domain.validation.validateConfirm
import com.movil.saferescue.domain.validation.validateStrongPassword
import com.movil.saferescue.domain.validation.validateChileanRUN
import com.movil.saferescue.domain.validation.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val userDao: UserDao,
    private val fotoDao: FotoDao
) {
    // --- CAMBIO 2: Variable para mantener el ID del usuario logueado ---
    // Usamos 'Volatile' para asegurar que el valor sea siempre el más reciente entre hilos.
    @Volatile
    private var loggedInUserId: Long? = null

    // Orquesta el login y guarda el ID del usuario si es exitoso
    suspend fun login(identifier: String, password: String): Result<UserEntity> {
        // Permitimos login con email o username
        val user = userDao.getByEmailOrUsername(identifier)
        return if (user != null && user.password == password) {
            // Guardamos el ID del usuario que ha iniciado sesión correctamente
            setLoggedInUserId(user.id)
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Credenciales Inválidas"))
        }
    }

    // --- CAMBIO 3: Nueva función para obtener el usuario logueado ---
    /**
     * Obtiene los datos del usuario que ha iniciado sesión actualmente.
     * Usa el ID guardado durante el login.
     */
    suspend fun getLoggedInUser(): UserEntity? {
        return withContext(Dispatchers.IO) {
            loggedInUserId?.let { id ->
                userDao.getUserById(id)
            }
        }
    }

    // --- CAMBIO 4: Nueva función para actualizar un usuario ---
    /**
     * Actualiza los datos de un usuario en la base de datos.
     * Primero valida los campos antes de persistir.
     */
    suspend fun updateUser(user: UserEntity): Result<Unit> {
        // Validación de campos antes de guardar (reutilizando la lógica de ValidatorUsers)
        validateNameLettersOnly(user.name)?.let { return Result.failure(IllegalArgumentException(it)) }
        validateUsername(user.username)?.let { return Result.failure(IllegalArgumentException(it)) }
        validatePhoneDigitsOnly(user.phone)?.let { return Result.failure(IllegalArgumentException(it)) }
        validateChileanRUN(user.run, user.dv)?.let { return Result.failure(IllegalArgumentException(it)) }

        return withContext(Dispatchers.IO) {
            try {
                userDao.updateUser(user)
                Result.success(Unit) // 'Unit' indica que la operación fue exitosa pero no devuelve un valor.
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Funciones de ayuda para gestionar el estado de login
    fun setLoggedInUserId(userId: Long) {
        loggedInUserId = userId
    }

    fun clearLoggedInUser() {
        loggedInUserId = null
    }

    // El resto de las funciones (register, getFotoUrlById) permanecen mayormente igual

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
        // Validación de correo existente
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
