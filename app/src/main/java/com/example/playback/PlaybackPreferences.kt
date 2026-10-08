package com.example.playback

import android.content.Context
import android.content.SharedPreferences
import com.example.data.RepeatMode
import com.example.data.youtube.YouTubePlaybackMode

class PlaybackPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var lastSongId: String?
        get() = prefs.getString(KEY_LAST_SONG_ID, null)
        set(value) = prefs.edit().putString(KEY_LAST_SONG_ID, value).apply()

    var lastPositionMs: Long
        get() = prefs.getLong(KEY_LAST_POSITION_MS, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_POSITION_MS, value).apply()

    var isShuffle: Boolean
        get() = prefs.getBoolean(KEY_IS_SHUFFLE, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_SHUFFLE, value).apply()

    var repeatMode: RepeatMode
        get() {
            val name = prefs.getString(KEY_REPEAT_MODE, RepeatMode.ALL.name)
            return try {
                RepeatMode.valueOf(name ?: RepeatMode.ALL.name)
            } catch (e: Exception) {
                RepeatMode.ALL
            }
        }
        set(value) = prefs.edit().putString(KEY_REPEAT_MODE, value.name).apply()

    var lastQueueIds: List<String>
        get() {
            val raw = prefs.getString(KEY_QUEUE_IDS, "") ?: ""
            if (raw.isBlank()) return emptyList()
            return raw.split(",").filter { it.isNotBlank() }
        }
        set(value) {
            val raw = value.joinToString(",")
            prefs.edit().putString(KEY_QUEUE_IDS, raw).apply()
        }

    var isDspEnabled: Boolean
        get() = prefs.getBoolean(KEY_DSP_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_DSP_ENABLED, value).apply()

    var equalizerPreset: String
        get() = prefs.getString(KEY_EQ_PRESET, "Club 503") ?: "Club 503"
        set(value) = prefs.edit().putString(KEY_EQ_PRESET, value).apply()

    var bandLevels: List<Int>
        get() {
            val raw = prefs.getString(KEY_BAND_LEVELS, "5,3,-1,2,4") ?: "5,3,-1,2,4"
            return try {
                raw.split(",").map { it.trim().toInt() }
            } catch (_: Exception) {
                listOf(5, 3, -1, 2, 4)
            }
        }
        set(value) {
            prefs.edit().putString(KEY_BAND_LEVELS, value.joinToString(",")).apply()
        }

    var bassBoostPercent: Int
        get() = prefs.getInt(KEY_BASS_BOOST, 70)
        set(value) = prefs.edit().putInt(KEY_BASS_BOOST, value).apply()

    var virtualizerPercent: Int
        get() = prefs.getInt(KEY_VIRTUALIZER, 50)
        set(value) = prefs.edit().putInt(KEY_VIRTUALIZER, value).apply()

    var playbackSpeed: Float
        get() = prefs.getFloat(KEY_PLAYBACK_SPEED, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_PLAYBACK_SPEED, value).apply()

    var youtubePlaybackMode: YouTubePlaybackMode
        get() {
            val name = prefs.getString(KEY_YOUTUBE_PLAYBACK_MODE, YouTubePlaybackMode.AUDIO_ONLY.name)
            return try {
                YouTubePlaybackMode.valueOf(name ?: YouTubePlaybackMode.AUDIO_ONLY.name)
            } catch (_: Exception) {
                YouTubePlaybackMode.AUDIO_ONLY
            }
        }
        set(value) = prefs.edit().putString(KEY_YOUTUBE_PLAYBACK_MODE, value.name).apply()

    companion object {
        private const val PREFS_NAME = "distrito_playback_prefs"
        private const val KEY_LAST_SONG_ID = "last_song_id"
        private const val KEY_LAST_POSITION_MS = "last_position_ms"
        private const val KEY_IS_SHUFFLE = "is_shuffle"
        private const val KEY_REPEAT_MODE = "repeat_mode"
        private const val KEY_QUEUE_IDS = "last_queue_ids"
        private const val KEY_DSP_ENABLED = "dsp_enabled"
        private const val KEY_EQ_PRESET = "eq_preset"
        private const val KEY_BAND_LEVELS = "band_levels"
        private const val KEY_BASS_BOOST = "bass_boost"
        private const val KEY_VIRTUALIZER = "virtualizer"
        private const val KEY_PLAYBACK_SPEED = "playback_speed"
        private const val KEY_YOUTUBE_PLAYBACK_MODE = "youtube_playback_mode"
    }
}
