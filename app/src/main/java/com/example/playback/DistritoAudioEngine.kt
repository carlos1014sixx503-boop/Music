package com.example.playback

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionToken
import com.example.data.DemoAudioProvider
import com.example.data.RepeatMode
import com.example.data.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * Motor central de audio para Distrito Music 503 basado en Jetpack Media3 (ExoPlayer y MediaController).
 * Gestiona la conexión mediante MediaController al DistritoMediaPlaybackService, la sincronización de colas,
 * los controles en segundo plano, la pantalla bloqueada y los eventos de audífonos / Bluetooth.
 *
 * IMPORTANTE: No crea reproductores secundarios independientes. Toda la reproducción está unificada
 * a través de la MediaSession del servicio para garantizar presencia en la barra de notificaciones y controles de sistema.
 */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class DistritoAudioEngine private constructor(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val preferences = PlaybackPreferences(context)

    // Referencias al servicio y MediaController vinculados a la MediaSession única
    private var servicePlayer: ExoPlayer? = null
    private var activeMediaSession: MediaSession? = null
    private var mediaController: MediaController? = null

    // Player activo unificado conectado directamente a MediaSession
    val player: Player?
        get() = mediaController ?: servicePlayer

    // Acción de reproducción diferida en caso de llamada previa a la finalización de la conexión
    private var pendingPlaybackAction: ((Player) -> Unit)? = null

    // Cola de canciones actual
    private var currentQueue: List<Song> = emptyList()

    // Estados observables para la interfaz de usuario
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isShuffle = MutableStateFlow(preferences.isShuffle)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(preferences.repeatMode)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _queueFlow = MutableStateFlow<List<Song>>(emptyList())
    val queueFlow: StateFlow<List<Song>> = _queueFlow.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(preferences.playbackSpeed)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _audioSessionId = MutableStateFlow(0)
    val audioSessionId: StateFlow<Int> = _audioSessionId.asStateFlow()

    private val _bufferedPositionMs = MutableStateFlow(0L)
    val bufferedPositionMs: StateFlow<Long> = _bufferedPositionMs.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // DSP / Ecualizador y Efectos
    private var equalizerEffect: Equalizer? = null
    private var bassBoostEffect: BassBoost? = null
    private var virtualizerEffect: Virtualizer? = null

    private val _isDspEnabled = MutableStateFlow(preferences.isDspEnabled)
    val isDspEnabled: StateFlow<Boolean> = _isDspEnabled.asStateFlow()

    private val _equalizerPreset = MutableStateFlow(preferences.equalizerPreset)
    val equalizerPreset: StateFlow<String> = _equalizerPreset.asStateFlow()

    private val _bandLevels = MutableStateFlow(preferences.bandLevels)
    val bandLevels: StateFlow<List<Int>> = _bandLevels.asStateFlow()

    private val _bassBoostPercent = MutableStateFlow(preferences.bassBoostPercent)
    val bassBoostPercent: StateFlow<Int> = _bassBoostPercent.asStateFlow()

    private val _virtualizerPercent = MutableStateFlow(preferences.virtualizerPercent)
    val virtualizerPercent: StateFlow<Int> = _virtualizerPercent.asStateFlow()

    private var positionTickerJob: Job? = null
    private var lastSavedPositionSec = 0L

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(playing: Boolean) {
            val songName = _currentSong.value?.title ?: "sin pista"
            Log.d(TAG, "ExoPlayer onIsPlayingChanged: playing=$playing, canción='$songName', playWhenReady=${player?.playWhenReady}")
            _isPlaying.value = playing
            if (playing) {
                startPositionTicker()
            } else {
                stopPositionTicker()
                saveCurrentPlaybackState()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateName = when (playbackState) {
                Player.STATE_IDLE -> "STATE_IDLE"
                Player.STATE_BUFFERING -> "STATE_BUFFERING"
                Player.STATE_READY -> "STATE_READY"
                Player.STATE_ENDED -> "STATE_ENDED"
                else -> "UNKNOWN"
            }
            Log.d(TAG, "ExoPlayer onPlaybackStateChanged: $stateName (duración=${player?.duration} ms)")

            when (playbackState) {
                Player.STATE_READY -> {
                    val dur = player?.duration ?: 0L
                    if (dur > 0L) {
                        _durationMs.value = dur
                    }
                }
                Player.STATE_ENDED -> {
                    _isPlaying.value = false
                    val active = player
                    if (active != null && _repeatMode.value == RepeatMode.OFF && !active.hasNextMediaItem()) {
                        active.seekTo(0, 0L)
                        active.pause()
                    }
                }
                Player.STATE_IDLE, Player.STATE_BUFFERING -> {}
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (mediaItem != null) {
                val songId = mediaItem.mediaId
                val matchedSong = currentQueue.firstOrNull { it.id == songId }
                    ?: _currentSong.value?.takeIf { it.id == songId }
                if (matchedSong != null) {
                    _currentSong.value = matchedSong
                    val active = player
                    _durationMs.value = if (matchedSong.durationSeconds > 0) {
                        matchedSong.durationSeconds * 1000L
                    } else (active?.duration?.coerceAtLeast(0L) ?: 0L)
                    preferences.lastSongId = matchedSong.id
                }
                _currentPositionMs.value = 0L
                Log.d(TAG, "onMediaItemTransition: nueva canción '${matchedSong?.title}', mediaId=$songId, reason=$reason")
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            val songTitle = _currentSong.value?.title ?: "pista seleccionada"
            Log.e(TAG, "onPlayerError en reproducción: ${error.message} (código=${error.errorCodeName})", error)
            _errorMessage.value = "No se pudo reproducir \"$songTitle\". El formato no es compatible o el archivo no está disponible."

            scope.launch {
                delay(5000L)
                if (_errorMessage.value?.contains(songTitle) == true) {
                    _errorMessage.value = null
                }
            }

            val active = player
            if (active?.hasNextMediaItem() == true) {
                scope.launch {
                    delay(600L)
                    active.seekToNextMediaItem()
                    active.prepare()
                    active.play()
                }
            }
        }
    }

    init {
        Log.d(TAG, "Inicializando DistritoAudioEngine y conectando a MediaSessionService")
        ensureServiceStarted()
        connectMediaController()
    }

    private fun connectMediaController() {
        try {
            val sessionToken = SessionToken(
                context,
                ComponentName(context, DistritoMediaPlaybackService::class.java)
            )
            Log.d(TAG, "Iniciando MediaController.Builder con SessionToken: $sessionToken")
            val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            controllerFuture.addListener(
                {
                    try {
                        val controller = controllerFuture.get()
                        mediaController = controller
                        Log.d(TAG, "MediaController conectado exitosamente a MediaSession: ${controller.connectedToken}")
                        controller.addListener(playerListener)
                        controller.shuffleModeEnabled = _isShuffle.value
                        applyRepeatModeToPlayer(controller, _repeatMode.value)
                        try {
                            controller.setPlaybackSpeed(_playbackSpeed.value)
                        } catch (_: Exception) {}

                        // Si había una acción de reproducción pendiente, ejecutarla ahora
                        pendingPlaybackAction?.let { action ->
                            Log.d(TAG, "Ejecutando acción de reproducción pendiente en MediaController recién conectado")
                            action(controller)
                            pendingPlaybackAction = null
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al resolver MediaController: ${e.message}", e)
                    }
                },
                ContextCompat.getMainExecutor(context)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al configurar MediaController.Builder: ${e.message}", e)
        }
    }

    fun attachServicePlayer(srvPlayer: ExoPlayer, session: MediaSession) {
        Log.d(TAG, "attachServicePlayer: adjuntando ExoPlayer de servicio y MediaSession (${session.id}) a AudioEngine")
        this.servicePlayer = srvPlayer
        this.activeMediaSession = session
        _audioSessionId.value = srvPlayer.audioSessionId
        srvPlayer.shuffleModeEnabled = _isShuffle.value
        applyRepeatModeToPlayer(srvPlayer, _repeatMode.value)
        try {
            srvPlayer.setPlaybackSpeed(_playbackSpeed.value)
        } catch (_: Exception) {}
        srvPlayer.addListener(playerListener)

        initAudioEffects(srvPlayer.audioSessionId)

        // Si MediaController aún no está listo pero tenemos el servicePlayer (misma sesión), ejecutar acción pendiente
        if (mediaController == null && pendingPlaybackAction != null) {
            Log.d(TAG, "Ejecutando acción de reproducción pendiente directamente en servicePlayer de MediaSession")
            pendingPlaybackAction?.let { action ->
                action(srvPlayer)
                pendingPlaybackAction = null
            }
        }
    }

    fun detachServicePlayer() {
        Log.d(TAG, "detachServicePlayer: desacoplando referencias del servicio")
        releaseAudioEffects()
        servicePlayer = null
        activeMediaSession = null
    }

    /**
     * Sincroniza la cola disponible y restaura la última canción si es necesario.
     */
    fun syncQueue(songs: List<Song>) {
        if (songs.isEmpty()) return
        currentQueue = songs
        _queueFlow.value = songs

        if (_currentSong.value == null) {
            val savedSongId = preferences.lastSongId
            val savedPositionMs = preferences.lastPositionMs
            val targetSong = songs.firstOrNull { it.id == savedSongId } ?: songs.firstOrNull()

            if (targetSong != null) {
                _currentSong.value = targetSong
                _currentPositionMs.value = savedPositionMs.coerceAtLeast(0L)
                _durationMs.value = targetSong.durationSeconds * 1000L

                val mediaItems = songs.map { toMediaItem(it) }
                val targetIndex = songs.indexOfFirst { it.id == targetSong.id }.coerceAtLeast(0)
                val active = player
                if (active != null) {
                    try {
                        active.setMediaItems(mediaItems, targetIndex, savedPositionMs)
                        active.prepare()
                    } catch (_: Exception) {}
                }
            }
        }
    }

    /**
     * Inicia la reproducción de una canción específica con su cola correspondiente.
     * Toda la reproducción se ejecuta a través de la MediaSession (MediaController o servicePlayer)
     * para que la notificación multimedia y controles de sistema siempre se activen.
     */
    fun playSong(song: Song, queue: List<Song> = currentQueue) {
        val effectiveQueue = if (queue.isNotEmpty()) queue else listOf(song)
        currentQueue = effectiveQueue
        _queueFlow.value = effectiveQueue
        _currentSong.value = song
        _durationMs.value = song.durationSeconds * 1000L
        _currentPositionMs.value = 0L

        // Verificar existencia de archivo local si hay ruta física
        if (song.filePath.isNotBlank()) {
            val file = File(song.filePath)
            if (!file.exists() && song.contentUri.isBlank()) {
                _errorMessage.value = "El archivo \"${song.title}\" ya no existe en el almacenamiento."
                return
            }
        }

        val mediaItems = effectiveQueue.map { toMediaItem(it) }
        val startIndex = effectiveQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)

        ensureServiceStarted()

        val playbackAction: (Player) -> Unit = { activePlayer ->
            try {
                Log.d(
                    TAG,
                    "Comenzando reproducción en Player (${activePlayer.javaClass.simpleName}): " +
                        "título='${song.title}', artista='${song.artist}', items=${mediaItems.size}, index=$startIndex"
                )
                activePlayer.setMediaItems(mediaItems, startIndex, 0L)
                activePlayer.prepare()
                activePlayer.play()
                _isPlaying.value = true
                saveCurrentPlaybackState()
            } catch (e: Exception) {
                Log.e(TAG, "Error al iniciar reproducción en Player: ${e.message}", e)
                _errorMessage.value = "Error al iniciar reproducción: ${e.localizedMessage}"
            }
        }

        val active = player
        if (active != null) {
            playbackAction(active)
        } else {
            Log.d(TAG, "Player de MediaSession aún no listo; encolando reproducción pendiente")
            pendingPlaybackAction = playbackAction
            connectMediaController()
        }
    }

    fun togglePlayPause() {
        val active = player
        if (active == null) {
            ensureServiceStarted()
            connectMediaController()
            return
        }

        if (_currentSong.value == null && currentQueue.isNotEmpty()) {
            playSong(currentQueue.first())
            return
        }

        if (active.isPlaying) {
            Log.d(TAG, "Pausando reproducción desde togglePlayPause")
            active.pause()
            _isPlaying.value = false
        } else {
            Log.d(TAG, "Reanudando reproducción desde togglePlayPause")
            ensureServiceStarted()
            if (active.playbackState == Player.STATE_IDLE) {
                val current = _currentSong.value
                if (current != null) {
                    playSong(current)
                    return
                }
            }
            active.play()
            _isPlaying.value = true
        }
    }

    fun pause() {
        val active = player
        if (active?.isPlaying == true) {
            Log.d(TAG, "Pausando reproducción desde pause()")
            active.pause()
            _isPlaying.value = false
        }
    }

    fun playNext() {
        val active = player ?: return
        ensureServiceStarted()

        if (active.hasNextMediaItem()) {
            Log.d(TAG, "playNext: avanzando a siguiente MediaItem en MediaSession")
            active.seekToNextMediaItem()
            active.play()
        } else if (_repeatMode.value == RepeatMode.ALL && currentQueue.isNotEmpty()) {
            Log.d(TAG, "playNext: reiniciando cola desde índice 0 (RepeatMode.ALL)")
            active.seekTo(0, 0L)
            active.play()
        } else if (currentQueue.isNotEmpty()) {
            val currentIndex = currentQueue.indexOfFirst { it.id == _currentSong.value?.id }
            val nextIndex = if (currentIndex in 0 until currentQueue.size - 1) currentIndex + 1 else 0
            playSong(currentQueue[nextIndex])
        }
    }

    fun playPrevious() {
        val active = player ?: return
        ensureServiceStarted()

        if (active.currentPosition > 3000L) {
            Log.d(TAG, "playPrevious: reiniciando canción actual (posición > 3s)")
            active.seekTo(0L)
            _currentPositionMs.value = 0L
            return
        }

        if (active.hasPreviousMediaItem()) {
            Log.d(TAG, "playPrevious: retrocediendo a anterior MediaItem en MediaSession")
            active.seekToPreviousMediaItem()
            active.play()
        } else if (_repeatMode.value == RepeatMode.ALL && currentQueue.isNotEmpty()) {
            Log.d(TAG, "playPrevious: saltando al final de la cola (RepeatMode.ALL)")
            val lastIndex = currentQueue.size - 1
            active.seekTo(lastIndex, 0L)
            active.play()
        } else {
            active.seekTo(0L)
            _currentPositionMs.value = 0L
        }
    }

    fun seekToSeconds(seconds: Int) {
        val targetMs = (seconds * 1000L).coerceAtLeast(0L)
        val active = player
        active?.seekTo(targetMs)
        _currentPositionMs.value = targetMs
        saveCurrentPlaybackState()
    }

    fun seekForward(seconds: Int = 10) {
        val active = player ?: return
        val currentMs = active.currentPosition
        val duration = if (active.duration > 0L) active.duration else (_currentSong.value?.durationSeconds?.times(1000L) ?: 0L)
        val targetMs = (currentMs + (seconds * 1000L)).coerceAtMost(duration)
        active.seekTo(targetMs)
        _currentPositionMs.value = targetMs
    }

    fun seekBackward(seconds: Int = 10) {
        val active = player ?: return
        val currentMs = active.currentPosition
        val targetMs = (currentMs - (seconds * 1000L)).coerceAtLeast(0L)
        active.seekTo(targetMs)
        _currentPositionMs.value = targetMs
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0.0f, 1.0f)
        _volume.value = clamped
        try {
            val active = player
            if (active is ExoPlayer) {
                active.volume = clamped
            } else if (servicePlayer != null) {
                servicePlayer?.volume = clamped
            }
        } catch (_: Exception) {}
    }

    fun toggleShuffle(): Boolean {
        val newShuffle = !_isShuffle.value
        _isShuffle.value = newShuffle
        val active = player
        active?.shuffleModeEnabled = newShuffle
        preferences.isShuffle = newShuffle
        return newShuffle
    }

    fun toggleRepeat(): RepeatMode {
        val newMode = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        setRepeatMode(newMode)
        return newMode
    }

    fun setRepeatMode(mode: RepeatMode) {
        _repeatMode.value = mode
        val active = player
        if (active != null) {
            applyRepeatModeToPlayer(active, mode)
        }
        preferences.repeatMode = mode
    }

    private fun applyRepeatModeToPlayer(targetPlayer: Player, mode: RepeatMode) {
        targetPlayer.repeatMode = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
    }

    fun dismissErrorMessage() {
        _errorMessage.value = null
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = (Math.round(speed * 100f) / 100f).coerceIn(0.5f, 2.0f)
        _playbackSpeed.value = clamped
        preferences.playbackSpeed = clamped
        try {
            player?.setPlaybackSpeed(clamped)
        } catch (_: Exception) {}
    }

    fun addToQueue(song: Song) {
        val updated = currentQueue.toMutableList()
        updated.add(song)
        currentQueue = updated
        _queueFlow.value = updated
        try {
            player?.addMediaItem(toMediaItem(song))
        } catch (_: Exception) {}
    }

    fun playNextInQueue(song: Song) {
        val currentSongId = _currentSong.value?.id
        val currentIndex = currentQueue.indexOfFirst { it.id == currentSongId }
        val insertIndex = if (currentIndex != -1) (currentIndex + 1).coerceAtMost(currentQueue.size) else currentQueue.size
        val updated = currentQueue.toMutableList()
        updated.add(insertIndex, song)
        currentQueue = updated
        _queueFlow.value = updated
        try {
            player?.addMediaItem(insertIndex, toMediaItem(song))
        } catch (_: Exception) {}
    }

    fun removeQueueItem(index: Int) {
        if (index in currentQueue.indices) {
            val updated = currentQueue.toMutableList()
            updated.removeAt(index)
            currentQueue = updated
            _queueFlow.value = updated
            try {
                player?.removeMediaItem(index)
            } catch (_: Exception) {}
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        if (fromIndex in currentQueue.indices && toIndex in currentQueue.indices && fromIndex != toIndex) {
            val updated = currentQueue.toMutableList()
            val item = updated.removeAt(fromIndex)
            updated.add(toIndex, item)
            currentQueue = updated
            _queueFlow.value = updated
            try {
                player?.moveMediaItem(fromIndex, toIndex)
            } catch (_: Exception) {}
        }
    }

    fun playQueueIndex(index: Int) {
        if (index in currentQueue.indices) {
            val targetSong = currentQueue[index]
            val active = player
            if (active != null) {
                try {
                    active.seekTo(index, 0L)
                    active.play()
                    _currentSong.value = targetSong
                } catch (_: Exception) {
                    playSong(targetSong, currentQueue)
                }
            } else {
                playSong(targetSong, currentQueue)
            }
        }
    }

    fun clearQueueExceptCurrent() {
        val current = _currentSong.value
        if (current != null) {
            currentQueue = listOf(current)
            _queueFlow.value = listOf(current)
            val active = player
            if (active != null) {
                val count = active.mediaItemCount
                val curIdx = active.currentMediaItemIndex
                for (i in count - 1 downTo 0) {
                    if (i != curIdx) {
                        try { active.removeMediaItem(i) } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    fun getAudioStats(): AudioStats {
        val song = _currentSong.value
        val active = player
        val exo = active as? ExoPlayer ?: servicePlayer
        val format = exo?.audioFormat

        val codecName = when {
            format?.sampleMimeType != null -> format.sampleMimeType?.substringAfter('/')?.uppercase() ?: "AUDIO"
            song?.audioFormat?.contains("FLAC", ignoreCase = true) == true -> "FLAC"
            song?.audioFormat?.contains("WAV", ignoreCase = true) == true -> "PCM / WAV"
            song?.audioFormat?.contains("DSD", ignoreCase = true) == true -> "DSD"
            song?.audioFormat?.contains("MP3", ignoreCase = true) == true -> "MP3"
            else -> "AAC / MP4A"
        }

        val sampleRate = when {
            format?.sampleRate != null && format.sampleRate > 0 -> format.sampleRate
            song?.sampleRateHz != null && song.sampleRateHz > 0 -> song.sampleRateHz
            song?.audioFormat?.contains("192kHz", ignoreCase = true) == true -> 192000
            song?.audioFormat?.contains("96kHz", ignoreCase = true) == true -> 96000
            song?.audioFormat?.contains("48kHz", ignoreCase = true) == true -> 48000
            else -> 44100
        }

        val bitDepth = when {
            song?.audioFormat?.contains("32-bit", ignoreCase = true) == true -> 32
            song?.audioFormat?.contains("24-bit", ignoreCase = true) == true -> 24
            else -> 16
        }

        val bitrate = when {
            format?.bitrate != null && format.bitrate > 0 -> format.bitrate / 1000
            song?.bitrateKbps != null && song.bitrateKbps > 0 -> song.bitrateKbps
            song?.audioFormat?.contains("FLAC", ignoreCase = true) == true -> 1411
            song?.audioFormat?.contains("320", ignoreCase = true) == true -> 320
            else -> 256
        }

        val channels = format?.channelCount ?: 2
        val sessId = _audioSessionId.value
        val bufMs = active?.bufferedPosition ?: _bufferedPositionMs.value
        val durMs = if (_durationMs.value > 0) _durationMs.value else (active?.duration ?: 0L)
        val bufPct = if (durMs > 0) ((bufMs * 100) / durMs).toInt().coerceIn(0, 100) else 100

        return AudioStats(
            title = song?.title ?: "Sin pista activa",
            artist = song?.artist ?: "Distrito Music 503",
            album = song?.album ?: "Distrito Master",
            codec = codecName,
            containerFormat = song?.audioFormat ?: "Direct Native Audio",
            sampleRateHz = sampleRate,
            bitDepthBits = bitDepth,
            bitrateKbps = bitrate,
            channelCount = channels,
            audioSessionId = sessId,
            bufferHealthPercent = bufPct,
            bufferBufferedMs = bufMs,
            currentPositionMs = _currentPositionMs.value,
            totalDurationMs = durMs,
            playbackSpeed = _playbackSpeed.value,
            isDspActive = _isDspEnabled.value,
            equalizerPreset = _equalizerPreset.value,
            bassBoostPercent = _bassBoostPercent.value,
            virtualizerPercent = _virtualizerPercent.value,
            outputSink = if (sessId > 0) "Android AudioTrack / OpenSL ES Direct HAL" else "Media3 Engine",
            sourcePath = song?.filePath?.ifBlank { song.contentUri } ?: "Pista Local 503",
            fileSizeBytes = song?.sizeBytes ?: ((durMs / 1000L).coerceAtLeast(180L) * 320L * 128L)
        )
    }

    private fun initAudioEffects(sessionId: Int) {
        if (sessionId == C.AUDIO_SESSION_ID_UNSET || sessionId <= 0) return
        try {
            releaseAudioEffects()
            equalizerEffect = Equalizer(0, sessionId).apply {
                enabled = _isDspEnabled.value
            }
            applyBandsToHardware()

            bassBoostEffect = BassBoost(0, sessionId).apply {
                enabled = _isDspEnabled.value
                if (strengthSupported) {
                    setStrength((_bassBoostPercent.value * 10).toShort().coerceIn(0, 1000))
                }
            }

            virtualizerEffect = Virtualizer(0, sessionId).apply {
                enabled = _isDspEnabled.value
                if (strengthSupported) {
                    setStrength((_virtualizerPercent.value * 10).toShort().coerceIn(0, 1000))
                }
            }
            Log.d(TAG, "AudioFX configurado exitosamente en sesión de audio $sessionId")
        } catch (e: Exception) {
            Log.w(TAG, "AudioFX no disponible en esta sesión ($sessionId): ${e.message}")
        }
    }

    private fun releaseAudioEffects() {
        try {
            equalizerEffect?.release()
            equalizerEffect = null
            bassBoostEffect?.release()
            bassBoostEffect = null
            virtualizerEffect?.release()
            virtualizerEffect = null
        } catch (e: Exception) {
            Log.w(TAG, "Error liberando AudioFX: ${e.message}")
        }
    }

    private fun applyBandsToHardware() {
        val eq = equalizerEffect ?: return
        try {
            val numBands = eq.numberOfBands.toInt().coerceAtMost(5)
            val levels = _bandLevels.value
            val bandRange = eq.bandLevelRange // [minMillibels, maxMillibels] (normalmente -1500 a 1500)
            val minMb = bandRange.getOrNull(0)?.toInt() ?: -1200
            val maxMb = bandRange.getOrNull(1)?.toInt() ?: 1200

            for (i in 0 until numBands) {
                val db = levels.getOrElse(i) { 0 }
                val millibels = (db * 100).coerceIn(minMb, maxMb).toShort()
                eq.setBandLevel(i.toShort(), millibels)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error aplicando bandas de ecualizador a hardware: ${e.message}")
        }
    }

    fun setDspEnabled(enabled: Boolean) {
        _isDspEnabled.value = enabled
        preferences.isDspEnabled = enabled
        try {
            equalizerEffect?.enabled = enabled
            bassBoostEffect?.enabled = enabled
            virtualizerEffect?.enabled = enabled
        } catch (e: Exception) {
            Log.w(TAG, "Error al conmutar estado DSP: ${e.message}")
        }
    }

    fun setBandLevel(bandIndex: Int, levelDb: Int) {
        val clampedDb = levelDb.coerceIn(-12, 12)
        val current = _bandLevels.value.toMutableList()
        while (current.size < 5) current.add(0)
        if (bandIndex in 0 until 5) {
            current[bandIndex] = clampedDb
        }
        _bandLevels.value = current
        preferences.bandLevels = current
        _equalizerPreset.value = "Personalizado"
        preferences.equalizerPreset = "Personalizado"

        applyBandsToHardware()
    }

    fun setEqualizerPreset(presetName: String, levels: List<Int>) {
        _equalizerPreset.value = presetName
        preferences.equalizerPreset = presetName
        val normalized = levels.map { it.coerceIn(-12, 12) }
        _bandLevels.value = normalized
        preferences.bandLevels = normalized

        applyBandsToHardware()
    }

    fun setBassBoost(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        _bassBoostPercent.value = clamped
        preferences.bassBoostPercent = clamped
        try {
            val bb = bassBoostEffect
            if (bb != null && bb.strengthSupported) {
                bb.setStrength((clamped * 10).toShort().coerceIn(0, 1000))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error ajustando BassBoost: ${e.message}")
        }
    }

    fun setVirtualizer(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        _virtualizerPercent.value = clamped
        preferences.virtualizerPercent = clamped
        try {
            val virt = virtualizerEffect
            if (virt != null && virt.strengthSupported) {
                virt.setStrength((clamped * 10).toShort().coerceIn(0, 1000))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error ajustando Virtualizer: ${e.message}")
        }
    }

    fun resetEqualizer() {
        setEqualizerPreset("Plano", listOf(0, 0, 0, 0, 0))
        setBassBoost(50)
        setVirtualizer(50)
    }

    private fun startPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = scope.launch {
            while (isActive) {
                delay(300L)
                val active = player
                if (active != null) {
                    val pos = active.currentPosition
                    _currentPositionMs.value = pos
                    val dur = active.duration
                    if (dur > 0L) {
                        _durationMs.value = dur
                    }
                    _bufferedPositionMs.value = active.bufferedPosition

                    val currentSec = pos / 1000L
                    if (Math.abs(currentSec - lastSavedPositionSec) >= 5) {
                        lastSavedPositionSec = currentSec
                        preferences.lastPositionMs = pos
                    }
                }
            }
        }
    }

    private fun stopPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = null
    }

    private fun saveCurrentPlaybackState() {
        val song = _currentSong.value
        val active = player
        if (song != null && active != null) {
            preferences.lastSongId = song.id
            preferences.lastPositionMs = active.currentPosition
            preferences.lastQueueIds = currentQueue.map { it.id }
        }
    }

    fun ensureServiceStarted() {
        val intent = Intent(context, DistritoMediaPlaybackService::class.java)
        try {
            context.startService(intent)
            Log.d(TAG, "ensureServiceStarted: startService invocado exitosamente")
        } catch (e: Exception) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                    Log.d(TAG, "ensureServiceStarted: startForegroundService invocado como alternativa")
                }
            } catch (e2: Exception) {
                Log.e(TAG, "Error en ensureServiceStarted: ${e2.message}", e2)
            }
        }
    }

    private fun toMediaItem(song: Song): MediaItem {
        val uri: Uri = when {
            song.contentUri.isNotBlank() -> Uri.parse(song.contentUri)
            song.filePath.isNotBlank() -> Uri.fromFile(File(song.filePath))
            else -> DemoAudioProvider.getOrCreateDemoAudioUri(context)
        }

        val displayArtist = song.artist.ifBlank { "Distrito Music 503" }
        val displayAlbum = song.album.ifBlank { "Distrito Music 503" }

        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(displayArtist)
            .setAlbumTitle(displayAlbum)
            .setDisplayTitle(song.title)
            .setSubtitle(displayArtist)
            .setDescription(displayAlbum)

        if (!song.albumArtUri.isNullOrBlank()) {
            try {
                metadataBuilder.setArtworkUri(Uri.parse(song.albumArtUri))
            } catch (_: Exception) {}
        } else if (song.coverResId != null) {
            try {
                metadataBuilder.setArtworkUri(Uri.parse("android.resource://${context.packageName}/${song.coverResId}"))
            } catch (_: Exception) {}
        }

        return MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(uri)
            .setMediaMetadata(metadataBuilder.build())
            .build()
    }

    companion object {
        private const val TAG = "DistritoMediaPlayback"

        @Volatile
        private var instance: DistritoAudioEngine? = null

        fun getInstance(context: Context): DistritoAudioEngine {
            return instance ?: synchronized(this) {
                instance ?: DistritoAudioEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}
