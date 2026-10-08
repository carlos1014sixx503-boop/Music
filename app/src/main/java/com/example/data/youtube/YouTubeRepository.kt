package com.example.data.youtube

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.BuildConfig
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

/**
 * Repositorio de búsqueda oficial para YouTube Data API v3.
 * Maneja llamadas de red, parsing de duración ISO 8601, errores de cuota, falta de internet y fallback demostrativo.
 */
class YouTubeRepository(private val context: Context) {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val apiService: YouTubeApiService = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(YouTubeApiService::class.java)

    /**
     * Retorna la API key configurada en BuildConfig.YOUTUBE_API_KEY o null si no está definida.
     */
    fun getApiKey(): String {
        return try {
            val key = BuildConfig.YOUTUBE_API_KEY
            if (key.isNullOrBlank() || key == "YOUR_YOUTUBE_API_KEY" || key == "MY_NEW_API_KEY_DEFAULT_VALUE") "" else key.trim()
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Comprueba conectividad real a internet.
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Realiza la búsqueda de videos usando YouTube Data API v3 oficial.
     */
    suspend fun search(query: String): YouTubeSearchState = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            return@withContext YouTubeSearchState.NoInternet
        }

        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            // Si el usuario aún no ha colocado su API Key en los Secretos, devolver un conjunto
            // de muestra curado relevante a la búsqueda con indicación amigable para configurar la clave.
            val sampleVideos = getFallbackVideos(query)
            return@withContext YouTubeSearchState.Success(sampleVideos, isDemoFallback = true)
        }

