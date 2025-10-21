package com.movil.saferescue.data.local.incidente

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.movil.saferescue.data.local.foto.FotoEntity

@Entity(
    tableName = "incidente",
    foreignKeys = [
        ForeignKey(
            entity = FotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["foto_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["foto_id"])]
)
data class IncidenteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val titulo: String,

    val fechaRegistro: Long = System.currentTimeMillis(),

    val detalle: String,

    @ColumnInfo(name = "foto_id")
    val foto_id: Long? = null
)
