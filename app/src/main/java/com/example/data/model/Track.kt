package com.example.data.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val albumArtUrl: String,
    val isOnline: Boolean = false,
    val youtubeVideoId: String? = null,
    val audioUrl: String? = null,
    val genre: String = "Pop",
    val isFavorite: Boolean = false
) {
    val durationFormatted: String
        get() {
            val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}
