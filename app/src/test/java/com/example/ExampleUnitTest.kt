package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.Song
import com.example.state.MusicPlayerViewModel
import com.example.state.NavDestination
import com.example.ui.theme.AppThemeKey
import com.example.ui.theme.DistritoThemePresets
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testThemePresetsAvailable() {
    val themes = DistritoThemePresets.allThemes
    assertEquals(5, themes.size)

    val liquidGlass = DistritoThemePresets.getTheme(AppThemeKey.LIQUID_GLASS)
    assertEquals("Liquid Glass", liquidGlass.name)
    assertTrue(liquidGlass.isGlassmorphism)

    val barcelona = DistritoThemePresets.getTheme(AppThemeKey.BARCELONA)
    assertEquals("Barcelona", barcelona.name)

    val realMadrid = DistritoThemePresets.getTheme(AppThemeKey.REAL_MADRID)
    assertEquals("Real Madrid", realMadrid.name)

    val amoled = DistritoThemePresets.getTheme(AppThemeKey.AMOLED_DARK)
    assertEquals("AMOLED Dark", amoled.name)
    assertFalse(amoled.isGlassmorphism)

    val neon = DistritoThemePresets.getTheme(AppThemeKey.NEON)
    assertEquals("Neon", neon.name)
  }

  @Test
  fun testSongDurationFormatting() {
    val song = Song(
      id = "test_1",
      title = "Test Song",
      artist = "Test Artist",
      album = "Test Album",
      durationSeconds = 214
    )
    assertEquals("3:34", song.durationFormatted)
  }

  @Test
  fun testViewModelPlaybackControls() {
    val app: Application = ApplicationProvider.getApplicationContext()
    val vm = MusicPlayerViewModel(app)
    assertEquals(AppThemeKey.LIQUID_GLASS, vm.currentThemeKey)
    assertEquals(NavDestination.HOME, vm.currentDestination)

    // Theme switch
    vm.setTheme(AppThemeKey.BARCELONA)
    assertEquals(AppThemeKey.BARCELONA, vm.currentThemeKey)

    // Destination change
    vm.setDestination(NavDestination.LIBRARY)
    assertEquals(NavDestination.LIBRARY, vm.currentDestination)

    // Favorite toggle
    val songId = "song_3"
    val initialFav = vm.isSongFavorite(songId)
    vm.toggleFavorite(songId)
    assertEquals(!initialFav, vm.isSongFavorite(songId))
  }
}
