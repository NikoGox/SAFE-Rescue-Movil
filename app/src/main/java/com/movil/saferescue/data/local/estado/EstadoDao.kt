package com.movil.saferescue.data.local.estado

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EstadoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(estados: List<EstadoEntity>)

    @Query("SELECT * FROM estado WHERE id_estado = :id")
    suspend fun getEstadoById(id: Long): EstadoEntity?
}
