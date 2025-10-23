package com.movil.saferescue.data.local.mensaje

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.movil.saferescue.data.local.user.UserEntity

/**
 * Representa un único mensaje en la base de datos, que puede ser una alerta o parte de un chat.
 *
 * @param id El identificador único del mensaje.
 * @param titulo Un título corto para el mensaje, útil para alertas.
 * @param mensaje El contenido principal del mensaje.
 * @param fechaSubida La marca de tiempo (timestamp) de cuándo se creó o recibió el mensaje.
 * @param isRead Indica si el usuario ha leído el mensaje.
 * @param tipo_mensaje_id La llave foránea que lo vincula con `TipoMensajeEntity` (ej. 1 para "Alerta", 2 para "Chat").
 * @param remitente_id La llave foránea que lo vincula con `UserEntity`, indicando quién envió el mensaje.
 */
@Entity(
    tableName = "mensajes", // Renombramos la tabla de "notificaciones" a "mensajes"
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
data class MensajeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "titulo")
    val titulo: String,

    @ColumnInfo(name = "mensaje")
    val mensaje: String,

    @ColumnInfo(name = "fechaSubida")
    val fechaSubida: Long,

    @ColumnInfo(name = "isRead")
    var isRead: Boolean = false,

    /**
     * Define el tipo de mensaje. Se vincula con la tabla `tipos_mensaje`.
     * Por ejemplo: "alerta" o "mensaje de chat".
     */
    @ColumnInfo(name = "tipo_mensaje_id", index = true)
    val tipo_mensaje_id: Long, // Renombrado de 'tipo_mensaje' para ser consistente

    /**
     * Define quién envía el mensaje. Se vincula con la tabla `users`.
     * Por ejemplo: "Sistema", "Tú", "Soporte".
     */
    @ColumnInfo(name = "remitente_id", index = true)
    val remitente_id: Long
)
