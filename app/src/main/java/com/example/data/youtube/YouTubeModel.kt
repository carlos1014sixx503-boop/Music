package com.example.data.youtube

import com.example.data.model.Track

/**
 * Modelo de datos para videos y canciones de YouTube.
 */
data class YouTubeVideo(
    val videoId: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val description: String = "",
    val durationFormatted: String? = null,
    val durationMs: Long = 0L,
    val publishedAt: String = "",
    val viewCountFormatted: String? = null
) {
    val id: String get() = videoId

    val watchUrl: String
        get() = "https://www.youtube.com/watch?v=$videoId"

    val embedUrl: String
        get() = "https://www.youtube-nocookie.com/embed/$videoId?autoplay=1&playsinline=1&rel=0&modestbranding=1"

    fun toTrack(): Track {
        return Track(
            id = "yt_$videoId",
            title = title,
            artist = channelTitle,
            durationMs = if (durationMs > 0) durationMs else 210000L,
            albumArtUrl = thumbnailUrl,
            isOnline = true,
            youtubeVideoId = videoId
        )
    }
}

enum class YouTubePlaybackMode {
    VIDEO,
    AUDIO_ONLY
}

data class YouTubePlaylist(
    val id: String,
    val title: String,
    val author: String,
    val videoCount: String,
    val thumbnailUrl: String,
    val tracks: List<Track> = emptyList()
)

data class YouTubeAccountProfile(
    val channelTitle: String = "Distrito Music 503",
    val channelHandle: String = "@distritomusic503",
    val avatarUrl: String = "",
    val subscriberCount: String = "15.8K suscriptores",
    val isConnected: Boolean = true,
    val lastSyncTime: String = "Sincronizado ahora"
)

sealed class YouTubeSearchState {
    object Idle : YouTubeSearchState()
    object Loading : YouTubeSearchState()
    data class Success(
        val videos: List<YouTubeVideo>,
        val nextPageToken: String? = null,
        val isDemoFallback: Boolean = false
    ) : YouTubeSearchState()
    data class Empty(val query: String) : YouTubeSearchState()
    data class Error(val message: String, val canRetry: Boolean = true) : YouTubeSearchState()
    object NoInternet : YouTubeSearchState()
    object QuotaExceeded : YouTubeSearchState()
}
