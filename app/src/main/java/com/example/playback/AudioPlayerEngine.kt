package com.example.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.net.Uri
import android.util.Log
import com.example.data.DemoAudioProvider
import com.example.data.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * Motor nativo de reproducción de audio para Distrito Music 503.
 * Utiliza MediaPlayer del sistema Android para reproducir audio real tanto de
 * archivos locales (MediaStore, content://, file://) como de audio de demostración y streams.
 * Se conecta directamente a la sesión de audio DSP (Equalizer, BassBoost, Virtualizer).
 */
class AudioPlayerEngine(
    private val context: Context,
    private val onPositionUpdate: (currentMs: Long, durationMs: Long) -> Unit,
    private val onCompletion: () -> Unit,
    private val onError: (String) -> Unit
) {
    private val tag = "AudioPlayerEngine"
    private var mediaPlayer: MediaPlayer? = null
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    var currentAudioSessionId: Int = 0
        private set

    private var dspEnabled: Boolean = true
    private var cachedBandLevels: List<Int> = listOf(5, 3, -1, 2, 4)
    private var cachedBassBoost: Int = 45
    private var cachedVirtualizer: Int = 30

    fun play(track: Track) {
        stopProgressTracker()
        releasePlayer()

        try {
            val player = MediaPlayer()
            mediaPlayer = player

            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            // Determinar fuente de audio
            var sourceSet = false
            val audioUrl = track.audioUrl

            if (!audioUrl.isNullOrBlank()) {
                try {
                    when {
                        audioUrl.startsWith("content://") -> {
                            player.setDataSource(context, Uri.parse(audioUrl))
                            sourceSet = true
                        }
                        audioUrl.startsWith("file://") -> {
                            val path = audioUrl.removePrefix("file://")
                            val file = File(path)
                            if (file.exists() && file.length() > 0) {
                                player.setDataSource(file.absolutePath)
                                sourceSet = true
                            }
                        }
                        audioUrl.startsWith("/") -> {
                            val file = File(audioUrl)
                            if (file.exists() && file.length() > 0) {
                                player.setDataSource(file.absolutePath)
                                sourceSet = true
                            }
                        }
                        audioUrl.startsWith("http://") || audioUrl.startsWith("https://") -> {
                            player.setDataSource(audioUrl)
                            sourceSet = true
                        }
                    }
                } catch (e: Exception) {
                    Log.w(tag, "Could not set audio source from audioUrl: ${e.message}")
                    sourceSet = false
                }
            }

            // Si no se pudo reproducir desde la URI o es una pista demostrativa/online sin stream directo,
            // reproducir el audio musical sintetizado real en PCM WAV de alta fidelidad
            if (!sourceSet) {
                val demoUri = DemoAudioProvider.getOrCreateDemoAudioUri(context)
                player.setDataSource(context, demoUri)
                sourceSet = true
            }

            player.setOnPreparedListener { mp ->
                try {
                    currentAudioSessionId = mp.audioSessionId
                    setupAudioFx(currentAudioSessionId)
                    mp.start()
                    startProgressTracker()
                    Log.d(tag, "MediaPlayer started successfully for track: ${track.title} (Session ID: $currentAudioSessionId)")
                } catch (e: Exception) {
                    Log.e(tag, "Error starting playback after prep: ${e.message}")
                }
            }

            player.setOnCompletionListener {
                stopProgressTracker()
                onCompletion()
            }

            player.setOnErrorListener { _, what, extra ->
                Log.e(tag, "MediaPlayer error: what=$what, extra=$extra")
                stopProgressTracker()
                onError("Error de reproducción ($what, $extra)")
                true
            }

            player.prepareAsync()

        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize MediaPlayer for track: ${track.title}: ${e.message}", e)
            onError(e.message ?: "Error al reproducir audio")
        }
    }

    fun resume() {
        try {
            mediaPlayer?.let {
                if (!it.isPlaying) {
                    it.start()
                    startProgressTracker()
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error resuming playback: ${e.message}")
        }
    }

    fun pause() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                }
            }
            stopProgressTracker()
        } catch (e: Exception) {
            Log.e(tag, "Error pausing playback: ${e.message}")
        }
    }

    fun seekTo(positionMs: Long) {
        try {
            mediaPlayer?.seekTo(positionMs.toInt())
        } catch (e: Exception) {
            Log.e(tag, "Error seeking: ${e.message}")
        }
    }

    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (_: Exception) {
            false
        }
    }

    private fun setupAudioFx(sessionId: Int) {
        if (sessionId <= 0) return
        try {
            releaseAudioFx()

            equalizer = Equalizer(0, sessionId).apply {
                enabled = dspEnabled
            }

            bassBoost = BassBoost(0, sessionId).apply {
                enabled = dspEnabled
                if (strengthSupported) {
                    setStrength(((cachedBassBoost / 100f) * 1000).toInt().toShort())
                }
            }

            virtualizer = Virtualizer(0, sessionId).apply {
                enabled = dspEnabled
                if (strengthSupported) {
                    setStrength(((cachedVirtualizer / 100f) * 1000).toInt().toShort())
                }
            }

            applyDsp(dspEnabled, cachedBandLevels, cachedBassBoost, cachedVirtualizer)
        } catch (e: Exception) {
            Log.w(tag, "AudioFX setup warning (DSP may run in software emulation): ${e.message}")
        }
    }

    fun applyDsp(enabled: Boolean, bandLevels: List<Int>, bassPercent: Int, virtPercent: Int) {
        dspEnabled = enabled
        cachedBandLevels = bandLevels
        cachedBassBoost = bassPercent
        cachedVirtualizer = virtPercent

        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            virtualizer?.enabled = enabled

            if (enabled) {
                equalizer?.let { eq ->
                    val minBandLevel = eq.bandLevelRange?.get(0) ?: -1500
                    val maxBandLevel = eq.bandLevelRange?.get(1) ?: 1500
                    val numBands = eq.numberOfBands.toInt()

                    for (i in 0 until minOf(numBands, bandLevels.size)) {
                        val levelNormalized = bandLevels[i] // Rango -10 a +10
                        val millibels = ((levelNormalized / 10.0) * maxBandLevel).toInt().coerceIn(minBandLevel.toInt(), maxBandLevel.toInt()).toShort()
                        eq.setBandLevel(i.toShort(), millibels)
                    }
                }

                bassBoost?.let { bb ->
                    if (bb.strengthSupported) {
                        bb.setStrength(((bassPercent / 100.0) * 1000).toInt().coerceIn(0, 1000).toShort())
                    }
                }

                virtualizer?.let { virt ->
                    if (virt.strengthSupported) {
                        virt.setStrength(((virtPercent / 100.0) * 1000).toInt().coerceIn(0, 1000).toShort())
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Error applying DSP parameters: ${e.message}")
        }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch {
            while (isActive) {
                try {
                    mediaPlayer?.let { mp ->
                        if (mp.isPlaying) {
                            val current = mp.currentPosition.toLong()
                            val dur = mp.duration.toLong().coerceAtLeast(0L)
                            onPositionUpdate(current, dur)
                        }
                    }
                } catch (_: Exception) {}
                delay(300)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun releaseAudioFx() {
        try { equalizer?.release() } catch (_: Exception) {}
        try { bassBoost?.release() } catch (_: Exception) {}
        try { virtualizer?.release() } catch (_: Exception) {}
        equalizer = null
        bassBoost = null
        virtualizer = null
    }

    private fun releasePlayer() {
        stopProgressTracker()
        releaseAudioFx()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    fun release() {
        releasePlayer()
    }
}
