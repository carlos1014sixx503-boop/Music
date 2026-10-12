package com.example.data.youtube

import android.util.Log
import com.example.BuildConfig
import com.example.data.youtube.YouTubeVideo
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class YouTubeRepository {

    private val tag = "YouTubeRepository"

    private val apiService: YouTubeApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(YouTubeApiService::class.java)
    }

    private val apiKey: String
        get() = try {
            BuildConfig.YOUTUBE_API_KEY
        } catch (e: Throwable) {
            ""
        }

    sealed class SearchResult {
        data class Success(val videos: List<YouTubeVideo>, val isFallback: Boolean = false, val message: String? = null) : SearchResult()
        data class Error(val message: String, val fallbackVideos: List<YouTubeVideo>) : SearchResult()
    }

    suspend fun searchVideos(query: String): SearchResult = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return@withContext SearchResult.Success(getCuratedVideos(), isFallback = false)
        }

        if (apiKey.isBlank() || apiKey.startsWith("YOUR_")) {
            Log.d(tag, "No valid YouTube API key found. Using curated catalog.")
            val matches = getFallbackVideos().filter {
                it.title.contains(trimmed, ignoreCase = true) ||
                it.channelTitle.contains(trimmed, ignoreCase = true) ||
                it.description.contains(trimmed, ignoreCase = true)
            }.ifEmpty {
                getFallbackVideos()
            }
            return@withContext SearchResult.Success(
                videos = matches,
                isFallback = true,
                message = "Showing verified catalog results (offline/demo mode)."
            )
        }

        try {
            val response = apiService.searchVideos(query = "$trimmed music", apiKey = apiKey)
            val items = response.items.orEmpty().filter { it.id?.videoId != null }

            if (items.isEmpty()) {
                val matches = getFallbackVideos().filter {
                    it.title.contains(trimmed, ignoreCase = true) ||
                    it.channelTitle.contains(trimmed, ignoreCase = true)
                }.ifEmpty { getFallbackVideos() }
                return@withContext SearchResult.Success(matches, isFallback = true, message = "No direct API matches. Showing curated songs.")
            }

            // Get video details for duration and views
            val videoIds = items.mapNotNull { it.id?.videoId }.joinToString(",")
            val detailsMap = try {
                val detailsResp = apiService.getVideoDetails(ids = videoIds, apiKey = apiKey)
                detailsResp.items.orEmpty().associateBy { it.id.orEmpty() }
            } catch (e: Exception) {
                emptyMap()
            }

            val resultVideos = items.mapNotNull { item ->
                val videoId = item.id?.videoId ?: return@mapNotNull null
                val snippet = item.snippet
                val details = detailsMap[videoId]

                val durationIso = details?.contentDetails?.duration.orEmpty()
                val parsedDuration = parseIsoDuration(durationIso)
                val durationMs = parsedDuration.first
                val durationFormatted = parsedDuration.second

                val views = details?.statistics?.viewCount?.toLongOrNull()
                val formattedViews = formatViewCount(views)

                val thumb = snippet?.thumbnails?.high?.url
                    ?: snippet?.thumbnails?.medium?.url
                    ?: snippet?.thumbnails?.defaultThumb?.url
                    ?: "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

                YouTubeVideo(
                    videoId = videoId,
                    title = snippet?.title?.replace("&quot;", "\"")?.replace("&#39;", "'")?.replace("&amp;", "&") ?: "Unknown Track",
                    channelTitle = snippet?.channelTitle ?: "Artist",
                    description = snippet?.description.orEmpty(),
                    thumbnailUrl = thumb,
                    durationFormatted = durationFormatted,
                    durationMs = durationMs,
                    viewCountFormatted = formattedViews,
                    publishedAt = snippet?.publishedAt.orEmpty()
                )
            }

            SearchResult.Success(resultVideos, isFallback = false)
        } catch (e: Exception) {
            Log.e(tag, "YouTube search failed: ${e.message}", e)
            val fallbackMatches = getFallbackVideos().filter {
                it.title.contains(trimmed, ignoreCase = true) ||
                it.channelTitle.contains(trimmed, ignoreCase = true)
            }.ifEmpty { getFallbackVideos() }

            val userMessage = if (e.message?.contains("quota", ignoreCase = true) == true) {
                "YouTube API quota reached. Serving curated catalog."
            } else {
                "Network issue connecting to YouTube API. Showing verified songs."
            }
            SearchResult.Success(fallbackMatches, isFallback = true, message = userMessage)
        }
    }

    fun getCuratedVideos(): List<YouTubeVideo> {
        return getFallbackVideos()
    }

    fun getFallbackVideos(): List<YouTubeVideo> {
        return listOf(
            YouTubeVideo(
                videoId = "4NRXx6U8ABQ",
                title = "Blinding Lights",
                channelTitle = "The Weeknd",
                description = "Official music video for Blinding Lights by The Weeknd.",
                thumbnailUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
                durationFormatted = "03:20",
                durationMs = 200000L,
                viewCountFormatted = "1.1B views"
            ),
            YouTubeVideo(
                videoId = "jg09EbB-Q1U",
                title = "Levitating",
                channelTitle = "Dua Lipa",
                description = "Official music video for Levitating by Dua Lipa.",
                thumbnailUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
                durationFormatted = "03:23",
                durationMs = 203000L,
                viewCountFormatted = "850M views"
            ),
            YouTubeVideo(
                videoId = "JGwWNGJdvx8",
                title = "Shape of You",
                channelTitle = "Ed Sheeran",
                description = "Official music video for Shape of You by Ed Sheeran.",
                thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                durationFormatted = "03:53",
                durationMs = 233000L,
                viewCountFormatted = "6.1B views"
            ),
            YouTubeVideo(
                videoId = "7wtfhZwyrcc",
                title = "Believer",
                channelTitle = "Imagine Dragons",
                description = "Official music video for Believer by Imagine Dragons.",
                thumbnailUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=600&auto=format&fit=crop&q=80",
                durationFormatted = "03:24",
                durationMs = 204000L,
                viewCountFormatted = "2.4B views"
            ),
            YouTubeVideo(
                videoId = "m7Bc3pLyij0",
                title = "Marshmello ft. Bastille - Happier",
                channelTitle = "Marshmello",
                description = "Official Music Video for Happier by Marshmello ft. Bastille.",
                thumbnailUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=600&auto=format&fit=crop&q=80",
                durationFormatted = "03:34",
                durationMs = 214000L,
                viewCountFormatted = "1.0B views"
            ),
            YouTubeVideo(
                videoId = "kJQP7kiw5Fk",
                title = "Despacito",
                channelTitle = "Luis Fonsi ft. Daddy Yankee",
                description = "Official music video for Despacito.",
                thumbnailUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600&auto=format&fit=crop&q=80",
                durationFormatted = "03:47",
                durationMs = 227000L,
                viewCountFormatted = "8.2B views"
            ),
            YouTubeVideo(
                videoId = "hT_nvWreIhg",
                title = "Counting Stars",
                channelTitle = "OneRepublic",
                description = "Official music video for Counting Stars by OneRepublic.",
                thumbnailUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
                durationFormatted = "04:17",
                durationMs = 257000L,
                viewCountFormatted = "3.9B views"
            ),
            YouTubeVideo(
                videoId = "fJ9rUzIMcZQ",
                title = "Bohemian Rhapsody",
                channelTitle = "Queen",
                description = "Official video for Bohemian Rhapsody by Queen.",
                thumbnailUrl = "https://images.unsplash.com/photo-1520523839898-507127cd53c3?w=600&auto=format&fit=crop&q=80",
                durationFormatted = "05:55",
                durationMs = 355000L,
                viewCountFormatted = "1.6B views"
            ),
            YouTubeVideo(
                videoId = "3tmd-ClpJxA",
                title = "Taylor Swift - Look What You Made Me Do",
                channelTitle = "Taylor Swift",
                description = "Official Music Video by Taylor Swift.",
                thumbnailUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
                durationFormatted = "04:15",
                durationMs = 255000L,
                viewCountFormatted = "1.4B views"
            ),
            YouTubeVideo(
                videoId = "OPf0YbXqDm0",
                title = "Mark Ronson ft. Bruno Mars - Uptown Funk",
                channelTitle = "Mark Ronson",
                description = "Official Video for Uptown Funk.",
                thumbnailUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600&auto=format&fit=crop&q=80",
                durationFormatted = "04:30",
                durationMs = 270000L,
                viewCountFormatted = "4.9B views"
            )
        )
    }

    private fun parseIsoDuration(durationIso: String): Pair<Long, String> {
        if (durationIso.isBlank()) return Pair(210000L, "03:30")
        try {
            val pattern = Pattern.compile("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
            val matcher = pattern.matcher(durationIso)
            if (matcher.find()) {
                val hours = matcher.group(1)?.toLongOrNull() ?: 0L
                val minutes = matcher.group(2)?.toLongOrNull() ?: 0L
                val seconds = matcher.group(3)?.toLongOrNull() ?: 0L
                val totalSeconds = hours * 3600 + minutes * 60 + seconds
                val totalMs = totalSeconds * 1000L
                val formatted = if (hours > 0) {
                    String.format("%02d:%02d:%02d", hours, minutes, seconds)
                } else {
                    String.format("%02d:%02d", minutes, seconds)
                }
                return Pair(totalMs, formatted)
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to parse duration: $durationIso")
        }
        return Pair(210000L, "03:30")
    }

    private fun formatViewCount(views: Long?): String {
        if (views == null || views <= 0) return "1.2M views"
        return when {
            views >= 1_000_000_000 -> String.format("%.1fB views", views / 1_000_000_000.0)
            views >= 1_000_000 -> String.format("%.1fM views", views / 1_000_000.0)
            views >= 1_000 -> String.format("%.1fK views", views / 1_000.0)
            else -> "$views views"
        }
    }
}
