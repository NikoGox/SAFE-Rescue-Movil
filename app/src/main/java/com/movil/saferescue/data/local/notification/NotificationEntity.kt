package com.movil.saferescue.data.local.notification

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.movil.saferescue.data.local.user.UserEntity


@Entity(tableName = "notificaciones",
    // 3. Añade la anotación de llave foránea aquí
    foreignKeys = [
        ForeignKey(
            entity = TipoMensajeEntity::class,
            parentColumns = ["id"],
            childColumns = ["tipo_mensaje_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["remitente_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titulo: String,
    val mensaje: String,
    val fechaSubida: Long,
    var isRead: Boolean = false,


  /**
   * Define el tipo de notificación. Por ejemplo: "alerta" o "mensaje".
   * Esto nos permitirá filtrar qué mostrar en cada pantalla.
   */ @ColumnInfo(name = "tipo_mensaje_id", index = true)
  val tipo_mensaje: Long,

  /**
   * Define quién envía el mensaje. Por ejemplo: "Sistema", "Tú", "Soporte".
   * Esencial para la vista de chat.
   */@ColumnInfo(name = "remitente_id", index = true)
  val remitente_id: Long
)
