package com.movil.saferescue.data.repository

import com.movil.saferescue.data.local.foto.FotoDao
import com.movil.saferescue.data.local.foto.FotoEntity
import com.movil.saferescue.data.local.user.UserDao
import com.movil.saferescue.data.local.user.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//declarar reglas de negocio para el login/register
class UserRepository(
    //inyección de los DAOs necesarios
    private val userDao: UserDao,
    private val fotoDao: FotoDao // 1. Inyectamos el DAO de fotos
) {
    //orqueste el login
    suspend fun login(email: String, password: String): Result<UserEntity> {
        val user = userDao.getByEmail(email)
        return if (user != null && user.password == password) {
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Credenciales Inválidas"))
        }
    }

    /**
     * Obtiene los datos del usuario actualmente logueado.
     * ATENCIÓN: Esta es una implementación SIMULADA. En una app real,
     * deberías guardar el ID del usuario logueado en SharedPreferences
     * y usar ese ID para buscar al usuario específico.
     * Por ahora, simplemente devolvemos el primer usuario de la tabla.
     */
    suspend fun getCurrentUser(): UserEntity? {
        return withContext(Dispatchers.IO) {
            userDao.getAll().firstOrNull() // Devuelve el primer usuario que encuentre
        }
    }

    suspend fun getFotoUrlById(fotoId: Long?): String? {
        if (fotoId == null) return null // Si no hay ID, no hay foto
        return withContext(Dispatchers.IO) {
            fotoDao.findFotoById(fotoId)?.url // Busca en FotoDao y devuelve solo la URL
        }
    }

    /**
     * Orquesta el registro de un nuevo usuario.
     * Transforma la URL de la foto en un ID antes de guardarla.
     */
    suspend fun register(
        name: String,
        email: String,
        phone: String,
        pass: String,
        run: String,
        dv: String,
        username: String,
        fotoUrl: String, // 2. Recibimos la URL en lugar del ID
        rol_id: Long
    ): Result<Long> {
        // Validación de correo existente
        if (userDao.getByEmail(email) != null) {
            return Result.failure(IllegalArgumentException("Correo ya registrado"))
        }

        // 3. Lógica para obtener o crear el ID de la foto a partir de la URL
        val fotoExistente = fotoDao.getByUrl(fotoUrl)
        val fotoId: Long

        if (fotoExistente != null) {
            // Si la foto ya existe en la base de datos, usamos su ID
            fotoId = fotoExistente.id
        } else {
            // Si no existe, la insertamos y obtenemos el nuevo ID generado
            fotoId = fotoDao.insert(FotoEntity(url = fotoUrl, fechaSubida = System.currentTimeMillis()))
        }

        // 4. Creamos y guardamos el usuario con el foto_id correcto
        val nuevoUsuarioId = userDao.insertUsuario(
            UserEntity(
                name = name,
                email = email,
                phone = phone,
                password = pass,
                run = run,
                dv = dv,
                username = username,
                foto_id = fotoId, // Usamos el ID que obtuvimos
                rol_id = rol_id
            )
        )

        return Result.success(nuevoUsuarioId)
    }
}
