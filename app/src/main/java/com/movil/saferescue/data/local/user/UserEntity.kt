package com.movil.saferescue.data.local.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.movil.saferescue.data.local.foto.FotoEntity

@Entity(
    tableName = "users",
    // 3. Añade la anotación de llave foránea aquí
    foreignKeys = [
        ForeignKey(
            entity = TipoPerfilEntity::class,      // La tabla padre (la que tiene la clave principal)
            parentColumns = ["id"],         // El nombre de la columna en la tabla padre (de RolEntity)
            childColumns = ["rol_id"],      // El nombre de la columna en esta tabla (UserEntity)
            onDelete = ForeignKey.CASCADE   // Acción a tomar si se borra un rol (opcional pero recomendado)
        ),
        ForeignKey(
            entity = FotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["foto_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val run: String,
    val dv: String,
    val username: String,
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    // 4. Asegúrate de que el índice esté creado para mejorar el rendimiento
    @ColumnInfo(name = "foto_id", index = true)
    var foto_id: Long?,

    @ColumnInfo(name = "rol_id", index = true)
    var rol_id: Long
)

data class PerfilUsuario(
    val name: String,
    val email: String,
    val rol: String,
    val url: String
)

