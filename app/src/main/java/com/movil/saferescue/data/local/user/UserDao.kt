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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Query("SELECT * FROM tipo_perfil WHERE id = :rolId")
    suspend fun getRolById(rolId: Long): TipoPerfilEntity?

    @Query("""
        SELECT
            u.id,
            u.name,
            u.username,
            u.email,
            u.phone,
            u.run,
            u.dv,
            f.url AS fotoUrl,      
            r.rol AS rolName,   
            u.foto_id AS fotoId,   
            u.rol_id AS rolId      
        FROM users AS u
        LEFT JOIN tipo_perfil AS r ON u.rol_id = r.id
        LEFT JOIN fotos AS f ON u.foto_id = f.id 
        WHERE u.id = :userId
    """)
    suspend fun getUserProfileById(userId: Long): UserProfile?
}


