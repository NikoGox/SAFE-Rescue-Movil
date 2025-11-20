package com.movil.saferescue.data.local.conversacion

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.movil.saferescue.data.local.user.UserEntity

@Entity(
    tableName = "participante_conv",
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
            childColumns = ["id_usuario"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ParticipanteConvEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_participante_conv")
    val id: Long = 0,

    @ColumnInfo(name = "id_conversacion", index = true)
    val conversacionId: Long,

    @ColumnInfo(name = "id_usuario", index = true)
    val usuarioId: Long,

    @ColumnInfo(name = "fecha_inclusion")
    val fechaInclusion: Long
)
