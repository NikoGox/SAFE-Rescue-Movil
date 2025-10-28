package com.movil.saferescue.data.local.incidente

import androidx.room.*
import com.movil.saferescue.data.local.foto.FotoEntity
import com.movil.saferescue.data.local.user.UserEntity

enum class IncidenteEstado {
    ACTIVO,
    ASIGNADO,
    RESUELTO
}

@Entity(
    tableName = "incidente",
    foreignKeys = [
        ForeignKey(
            entity = FotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["foto_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["asignado_a_user_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["foto_id"]), Index(value = ["asignado_a_user_id"])]
)
data class IncidenteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val titulo: String,

    val fechaRegistro: Long = System.currentTimeMillis(),

    val detalle: String,

    @ColumnInfo(name = "foto_id")
    val foto_id: Long?,

    val estado: String = IncidenteEstado.ACTIVO.name,

    @ColumnInfo(name = "asignado_a_user_id")
    val asignadoA: Long? = null, // ID del usuario que tomó el incidente.

    val latitud: Double?,
    val longitud: Double?,
    val comuna: String?,
    val region: String?,
    val direccion: String?
)
