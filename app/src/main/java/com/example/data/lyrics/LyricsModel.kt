package com.example.data.lyrics

data class LyricLine(
    val timestampMs: Long,
    val text: String,
    val translation: String? = null
) {
    val timestampSeconds: Int
        get() = (timestampMs / 1000L).toInt()
        
    val formattedTime: String
        get() {
            val totalSec = timestampMs / 1000L
            val mins = totalSec / 60
            val secs = totalSec % 60
            return "%02d:%02d".format(mins, secs)
        }
}

data class SongLyrics(
    val songId: String,
    val title: String,
    val artist: String,
    val lines: List<LyricLine>,
    val isSynchronized: Boolean = true,
    val source: String = "Distrito 503 Sync Engine"
)
