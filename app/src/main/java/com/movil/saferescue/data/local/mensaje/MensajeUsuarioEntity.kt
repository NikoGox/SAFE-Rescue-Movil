package com.movil.saferescue.data.local.mensaje

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa una notificación asignada a un usuario específico.
 * Se crea una fila por cada usuario para las notificaciones globales.
 */
@Entity(tableName = "mensajes_usuario")
data class MensajeUsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "user_id", index = true)
    val userId: Long,

    @ColumnInfo(name = "plantilla_id", index = true)
    val plantillaId: Long?,

    @ColumnInfo(name = "titulo")
    val titulo: String,

    @ColumnInfo(name = "mensaje")
    val mensaje: String,

    @ColumnInfo(name = "fechaSubida")
    val fechaSubida: Long,

    @ColumnInfo(name = "isRead")
    var isRead: Boolean = false,

    @ColumnInfo(name = "deleted")
    var deleted: Boolean = false
)

