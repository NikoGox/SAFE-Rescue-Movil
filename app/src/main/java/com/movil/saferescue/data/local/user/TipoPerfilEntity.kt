package com.movil.saferescue.data.local.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("tipo_perfil")
data class TipoPerfilEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var rol: String,
    var detalle:String
)



