package com.example.data.youtube

/**
 * Modelo de datos para un elemento de video de YouTube.
 */
data class YouTubeVideo(
    val id: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val description: String = "",
    val durationFormatted: String? = null,
    val publishedAt: String = "",
    val viewCountFormatted: String? = null
) {
    val watchUrl: String
        get() = "https://www.youtube.com/watch?v=$id"

    val embedUrl: String
        get() = "https://www.youtube-nocookie.com/embed/$id?autoplay=1&playsinline=1&rel=0&modestbranding=1"
}

enum class YouTubePlaybackMode {
    VIDEO,
    AUDIO_ONLY
}

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
