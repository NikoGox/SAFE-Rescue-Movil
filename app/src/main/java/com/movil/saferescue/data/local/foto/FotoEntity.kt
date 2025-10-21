package com.movil.saferescue.data.local.foto

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fotos",
    indices = [Index(value = ["url"], unique = true)]
)
data class FotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val fechaSubida: Long,
    val url: String // La URL de la imagen
)