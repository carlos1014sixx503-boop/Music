package com.example.data.youtube

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {

    @GET("youtube/v3/search")
    suspend fun searchVideos(
        @Query("q") query: String,
        @Query("key") apiKey: String,
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 20
    ): YouTubeSearchResponse

    @GET("youtube/v3/videos")
    suspend fun getVideoDetails(
        @Query("id") ids: String,
        @Query("key") apiKey: String,
        @Query("part") part: String = "contentDetails,statistics"
    ): YouTubeVideoDetailsResponse
}

data class YouTubeSearchResponse(
    val items: List<YouTubeSearchItem>? = null
)

data class YouTubeSearchItem(
    val id: YouTubeItemId? = null,
    val snippet: YouTubeSnippet? = null
)

data class YouTubeItemId(
    val kind: String? = null,
    val videoId: String? = null
)

data class YouTubeSnippet(
    val title: String? = null,
    val channelTitle: String? = null,
    val description: String? = null,
    val publishedAt: String? = null,
    val thumbnails: YouTubeThumbnails? = null
)

data class YouTubeThumbnails(
    @Json(name = "default") val defaultThumb: YouTubeThumbnail? = null,
    val medium: YouTubeThumbnail? = null,
    val high: YouTubeThumbnail? = null
)

data class YouTubeThumbnail(
    val url: String? = null
)

data class YouTubeVideoDetailsResponse(
    val items: List<YouTubeVideoDetailsItem>? = null
)

data class YouTubeVideoDetailsItem(
    val id: String? = null,
    val contentDetails: YouTubeContentDetails? = null,
    val statistics: YouTubeStatistics? = null
)

data class YouTubeContentDetails(
    val duration: String? = null
)

data class YouTubeStatistics(
    val viewCount: String? = null
)
