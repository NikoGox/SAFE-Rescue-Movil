package com.movil.saferescue.data.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.movil.saferescue.data.local.foto.FotoEntity

@Dao
interface UserDao {
    //insertar un nuevo user en la tabla
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUsuario(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTipoPerfil(tipoPerfil: TipoPerfilEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFoto(foto: FotoEntity): Long

    //obtener los datos de un usuario mediante su email
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    //obtener los datos de un usuario mediante su id
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): UserEntity?

    //obtener todos los usuarios de la tabla ordenados por id ascendente
    @Query("SELECT * FROM users ORDER BY id ASC")
    suspend fun getAll(): List<UserEntity>

    //obtener la cantidad de registros en la tabla
    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    //obterner el usuario con el rol
    @Query("SELECT u.name, u.email, t.rol, f.url FROM users u JOIN tipo_perfil t ON u.rol_id = t.id JOIN fotos f ON u.foto_id = f.id WHERE u.id = :id LIMIT 1")
    suspend fun getPerfil(id: Long): PerfilUsuario?

}