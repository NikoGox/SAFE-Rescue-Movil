package com.movil.saferescue.data.local.notification

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("notificaciones")
data class NotificationEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val titulo: String,
    val fechaSubida: Long= System.currentTimeMillis(),
    var mensaje:String,
    val isRead: Boolean = false
)



