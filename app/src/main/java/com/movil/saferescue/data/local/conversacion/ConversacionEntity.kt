package com.movil.saferescue.data.local.conversacion

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversacion")
data class ConversacionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_conversacion")
    val id: Long = 0,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "tipo")
    val tipo: String, // Ej: "INDIVIDUAL", "GRUPAL"

    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: Long
)
