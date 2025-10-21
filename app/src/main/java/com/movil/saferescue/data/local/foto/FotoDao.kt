package com.movil.saferescue.data.local.foto

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FotoDao {
    // Busca una foto por su URL. Devuelve la entidad completa para obtener su ID.
    @Query("SELECT * FROM fotos WHERE url = :url LIMIT 1")
    suspend fun getByUrl(url: String): FotoEntity?

    @Insert
    suspend fun insert(foto: FotoEntity): Long

    @Query("SELECT * FROM fotos WHERE id = :fotoId LIMIT 1")
    suspend fun findFotoById(fotoId: Long): FotoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fotos: List<FotoEntity>)

}