        try {
            // Primer intento: Filtrar exclusivamente por categoría Música (videoCategoryId = 10) y que sea integrable
            var response = apiService.searchVideos(
                query = query,
                apiKey = apiKey,
                videoCategoryId = "10",
                videoEmbeddable = "true",
                maxResults = 25
            )

            // Si no devuelve resultados con la categoría 10 (ej. búsqueda muy específica), reintentar sin filtro de categoría
            if (response.isSuccessful && response.body()?.items.orEmpty().none { it.id?.videoId != null }) {
                response = apiService.searchVideos(
                    query = query,
                    apiKey = apiKey,
                    videoCategoryId = null,
                    videoEmbeddable = "true",
                    maxResults = 25
                )
            }

            if (!response.isSuccessful) {
                val code = response.code()
                val errorBody = response.errorBody()?.string() ?: ""
                return@withContext when {
                    code == 403 && (errorBody.contains("quota", ignoreCase = true) || errorBody.contains("quotaExceeded", ignoreCase = true)) -> {
                        YouTubeSearchState.QuotaExceeded
                    }
                    code == 400 || code == 403 -> {
                        YouTubeSearchState.Error("Error de autenticación con la API Key de YouTube (Código $code). Revisa que la YouTube Data API v3 esté habilitada en tu Google Cloud Console.")
                    }
                    else -> {
                        YouTubeSearchState.Error("Error al conectar con YouTube (Código $code): ${response.message()}")
                    }
                }
            }

            val body = response.body()
            val rawItems = body?.items.orEmpty()
            val videoItems = rawItems.filter { it.id?.videoId != null }

            if (videoItems.isEmpty()) {
                return@withContext YouTubeSearchState.Empty(query)
            }

            // Obtener duraciones y estadísticas (vistas) de los videos encontrados
            val videoIds = videoItems.mapNotNull { it.id?.videoId }.joinToString(",")
            val durationMap = mutableMapOf<String, String>()
            val viewsMap = mutableMapOf<String, String>()

            try {
                val detailsResponse = apiService.getVideoDetails(
                    ids = videoIds,
                    apiKey = apiKey
                )
                if (detailsResponse.isSuccessful) {
                    detailsResponse.body()?.items?.forEach { detail ->
                        val id = detail.id
                        val rawDuration = detail.contentDetails?.duration
                        if (id != null && rawDuration != null) {
                            durationMap[id] = formatIsoDuration(rawDuration)
                        }
                        val rawViews = detail.statistics?.viewCount
                        if (id != null && rawViews != null) {
                            val formatted = formatViewCount(rawViews)
                            if (formatted != null) {
                                viewsMap[id] = formatted
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Si falla el detalle de duración, continuamos con los datos del video básicos
            }

            val mappedVideos = videoItems.mapNotNull { item ->
                val vId = item.id?.videoId ?: return@mapNotNull null
                val snippet = item.snippet ?: return@mapNotNull null
                val bestThumb = snippet.thumbnails?.highThumb?.url
                    ?: snippet.thumbnails?.mediumThumb?.url
                    ?: snippet.thumbnails?.defaultThumb?.url
                    ?: "https://img.youtube.com/vi/$vId/hqdefault.jpg"

                // Decodificar entidades HTML comunes en títulos (ej. &quot;, &#39;, &amp;)
                val cleanTitle = cleanHtmlEntities(snippet.title.orEmpty())

                YouTubeVideo(
                    id = vId,
                    title = cleanTitle,
                    channelTitle = snippet.channelTitle.orEmpty(),
                    thumbnailUrl = bestThumb,
                    description = snippet.description.orEmpty(),
                    durationFormatted = durationMap[vId],
                    publishedAt = snippet.publishedAt.orEmpty(),
                    viewCountFormatted = viewsMap[vId]
                )
            }

            YouTubeSearchState.Success(
                videos = mappedVideos,
                nextPageToken = body?.nextPageToken,
                isDemoFallback = false
            )
        } catch (e: Exception) {
            YouTubeSearchState.Error("Error de red al consultar YouTube: ${e.localizedMessage ?: "Error desconocido"}")
        }
    }

    /**
     * Parsea duraciones ISO 8601 de YouTube (ej. PT3M45S, PT1H2M30S) a formato amigable (ej. 3:45, 1:02:30).
     */
    fun formatIsoDuration(iso: String): String {
        return try {
            val pattern = Pattern.compile("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
            val matcher = pattern.matcher(iso)
            if (matcher.matches()) {
                val hours = matcher.group(1)?.toIntOrNull() ?: 0
                val minutes = matcher.group(2)?.toIntOrNull() ?: 0
                val seconds = matcher.group(3)?.toIntOrNull() ?: 0

                if (hours > 0) {
                    String.format("%d:%02d:%02d", hours, minutes, seconds)
                } else {
                    String.format("%d:%02d", minutes, seconds)
                }
            } else {
                iso.removePrefix("PT")
            }
        } catch (_: Exception) {
            ""
        }
    }

    fun formatViewCount(viewsStr: String?): String? {
        val views = viewsStr?.toLongOrNull() ?: return null
        return when {
            views >= 1_000_000_000 -> String.format(java.util.Locale.US, "%.1fB vistas", views / 1_000_000_000.0)
            views >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM vistas", views / 1_000_000.0)
            views >= 1_000 -> String.format(java.util.Locale.US, "%.0fK vistas", views / 1_000.0)
            else -> "$views vistas"
        }
    }

    private fun cleanHtmlEntities(text: String): String {
        return text
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
    }

    /**
     * Colección de videos demostrativos oficiales permitidos cuando el usuario no ha suministrado aún su API Key.
     */
    private fun getFallbackVideos(query: String): List<YouTubeVideo> {
        val curated = listOf(
            YouTubeVideo(
                id = "dQw4w9WgXcQ",
                title = "Rick Astley - Never Gonna Give You Up (Official Music Video)",
                channelTitle = "Rick Astley",
                thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
                durationFormatted = "3:33",
                viewCountFormatted = "1.5B vistas",
                description = "The official video for Never Gonna Give You Up by Rick Astley."
            ),
            YouTubeVideo(
                id = "fJ9rUzIMcZQ",
                title = "Queen – Bohemian Rhapsody (Official Video Remastered)",
                channelTitle = "Queen Official",
                thumbnailUrl = "https://img.youtube.com/vi/fJ9rUzIMcZQ/hqdefault.jpg",
                durationFormatted = "5:59",
                viewCountFormatted = "1.8B vistas",
                description = "Bohemian Rhapsody official remastered music video by Queen."
            ),
            YouTubeVideo(
                id = "kJQP7kiw5Fk",
                title = "Luis Fonsi - Despacito ft. Daddy Yankee",
                channelTitle = "Luis Fonsi",
                thumbnailUrl = "https://img.youtube.com/vi/kJQP7kiw5Fk/hqdefault.jpg",
                durationFormatted = "4:42",
                viewCountFormatted = "8.3B vistas",
                description = "Despacito official video featuring Daddy Yankee."
            ),
            YouTubeVideo(
                id = "OPf0YbXqDm0",
                title = "Mark Ronson - Uptown Funk (Official Video) ft. Bruno Mars",
                channelTitle = "MarkRonsonVEVO",
                thumbnailUrl = "https://img.youtube.com/vi/OPf0YbXqDm0/hqdefault.jpg",
                durationFormatted = "4:31",
                viewCountFormatted = "5.1B vistas",
                description = "Official video for Uptown Funk by Mark Ronson ft. Bruno Mars."
            ),
            YouTubeVideo(
                id = "2Vv-BfVoq4g",
                title = "Ed Sheeran - Perfect (Official Music Video)",
                channelTitle = "Ed Sheeran",
                thumbnailUrl = "https://img.youtube.com/vi/2Vv-BfVoq4g/hqdefault.jpg",
                durationFormatted = "4:40",
                viewCountFormatted = "3.6B vistas",
                description = "The official music video for Ed Sheeran - Perfect."
            ),
            YouTubeVideo(
                id = "L_LUpnjgPso",
                title = "Kalin Beats 503 - Electronic Vibes Online",
                channelTitle = "Distrito Music 503",
                thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/mqdefault.jpg",
                durationFormatted = "3:50",
                viewCountFormatted = "420K vistas",
                description = "Música electrónica con la identidad de Distrito Music 503."
            )
        )

        return if (query.isBlank()) curated else {
            val filtered = curated.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.channelTitle.contains(query, ignoreCase = true)
            }
            if (filtered.isNotEmpty()) filtered else curated
        }
    }
}
