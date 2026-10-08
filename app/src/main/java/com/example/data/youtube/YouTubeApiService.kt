package com.example.data.youtube

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class YouTubeSearchResponse(
    @field:Json(name = "items") val items: List<YouTubeSearchItemDto>? = null,
    @field:Json(name = "nextPageToken") val nextPageToken: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeSearchItemDto(
    @field:Json(name = "id") val id: YouTubeIdDto? = null,
    @field:Json(name = "snippet") val snippet: YouTubeSnippetDto? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeIdDto(
    @field:Json(name = "kind") val kind: String? = null,
    @field:Json(name = "videoId") val videoId: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeSnippetDto(
    @field:Json(name = "publishedAt") val publishedAt: String? = null,
    @field:Json(name = "channelTitle") val channelTitle: String? = null,
    @field:Json(name = "title") val title: String? = null,
    @field:Json(name = "description") val description: String? = null,
    @field:Json(name = "thumbnails") val thumbnails: YouTubeThumbnailsDto? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeThumbnailsDto(
    @field:Json(name = "default") val defaultThumb: YouTubeThumbnailDto? = null,
    @field:Json(name = "medium") val mediumThumb: YouTubeThumbnailDto? = null,
    @field:Json(name = "high") val highThumb: YouTubeThumbnailDto? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeThumbnailDto(
    @field:Json(name = "url") val url: String? = null,
    @field:Json(name = "width") val width: Int? = null,
    @field:Json(name = "height") val height: Int? = null
)

// DTOs para detalles de video (contentDetails -> duration, statistics -> viewCount)
@JsonClass(generateAdapter = true)
data class YouTubeVideoDetailsResponse(
    @field:Json(name = "items") val items: List<YouTubeVideoDetailItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoDetailItemDto(
    @field:Json(name = "id") val id: String? = null,
    @field:Json(name = "contentDetails") val contentDetails: YouTubeContentDetailsDto? = null,
    @field:Json(name = "statistics") val statistics: YouTubeStatisticsDto? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeContentDetailsDto(
    @field:Json(name = "duration") val duration: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeStatisticsDto(
    @field:Json(name = "viewCount") val viewCount: String? = null,
    @field:Json(name = "likeCount") val likeCount: String? = null
)

/**
 * Interfaz Retrofit para YouTube Data API v3 oficial especializada en búsqueda de pistas musicales.
 */
interface YouTubeApiService {

    /**
     * Búsqueda de pistas y videos musicales en YouTube.
     * @param query Término de búsqueda (canción, artista, álbum o género).
     * @param videoCategoryId ID 10 para categoría 'Música' en YouTube Data API.
     * @param videoEmbeddable "true" para garantizar que las canciones puedan reproducirse en la app.
     */
    @GET("youtube/v3/search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("videoCategoryId") videoCategoryId: String? = "10",
        @Query("videoEmbeddable") videoEmbeddable: String? = "true",
        @Query("maxResults") maxResults: Int = 25,
        @Query("pageToken") pageToken: String? = null,
        @Query("key") apiKey: String
    ): Response<YouTubeSearchResponse>

    @GET("youtube/v3/videos")
    suspend fun getVideoDetails(
        @Query("part") part: String = "contentDetails,statistics",
        @Query("id") ids: String,
        @Query("key") apiKey: String
    ): Response<YouTubeVideoDetailsResponse>
}
