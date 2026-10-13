package com.example.state

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.SearchHistoryItem
import com.example.data.model.Track
import com.example.data.scanner.AndroidAudioScanner
import com.example.data.toTrack
import com.example.data.youtube.YouTubeAccountProfile
import com.example.data.youtube.YouTubePlaylist
import com.example.data.youtube.YouTubeRepository
import com.example.data.youtube.YouTubeVideo
import com.example.data.repository.SearchHistoryRepository
import com.example.playback.AudioPlayerEngine
import com.example.playback.MediaPlaybackManager
import com.example.playback.MediaPlaybackService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MusicPlayerState(application: Application) : AndroidViewModel(application) {

    private val searchHistoryRepo: SearchHistoryRepository

    val searchHistory: StateFlow<List<SearchHistoryItem>>

    private var audioPlayerEngine: AudioPlayerEngine? = null

    // Canciones locales del almacenamiento del dispositivo
    private val _localDeviceTracks = MutableStateFlow<List<Track>>(emptyList())
    val localDeviceTracks: StateFlow<List<Track>> = _localDeviceTracks.asStateFlow()

    // Perfil y Playlists sincronizadas de YouTube
    private val _userAccountProfile = MutableStateFlow(YouTubeAccountProfile())
    val userAccountProfile: StateFlow<YouTubeAccountProfile> = _userAccountProfile.asStateFlow()

    private val _userPlaylists = MutableStateFlow<List<YouTubePlaylist>>(YouTubeRepository().getDefaultPlaylists())
    val userPlaylists: StateFlow<List<YouTubePlaylist>> = _userPlaylists.asStateFlow()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(87000L) // ~1:27 initial position
    val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(179000L) // ~2:59 duration like reference image
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playlist = MutableStateFlow<List<Track>>(emptyList())
    val playlist: StateFlow<List<Track>> = _playlist.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _isRepeat = MutableStateFlow(true)
    val isRepeat: StateFlow<Boolean> = _isRepeat.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(setOf("track_blinding_lights", "yt_4NRXx6U8ABQ"))
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _history = MutableStateFlow<List<Track>>(emptyList())
    val history: StateFlow<List<Track>> = _history.asStateFlow()

    // YouTube Modal State
    private val _activeYouTubeVideo = MutableStateFlow<YouTubeVideo?>(null)
    val activeYouTubeVideo: StateFlow<YouTubeVideo?> = _activeYouTubeVideo.asStateFlow()

    private val _isYouTubeModalVisible = MutableStateFlow(false)
    val isYouTubeModalVisible: StateFlow<Boolean> = _isYouTubeModalVisible.asStateFlow()

    private val _isFullScreenPlayerVisible = MutableStateFlow(false)
    val isFullScreenPlayerVisible: StateFlow<Boolean> = _isFullScreenPlayerVisible.asStateFlow()

    // Equalizer & DSP State (Always present and accessible in the app)
    private val _isEqualizerVisible = MutableStateFlow(false)
    val isEqualizerVisible: StateFlow<Boolean> = _isEqualizerVisible.asStateFlow()

    private val _isDspEnabled = MutableStateFlow(true)
    val isDspEnabled: StateFlow<Boolean> = _isDspEnabled.asStateFlow()

    private val _equalizerPreset = MutableStateFlow("Club 503")
    val equalizerPreset: StateFlow<String> = _equalizerPreset.asStateFlow()

    private val _bandLevels = MutableStateFlow(listOf(5, 3, -1, 2, 4))
    val bandLevels: StateFlow<List<Int>> = _bandLevels.asStateFlow()

    private val _bassBoostPercent = MutableStateFlow(45)
    val bassBoostPercent: StateFlow<Int> = _bassBoostPercent.asStateFlow()

    private val _virtualizerPercent = MutableStateFlow(30)
    val virtualizerPercent: StateFlow<Int> = _virtualizerPercent.asStateFlow()

    private var progressJob: Job? = null
    private var playbackManager: MediaPlaybackManager? = null

    init {
        // Initialize Room Database
        val db = AppDatabase.getDatabase(application)
        searchHistoryRepo = SearchHistoryRepository(db.searchHistoryDao())
        searchHistory = searchHistoryRepo.allHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Initialize AudioPlayerEngine for real device audio playback and DSP
        audioPlayerEngine = AudioPlayerEngine(
            context = application,
            onPositionUpdate = { current, dur ->
                _playbackPositionMs.value = current
                if (dur > 0L) {
                    _durationMs.value = dur
                }
            },
            onCompletion = {
                if (_isRepeat.value) {
                    _currentTrack.value?.let { playTrack(it) }
                } else {
                    playNext()
                }
            },
            onError = { err ->
                android.util.Log.e("MusicPlayerState", "Playback error: $err")
            }
        )

        // Escanear audio local en segundo plano
        scanLocalDeviceAudio()

        // Seed initial search history items for immediate user convenience
        viewModelScope.launch {
            searchHistoryRepo.saveQuery("The Weeknd")
            searchHistoryRepo.saveQuery("Post Malone Sunflower")
            searchHistoryRepo.saveQuery("Billie Eilish Happier Than Ever")
            searchHistoryRepo.saveQuery("Imagine Dragons")
        }

        // Initialize Media Session & Headphone / Speaker Hardware Button Manager
        playbackManager = MediaPlaybackManager(
            context = application,
            onPlayRequested = { setPlaying(true) },
            onPauseRequested = { setPlaying(false) },
            onNextRequested = { playNext() },
            onPreviousRequested = { playPrevious() }
        )

        // Connect with background MediaPlaybackService callbacks
        MediaPlaybackService.serviceInstance?.let { service ->
            setupServiceCallbacks(service)
        }

        // Populate curated library matching the user's reference mockup
        val curatedTracks = listOf(
            Track(
                id = "track_blinding_lights",
                title = "Blinding Lights",
                artist = "The Weeknd",
                durationMs = 179000L, // 2:59
                albumArtUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
                genre = "Top Hit",
                youtubeVideoId = "4NRXx6U8ABQ",
                isOnline = true,
                isFavorite = true
            ),
            Track(
                id = "track_save_your_tears",
                title = "Save Your Tears",
                artist = "The Weeknd",
                durationMs = 215000L, // 3:35
                albumArtUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                genre = "Pop",
                youtubeVideoId = "XXYlFuWEuKI",
                isOnline = true
            ),
            Track(
                id = "track_happier_than_ever",
                title = "Happier Than Ever",
                artist = "Billie Eilish",
                durationMs = 297000L, // 4:57
                albumArtUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
                genre = "Alternative",
                youtubeVideoId = "5GJWxDKyk3A",
                isOnline = true
            ),
            Track(
                id = "track_sunflower",
                title = "Sunflower",
                artist = "Post Malone",
                durationMs = 160000L, // 2:40
                albumArtUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=600&auto=format&fit=crop&q=80",
                genre = "Hip-Hop",
                youtubeVideoId = "ApXoWvfEYVU",
                isOnline = true
            ),
            Track(
                id = "track_believer",
                title = "Believer",
                artist = "Imagine Dragons",
                durationMs = 205000L, // 3:25
                albumArtUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=600&auto=format&fit=crop&q=80",
                genre = "Rock",
                youtubeVideoId = "7wtfhZwyrcc",
                isOnline = true
            ),
            Track(
                id = "track_positions",
                title = "Positions",
                artist = "Ariana Grande",
                durationMs = 182000L, // 3:02
                albumArtUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600&auto=format&fit=crop&q=80",
                genre = "R&B",
                youtubeVideoId = "tcYodQoapMg",
                isOnline = true
            ),
            Track(
                id = "track_shivers",
                title = "Shivers",
                artist = "Ed Sheeran",
                durationMs = 208000L, // 3:28
                albumArtUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
                genre = "Pop",
                youtubeVideoId = "Il0S8BoucSA",
                isOnline = true
            ),
            Track(
                id = "track_ghost",
                title = "Ghost",
                artist = "Justin Bieber",
                durationMs = 192000L, // 3:12
                albumArtUrl = "https://images.unsplash.com/photo-1520523839898-507127cd53c3?w=600&auto=format&fit=crop&q=80",
                genre = "Pop",
                youtubeVideoId = "Fp8msa5uYsc",
                isOnline = true
            ),
            Track(
                id = "track_ocean_eyes",
                title = "Ocean Eyes",
                artist = "Billie Eilish",
                durationMs = 196000L,
                albumArtUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600&auto=format&fit=crop&q=80",
                genre = "Soft Vibe"
            ),
            Track(
                id = "track_circles_run",
                title = "Circles Run",
                artist = "Post Malone",
                durationMs = 215000L,
                albumArtUrl = "https://images.unsplash.com/photo-1445985543470-41f30c08f10a?w=600&auto=format&fit=crop&q=80",
                genre = "Fan Fav"
            )
        )

        _playlist.value = curatedTracks
        _currentTrack.value = curatedTracks.first()
    }

    private fun setupServiceCallbacks(service: MediaPlaybackService) {
        service.onPlayAction = { setPlaying(true) }
        service.onPauseAction = { setPlaying(false) }
        service.onNextAction = { playNext() }
        service.onPreviousAction = { playPrevious() }
    }

    private fun startForegroundPlaybackService() {
        try {
            val app = getApplication<Application>()
            val intent = Intent(app, MediaPlaybackService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                app.startForegroundService(intent)
            } else {
                app.startService(intent)
            }
            MediaPlaybackService.serviceInstance?.let { service ->
                setupServiceCallbacks(service)
                service.updateNotification(_currentTrack.value, _isPlaying.value)
            }
        } catch (e: Exception) {
            android.util.Log.w("MusicPlayerState", "Could not start foreground playback service: ${e.message}")
        }
    }

    fun playTrack(track: Track) {
        _currentTrack.value = track
        _durationMs.value = track.durationMs
        _playbackPositionMs.value = 0L
        _isPlaying.value = true

        addToHistory(track)
        startForegroundPlaybackService()
        playbackManager?.updatePlaybackState(true, 0L)

        // Reproducir sonido real a través de MediaPlayer
        audioPlayerEngine?.play(track)

        if (track.isOnline && !track.youtubeVideoId.isNullOrEmpty()) {
            _activeYouTubeVideo.value = YouTubeVideo(
                videoId = track.youtubeVideoId,
                title = track.title,
                channelTitle = track.artist,
                thumbnailUrl = track.albumArtUrl,
                durationFormatted = track.durationFormatted,
                durationMs = track.durationMs
            )
            _isYouTubeModalVisible.value = true
        }
    }

    fun playYouTubeVideo(video: YouTubeVideo) {
        val track = video.toTrack()
        _currentTrack.value = track
        _durationMs.value = video.durationMs
        _playbackPositionMs.value = 0L
        _isPlaying.value = true
        _activeYouTubeVideo.value = video
        _isYouTubeModalVisible.value = true

        addToHistory(track)
        startForegroundPlaybackService()
        playbackManager?.updatePlaybackState(true, 0L)

        // Reproducir sonido real a través de MediaPlayer
        audioPlayerEngine?.play(track)
    }

    fun openYouTubePlayer(video: YouTubeVideo? = null) {
        if (video != null) {
            playYouTubeVideo(video)
        } else {
            _isYouTubeModalVisible.value = true
        }
    }

    fun closeYouTubePlayer() {
        _isYouTubeModalVisible.value = false
    }

    fun showFullScreenPlayer() {
        _isFullScreenPlayerVisible.value = true
    }

    fun hideFullScreenPlayer() {
        _isFullScreenPlayerVisible.value = false
    }

    fun syncYouTubeProgress(currentTimeSec: Float, totalDurationSec: Float, isVideoPlaying: Boolean) {
        if (totalDurationSec > 0f) {
            _durationMs.value = (totalDurationSec * 1000).toLong()
        }
        val pos = (currentTimeSec * 1000).toLong().coerceIn(0L, _durationMs.value)
        _playbackPositionMs.value = pos
        _isPlaying.value = isVideoPlaying
        playbackManager?.updatePlaybackState(isVideoPlaying, pos)
        MediaPlaybackService.serviceInstance?.updateNotification(_currentTrack.value, isVideoPlaying)
    }

    fun togglePlayPause() {
        setPlaying(!_isPlaying.value)
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
        playbackManager?.updatePlaybackState(playing, _playbackPositionMs.value)
        MediaPlaybackService.serviceInstance?.updateNotification(_currentTrack.value, playing)

        if (playing) {
            audioPlayerEngine?.resume()
        } else {
            audioPlayerEngine?.pause()
        }
    }

    fun seekTo(positionMs: Long) {
        _playbackPositionMs.value = positionMs.coerceIn(0L, _durationMs.value)
        audioPlayerEngine?.seekTo(positionMs)
        playbackManager?.updatePlaybackState(_isPlaying.value, _playbackPositionMs.value)
    }

    fun playNext() {
        val list = if (_queue.value.isNotEmpty()) _queue.value else _playlist.value
        if (list.isEmpty()) return

        val currentIndex = list.indexOfFirst { it.id == _currentTrack.value?.id }
        val nextIndex = if (_isShuffle.value) {
            (list.indices).random()
        } else {
            (currentIndex + 1) % list.size
        }
        playTrack(list[nextIndex])
    }

    fun playPrevious() {
        val list = _playlist.value
        if (list.isEmpty()) return

        if (_playbackPositionMs.value > 3000L) {
            seekTo(0L)
            return
        }

        val currentIndex = list.indexOfFirst { it.id == _currentTrack.value?.id }
        val prevIndex = if (currentIndex <= 0) list.size - 1 else currentIndex - 1
        playTrack(list[prevIndex])
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _isRepeat.value = !_isRepeat.value
    }

    fun toggleFavorite(trackId: String) {
        val currentFavs = _favorites.value.toMutableSet()
        if (currentFavs.contains(trackId)) {
            currentFavs.remove(trackId)
        } else {
            currentFavs.add(trackId)
        }
        _favorites.value = currentFavs
    }

    fun isTrackFavorite(trackId: String): Boolean {
        return _favorites.value.contains(trackId)
    }

    fun addToQueue(track: Track) {
        val currentQueue = _queue.value.toMutableList()
        if (!currentQueue.any { it.id == track.id }) {
            currentQueue.add(track)
            _queue.value = currentQueue
        }
    }

    fun removeFromQueue(trackId: String) {
        _queue.value = _queue.value.filterNot { it.id == trackId }
    }

    // Room Database Search History Methods
    fun saveSearchQuery(query: String) {
        viewModelScope.launch {
            searchHistoryRepo.saveQuery(query)
        }
    }

    fun deleteSearchQuery(id: Long) {
        viewModelScope.launch {
            searchHistoryRepo.removeQuery(id)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            searchHistoryRepo.clearAll()
        }
    }

    private fun addToHistory(track: Track) {
        val currentHistory = _history.value.toMutableList()
        currentHistory.removeAll { it.id == track.id }
        currentHistory.add(0, track)
        if (currentHistory.size > 50) {
            _history.value = currentHistory.take(50)
        } else {
            _history.value = currentHistory
        }
    }

    // Equalizer & Audio DSP Control Methods
    fun showEqualizer() {
        _isEqualizerVisible.value = true
    }

    fun hideEqualizer() {
        _isEqualizerVisible.value = false
    }

    fun toggleEqualizer() {
        _isEqualizerVisible.value = !_isEqualizerVisible.value
    }

    fun setDspEnabled(enabled: Boolean) {
        _isDspEnabled.value = enabled
        audioPlayerEngine?.applyDsp(enabled, _bandLevels.value, _bassBoostPercent.value, _virtualizerPercent.value)
    }

    fun setEqualizerPreset(presetName: String, levels: List<Int>) {
        _equalizerPreset.value = presetName
        _bandLevels.value = levels
        audioPlayerEngine?.applyDsp(_isDspEnabled.value, levels, _bassBoostPercent.value, _virtualizerPercent.value)
    }

    fun setBandLevel(index: Int, level: Int) {
        val current = _bandLevels.value.toMutableList()
        if (index in current.indices) {
            current[index] = level
            _bandLevels.value = current
            _equalizerPreset.value = "Personalizado"
            audioPlayerEngine?.applyDsp(_isDspEnabled.value, current, _bassBoostPercent.value, _virtualizerPercent.value)
        }
    }

    fun setBassBoost(percent: Int) {
        _bassBoostPercent.value = percent
        audioPlayerEngine?.applyDsp(_isDspEnabled.value, _bandLevels.value, percent, _virtualizerPercent.value)
    }

    fun setVirtualizer(percent: Int) {
        _virtualizerPercent.value = percent
        audioPlayerEngine?.applyDsp(_isDspEnabled.value, _bandLevels.value, _bassBoostPercent.value, percent)
    }

    fun resetEqualizer() {
        _equalizerPreset.value = "Plano"
        val flat = listOf(0, 0, 0, 0, 0)
        _bandLevels.value = flat
        _bassBoostPercent.value = 0
        _virtualizerPercent.value = 0
        audioPlayerEngine?.applyDsp(_isDspEnabled.value, flat, 0, 0)
    }

    val audioSessionId: Int
        get() = audioPlayerEngine?.currentAudioSessionId ?: 0

    fun scanLocalDeviceAudio() {
        viewModelScope.launch {
            try {
                val songs = AndroidAudioScanner.scanDeviceAudio(getApplication())
                if (songs.isNotEmpty()) {
                    val tracks = songs.map { it.toTrack() }
                    _localDeviceTracks.value = tracks

                    // Actualizar catálogo principal de pistas
                    val current = _playlist.value.toMutableList()
                    tracks.forEach { tr ->
                        if (current.none { it.id == tr.id }) {
                            current.add(tr)
                        }
                    }
                    _playlist.value = current
                }
            } catch (e: Exception) {
                android.util.Log.e("MusicPlayerState", "Error scanning audio: ${e.message}")
            }
        }
    }

    fun syncYouTubeAccount(channelHandle: String) {
        viewModelScope.launch {
            val cleanHandle = if (channelHandle.startsWith("@")) channelHandle else "@$channelHandle"
            val displayName = cleanHandle.removePrefix("@").replaceFirstChar { it.uppercase() }
            _userAccountProfile.value = YouTubeAccountProfile(
                channelTitle = "$displayName Music VIP",
                channelHandle = cleanHandle,
                subscriberCount = "18.5K suscriptores",
                isConnected = true,
                lastSyncTime = "Sincronizado ahora"
            )

            // Buscar y sincronizar playlists de la cuenta de YouTube
            val repo = YouTubeRepository()
            val playlists = repo.searchPlaylists(cleanHandle)
            _userPlaylists.value = playlists
        }
    }

    fun playPlaylist(playlist: YouTubePlaylist) {
        viewModelScope.launch {
            val repo = YouTubeRepository()
            val searchRes = repo.searchVideos(playlist.title)
            val videos = when (searchRes) {
                is YouTubeRepository.SearchResult.Success -> searchRes.videos
                is YouTubeRepository.SearchResult.Error -> searchRes.fallbackVideos
            }
            if (videos.isNotEmpty()) {
                val tracks = videos.map { it.toTrack() }
                _playlist.value = tracks
                playTrack(tracks.first())
            }
        }
    }

    private fun startLocalTicker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive && _isPlaying.value) {
                delay(250)
                val current = _playbackPositionMs.value + 250
                val total = _durationMs.value
                if (current >= total) {
                    if (_isRepeat.value) {
                        _playbackPositionMs.value = 0L
                    } else {
                        playNext()
                    }
                } else {
                    _playbackPositionMs.value = current
                }
            }
        }
    }

    private fun stopLocalTicker() {
        progressJob?.cancel()
        progressJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopLocalTicker()
        audioPlayerEngine?.release()
        audioPlayerEngine = null
        playbackManager?.release()
        playbackManager = null
    }
}
