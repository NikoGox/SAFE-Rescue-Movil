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
    val nombre: String, // El nombre único del archivo, ej: "IMG_20251022_051208.jpg"
    val url: String     // La URI local persistente de la imagen
)
