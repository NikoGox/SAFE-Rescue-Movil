package com.movil.saferescue.data.local.mensaje

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.movil.saferescue.data.local.conversacion.ConversacionEntity
import com.movil.saferescue.data.local.user.UserEntity

/**
 * Representa un mensaje en una conversación.
 */
@Entity(
    tableName = "mensaje",
    foreignKeys = [
        ForeignKey(
            entity = ConversacionEntity::class,
            parentColumns = ["id_conversacion"],
            childColumns = ["id_conversacion"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_usuario_emisor"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MensajeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_mensaje")
    val id: Long = 0,

    @ColumnInfo(name = "detalle")
    val mensaje: String, // Mapped to 'detalle' in DB, keeping 'mensaje' property name for code compatibility

    @ColumnInfo(name = "fecha_creacion")
    val fechaSubida: Long, // Mapped to 'fecha_creacion', property name 'fechaSubida'

    @ColumnInfo(name = "id_conversacion", index = true)
    val conversacionId: Long,

    @ColumnInfo(name = "id_usuario_emisor", index = true)
    val remitenteId: Long,

    @ColumnInfo(name = "id_estado")
    var isRead: Boolean = false // Using boolean for simplicity as 'id_estado' for now (0/1)
)
