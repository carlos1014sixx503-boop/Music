package com.example.data

import androidx.annotation.DrawableRes

enum class RepeatMode {
    OFF, ONE, ALL
}

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    @DrawableRes val coverResId: Int? = null,
    val albumArtUri: String? = null,
    val genre: String = "Desconocido",
    val audioFormat: String = "Audio",
    val isFavorite: Boolean = false,
    val folderName: String = "Music",
    val folderPath: String = "",
    val fileName: String = "",
    val filePath: String = "",
    val contentUri: String = "",
    val bitrateKbps: Int? = null,
    val sampleRateHz: Int? = null,
    val sizeBytes: Long = 0L,
    val playCount: Int = 0,
    val year: Int = 2024,
    val dateAdded: Long = 0L
) {
    val durationFormatted: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

fun Song.toTrack(): com.example.data.model.Track {
    return com.example.data.model.Track(
        id = this.id,
        title = this.title,
        artist = this.artist,
        durationMs = (this.durationSeconds * 1000L).coerceAtLeast(1000L),
        albumArtUrl = this.albumArtUri ?: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
        isOnline = false,
        audioUrl = if (this.contentUri.isNotBlank()) this.contentUri else this.filePath,
        genre = this.genre,
        isFavorite = this.isFavorite,
        folderName = if (this.folderName.isNotBlank()) this.folderName else "Música",
        folderPath = this.folderPath
    )
}

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val year: Int,
    @DrawableRes val coverResId: Int? = null,
    val albumArtUri: String? = null,
    val songsCount: Int
)

data class Artist(
    val id: String,
    val name: String,
    val songsCount: Int,
    val albumsCount: Int
)

data class MusicFolder(
    val id: String,
    val name: String,
    val path: String,
    val songCount: Int
)

enum class LibraryTab(val title: String) {
    PLAYLISTS("Listas"),
    SONGS("Canciones"),
    ALBUMS("Álbumes"),
    ARTISTS("Artistas"),
    FOLDERS("Carpetas"),
    FAVORITES("Favoritos"),
    RECENT("Recientes")
}
