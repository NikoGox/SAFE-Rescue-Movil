package com.movil.saferescue.data.local.mensaje

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("tipo_mensaje")
data class TipoMensajeEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var tipo: String,
    var detalle:String
)
