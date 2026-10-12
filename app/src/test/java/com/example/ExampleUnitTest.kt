package com.example

import com.example.data.model.Track
import com.example.data.youtube.YouTubeVideo
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun track_durationFormatting_isCorrect() {
        val track = Track(
            id = "test_1",
            title = "Test Song",
            artist = "Test Artist",
            durationMs = 215000L,
            albumArtUrl = ""
        )
        assertEquals("03:35", track.durationFormatted)
    }

    @Test
    fun youtubeVideo_toTrack_convertsProperly() {
        val video = YouTubeVideo(
            videoId = "xyz123",
            title = "Video Song",
            channelTitle = "Channel 1",
            thumbnailUrl = "https://example.com/thumb.jpg",
            durationFormatted = "04:10",
            durationMs = 250000L
        )
        val track = video.toTrack()
        assertEquals("yt_xyz123", track.id)
        assertEquals("Video Song", track.title)
        assertEquals("Channel 1", track.artist)
        assertEquals(true, track.isOnline)
        assertEquals("xyz123", track.youtubeVideoId)
    }
}
