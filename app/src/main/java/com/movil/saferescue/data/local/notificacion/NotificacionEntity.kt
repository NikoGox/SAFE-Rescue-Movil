package com.movil.saferescue.data.local.notificacion

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.movil.saferescue.data.local.estado.EstadoEntity
import com.movil.saferescue.data.local.user.UserEntity

@Entity(
    tableName = "notificacion",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_usuario_receptor"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EstadoEntity::class,
            parentColumns = ["id_estado"],
            childColumns = ["id_estado"],
            onDelete = ForeignKey.NO_ACTION // Evitar cascada para historial
        )
    ]
)
data class NotificacionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_notificacion")
    val id: Long = 0,

    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: Long,

    @ColumnInfo(name = "titulo")
    val titulo: String,

    @ColumnInfo(name = "detalle")
    val detalle: String,

    @ColumnInfo(name = "id_usuario_receptor", index = true)
    val usuarioReceptorId: Long,

    @ColumnInfo(name = "id_estado", index = true)
    var estadoId: Long // 1=Creada, 2=Leída, 3=Eliminada
)
