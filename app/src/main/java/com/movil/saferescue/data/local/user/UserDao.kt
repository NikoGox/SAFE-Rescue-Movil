package com.movil.saferescue.data.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    /**
     * Inserta un nuevo usuario en la tabla. Si el email ya existe, aborta.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUsuario(user: UserEntity): Long

    /**
     * Obtiene un usuario por su email.
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    /**
     * Obtiene un usuario por su nombre de usuario (username).
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): UserEntity? // <-- FUNCIÓN AÑADIDA

    /**
     * Obtiene un usuario por su email o por su nombre de usuario.
     * Ideal para la pantalla de Login.
     */
    @Query("SELECT * FROM users WHERE email = :identifier OR username = :identifier LIMIT 1")
    suspend fun getByEmailOrUsername(identifier: String): UserEntity?

    /**
     * Obtiene un usuario por su ID.
     */
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    /**
     * Obtiene todos los usuarios de la tabla, ordenados por ID.
     */
    @Query("SELECT * FROM users ORDER BY id ASC")
    suspend fun getAll(): List<UserEntity>

    /**
     * Cuenta el número total de usuarios en la tabla.
     */
    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    /**
     * Actualiza los datos de un usuario existente.
     */
    @Update
    suspend fun updateUser(user: UserEntity)

    /**
     * Obtiene los datos combinados para mostrar el perfil de un usuario.
     * Esta función es muy útil, ¡buen trabajo al crearla!
     */
    @Query("SELECT u.name, u.email, t.rol, f.url FROM users u JOIN tipo_perfil t ON u.rol_id = t.id JOIN fotos f ON u.foto_id = f.id WHERE u.id = :id LIMIT 1")
    suspend fun getPerfil(id: Long): PerfilUsuario?
}
