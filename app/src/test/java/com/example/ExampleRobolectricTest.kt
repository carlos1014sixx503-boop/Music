package com.example

import com.example.data.db.AppDatabase
import com.example.data.db.SearchHistoryItem
import com.example.data.youtube.YouTubePlaybackMode
import com.example.data.youtube.YouTubeVideo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private val context get() = RuntimeEnvironment.getApplication()

    @Test
    fun `youtube video model generates correct watch and embed urls`() {
        val video = YouTubeVideo(
            videoId = "dQw4w9WgXcQ",
            title = "Test Song",
            channelTitle = "Test Channel",
            thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg"
        )
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", video.watchUrl)
        assertTrue(video.embedUrl.contains("embed/dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", video.videoId)
    }

    @Test
    fun `youtube playback modes enum has video and audio only`() {
        val modes = YouTubePlaybackMode.values()
        assertEquals(2, modes.size)
        assertTrue(modes.contains(YouTubePlaybackMode.VIDEO))
        assertTrue(modes.contains(YouTubePlaybackMode.AUDIO_ONLY))
    }

    @Test
    fun `room search history dao inserts and retrieves history`() = runBlocking {
        val db = AppDatabase.getDatabase(context)
        val dao = db.searchHistoryDao()

        dao.clearAll()
        dao.insertQuery(SearchHistoryItem(query = "Dua Lipa", timestamp = System.currentTimeMillis()))
        dao.insertQuery(SearchHistoryItem(query = "The Weeknd", timestamp = System.currentTimeMillis() + 10))

        val historyList = dao.getAllHistory().first()
        assertTrue(historyList.isNotEmpty())
        assertEquals("The Weeknd", historyList.first().query)
    }
}
