package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room para persistir las canciones favoritas marcadas por el usuario.
 * Almacena el ID único de la canción y la marca de tiempo en la que se añadió.
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val songId: String,
    val addedAt: Long = System.currentTimeMillis()
)
