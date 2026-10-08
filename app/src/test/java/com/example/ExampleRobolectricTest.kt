package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Distrito Music 503", appName)
  }

  @Test
  fun `load distrito_logo drawable`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val drawable = context.getDrawable(R.drawable.distrito_logo)
    assertNotNull(drawable)
  }

  @Test
  fun `kalin message provider contains initial 5 messages`() {
    val messages = com.example.data.KalinMessageProvider.getAllMessages()
    assertEquals(5, messages.size)
    
    val randomMsg = com.example.data.KalinMessageProvider.getRandomMessage()
    assertNotNull(randomMsg)
    assertEquals("Kalin", randomMsg.author)
  }

  @Test
  fun `youtube video model generates correct watch and embed urls`() {
    val video = com.example.data.youtube.YouTubeVideo(
      id = "dQw4w9WgXcQ",
      title = "Test Song",
      channelTitle = "Test Channel",
      thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg"
    )
    assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", video.watchUrl)
    assert(video.embedUrl.contains("embed/dQw4w9WgXcQ"))
  }

  @Test
  fun `youtube playback modes enum has video and audio only`() {
    val modes = com.example.data.youtube.YouTubePlaybackMode.values()
    assertEquals(2, modes.size)
    assert(modes.contains(com.example.data.youtube.YouTubePlaybackMode.VIDEO))
    assert(modes.contains(com.example.data.youtube.YouTubePlaybackMode.AUDIO_ONLY))
  }

  @Test
  fun `demo audio provider creates valid audio uri`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val uri = com.example.data.DemoAudioProvider.getOrCreateDemoAudioUri(context)
    assertNotNull(uri)
    assert(uri.path?.endsWith(".wav") == true)
  }

  @Test
  fun `playback notification channel string is defined`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val channelName = context.getString(R.string.playback_notification_channel_name)
    assertNotNull(channelName)
    assert(channelName.isNotEmpty())
  }
}
