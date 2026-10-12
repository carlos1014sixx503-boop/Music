package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val songId: String,
    val addedAt: Long = System.currentTimeMillis(),
    val title: String = "",
    val artist: String = "",
    val albumArtUri: String? = null
)
