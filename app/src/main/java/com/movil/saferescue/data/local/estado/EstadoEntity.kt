package com.movil.saferescue.data.local.estado

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "estado")
data class EstadoEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id_estado")
    val id: Long,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "descripcion")
    val descripcion: String? // Ahora permite nulos
)
