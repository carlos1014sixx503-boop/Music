package com.example.state

import android.app.Application
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Album
import com.example.data.Artist
import com.example.data.KalinMessage
import com.example.data.KalinMessageProvider
import com.example.data.LibraryTab
import com.example.data.MusicFolder
import com.example.data.RepeatMode
import com.example.data.Song
import com.example.data.lyrics.LyricsRepository
import com.example.data.lyrics.SongLyrics
import com.example.data.repository.ScanState
import com.example.data.repository.SongRepository
import com.example.data.youtube.YouTubePlaybackMode
import com.example.data.youtube.YouTubeRepository
import com.example.data.youtube.YouTubeSearchState
import com.example.data.youtube.YouTubeVideo
import com.example.playback.AudioStats
import com.example.playback.DistritoAudioEngine
import com.example.playback.PlaybackPreferences
import com.example.ui.theme.AppThemeKey
import com.example.ui.theme.DistritoThemeConfig
import com.example.ui.theme.DistritoThemePresets
import com.example.util.AudioPermissionHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PlayerViewMode {
    ARTWORK,
    VINYL_3D,
    LYRICS,
    QUEUE
}

enum class NavDestination(val label: String) {
    HOME("Inicio"),
    LIBRARY("Biblioteca"),
    SEARCH("Buscar"),
    FAVORITES("Favoritos"),
    SETTINGS("Ajustes")
}

class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SongRepository.getInstance(application)

    var currentThemeKey by mutableStateOf(AppThemeKey.LIQUID_GLASS)
        private set

    val currentTheme: DistritoThemeConfig
        get() = DistritoThemePresets.getTheme(currentThemeKey)

    var currentDestination by mutableStateOf(NavDestination.HOME)
        private set

    var selectedLibraryTab by mutableStateOf(LibraryTab.SONGS)
        private set

    var isFullScreenPlayerVisible by mutableStateOf(false)
        private set

    // Estado real de la biblioteca
    var songsList by mutableStateOf<List<Song>>(emptyList())
        private set

    var scanState by mutableStateOf<ScanState>(ScanState.Idle)
        private set

    var isPermissionGranted by mutableStateOf(false)
        private set

    var isNotificationPermissionGranted by mutableStateOf(false)
        private set

    var requestNotificationPermissionAction: (() -> Unit)? = null

    fun updateNotificationPermissionState(granted: Boolean) {
        isNotificationPermissionGranted = granted
    }

    fun requestNotificationPermissionIfNeeded() {
        if (!isNotificationPermissionGranted) {
            requestNotificationPermissionAction?.invoke()
        }
    }

    var lastScanTimestamp by mutableLongStateOf(0L)
        private set

    val lastScanFormatted: String
        get() {
            if (lastScanTimestamp <= 0L) return "Aún no sincronizado"
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(lastScanTimestamp))
        }

    // Listas agrupadas dinámicamente desde canciones REALES
    val albums: List<Album> by derivedStateOf {
        songsList.groupBy { it.album.ifBlank { "Álbum Desconocido" } }
            .map { (albumTitle, songsInAlbum) ->
                val firstWithArt = songsInAlbum.firstOrNull { !it.albumArtUri.isNullOrBlank() }
                Album(
                    id = "album_${albumTitle.hashCode()}",
                    title = albumTitle,
                    artist = songsInAlbum.firstOrNull()?.artist ?: "Varios Artistas",
                    year = songsInAlbum.map { it.year }.filter { it > 1900 }.maxOrNull() ?: 2024,
                    albumArtUri = firstWithArt?.albumArtUri,
                    coverResId = null,
                    songsCount = songsInAlbum.size
                )
            }
            .sortedBy { it.title.lowercase(Locale.getDefault()) }
    }

    val artists: List<Artist> by derivedStateOf {
        songsList.groupBy { it.artist.ifBlank { "Artista Desconocido" } }
            .map { (artistName, songsInArtist) ->
                Artist(
                    id = "artist_${artistName.hashCode()}",
                    name = artistName,
                    songsCount = songsInArtist.size,
                    albumsCount = songsInArtist.map { it.album }.distinct().size
                )
            }
            .sortedBy { it.name.lowercase(Locale.getDefault()) }
    }

    val folders: List<MusicFolder> by derivedStateOf {
        songsList.groupBy {
            val path = it.folderPath.ifBlank { it.folderName }
            if (path.isBlank()) "Almacenamiento" else path
        }
            .map { (path, songsInFolder) ->
                val name = songsInFolder.firstOrNull()?.folderName?.ifBlank { "Música" } ?: "Música"
                MusicFolder(
                    id = "folder_${path.hashCode()}",
                    name = name,
                    path = path,
                    songCount = songsInFolder.size
                )
            }
            .sortedBy { it.name.lowercase(Locale.getDefault()) }
    }

    // Playback state
    var currentSong by mutableStateOf<Song?>(null)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var currentPositionSeconds by mutableIntStateOf(0)
        private set

    var isShuffle by mutableStateOf(false)
        private set

    var repeatMode by mutableStateOf(RepeatMode.ALL)
        private set

    var volume by mutableStateOf(1.0f)
        private set

    var favoriteSongIds by mutableStateOf<Set<String>>(emptySet())
        private set

    var favoriteSongs by mutableStateOf<List<Song>>(emptyList())
        private set

    var favoriteFeedbackMessage by mutableStateOf<String?>(null)
        private set

    private var favoriteFeedbackJob: Job? = null

    fun dismissFavoriteFeedback() {
        favoriteFeedbackJob?.cancel()
        favoriteFeedbackMessage = null
    }

    // Configuración interactiva de Fondo Traslúcido y Difuminado Batman
    var wallpaperBlurDp by mutableIntStateOf(2)
        private set

    var wallpaperAlpha by mutableFloatStateOf(0.85f)
        private set

    var customWallpaperRes by mutableStateOf<Int?>(null)
        private set

    fun updateWallpaperBlur(dp: Int) {
        wallpaperBlurDp = dp
    }

    fun updateWallpaperAlpha(alpha: Float) {
        wallpaperAlpha = alpha
    }

    fun updateCustomWallpaper(resId: Int?) {
        customWallpaperRes = resId
    }

    var recentlyPlayedSongs by mutableStateOf<List<Song>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")
        private set

    // Resultados de búsqueda sobre la biblioteca real
    val searchResultsSongs: List<Song> by derivedStateOf {
        if (searchQuery.isBlank()) emptyList()
        else songsList.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.artist.contains(searchQuery, ignoreCase = true) ||
            it.album.contains(searchQuery, ignoreCase = true) ||
            it.genre.contains(searchQuery, ignoreCase = true)
        }
    }

    val searchResultsAlbums: List<Album> by derivedStateOf {
        if (searchQuery.isBlank()) emptyList()
        else albums.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.artist.contains(searchQuery, ignoreCase = true)
        }
    }

    val searchResultsArtists: List<Artist> by derivedStateOf {
        if (searchQuery.isBlank()) emptyList()
        else artists.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    // Audio effects / Equalizer
    var isBassBoostEnabled by mutableStateOf(true)
        private set

    var bassBoostLevel by mutableIntStateOf(75)
        private set

    var equalizerPreset by mutableStateOf("Club Electrónico 503")
        private set

    // Audio Engine real basado en Jetpack Media3 (ExoPlayer)
    val audioEngine = DistritoAudioEngine.getInstance(application)

    // Preferencias de reproducción persistentes
    private val playbackPreferences = PlaybackPreferences(application)

    // Repositorio oficial para YouTube
    private val youtubeRepository = YouTubeRepository(application)

    var youtubeSearchQuery by mutableStateOf("")
        private set

    var youtubeSearchState by mutableStateOf<YouTubeSearchState>(YouTubeSearchState.Idle)
        private set

    var activeOnlineVideo by mutableStateOf<YouTubeVideo?>(null)
        private set

    var youtubePlaybackMode by mutableStateOf(playbackPreferences.youtubePlaybackMode)
        private set

    var pendingModeSelectionVideo by mutableStateOf<YouTubeVideo?>(null)
        private set

    var isOnlinePlayerVisible by mutableStateOf(false)
        private set

    var isOnlineMiniPlayerActive by mutableStateOf(false)
        private set

    var isYouTubeAudioPlaying by mutableStateOf(true)
        private set

    var playbackErrorMessage by mutableStateOf<String?>(null)
        private set

    // Kalin Welcome Personality State
    var kalinWelcomeMessage by mutableStateOf<KalinMessage?>(null)
        private set

    var isKalinWelcomeVisible by mutableStateOf(false)
        private set

    private var kalinWelcomeJob: Job? = null

    // Modal de Ecualizador DSP
    var isEqualizerModalVisible by mutableStateOf(false)
        private set

    // Modo 3D / Vinilo Giratorio en FullScreenPlayer
    var isPlayer3DMode by mutableStateOf(false)
        private set

    // Vista activa en FullScreenPlayer (Carátula, Vinilo 3D, Letras Sincronizadas, Cola)
    var playerViewMode by mutableStateOf(PlayerViewMode.ARTWORK)
        private set

    // Velocidad de reproducción (0.5x a 2.0x estilo BitChord)
    var playbackSpeed by mutableFloatStateOf(1.0f)
        private set

    // Cola actual observable
    var currentQueue by mutableStateOf<List<Song>>(emptyList())
        private set

    // Modales de funciones BitChord
    var isStatsForNerdsVisible by mutableStateOf(false)
        private set

    var isSleepTimerModalVisible by mutableStateOf(false)
        private set

    var isPlaybackSpeedModalVisible by mutableStateOf(false)
        private set

    var isQueueModalVisible by mutableStateOf(false)
        private set

    // Menú contextual de 3 puntos para cualquier canción
    var songForActionMenu by mutableStateOf<Song?>(null)
        private set

    var isSongActionMenuVisible by mutableStateOf(false)
        private set

    fun openSongActionMenu(song: Song) {
        songForActionMenu = song
        isSongActionMenuVisible = true
    }

    fun dismissSongActionMenu() {
        isSongActionMenuVisible = false
        songForActionMenu = null
    }

    // Filtros de categoría rápida en Home ("Todo", "Energía", "Relajante", "Entrenamiento", "Para concentrarse", "Viaje diario")
    var selectedHomeCategory by mutableStateOf("Todo")
        private set

    fun selectHomeCategory(category: String) {
        selectedHomeCategory = category
    }

    // Letras sincronizadas dinámicas de la canción actual
    val currentLyrics: SongLyrics? by derivedStateOf {
        currentSong?.let { LyricsRepository.getLyricsForSong(it) }
    }

    // Estadísticas de audio para Stats for Nerds
    val audioStats: AudioStats by derivedStateOf {
        audioEngine.getAudioStats()
    }

    // Temporizador de Apagado (Sleep Timer)
    var sleepTimerRemainingSeconds by mutableStateOf<Int?>(null)
        private set

    private var sleepTimerJob: Job? = null

    // Estados de DSP
    var isDspEnabled by mutableStateOf(true)
        private set
    var bandLevels by mutableStateOf(listOf(5, 3, -1, 2, 4))
        private set
    var bassBoostPercent by mutableStateOf(70)
        private set
    var virtualizerPercent by mutableStateOf(50)
        private set

    init {
        checkPermissionState()
        observeDatabase()
        observeAudioEngine()
        triggerKalinWelcome()

        // Sembrar canciones iniciales en Room para permitir usar favoritos inmediatamente
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        // Si ya tenemos permisos al iniciar, escanear o refrescar automáticamente
        if (isPermissionGranted) {
            scanLibrary()
        }
    }

    private fun observeAudioEngine() {
        viewModelScope.launch {
            audioEngine.currentSong.collectLatest { song ->
                if (song != null) {
                    currentSong = song
                }
            }
        }

        viewModelScope.launch {
            audioEngine.isPlaying.collectLatest { playing ->
                isPlaying = playing
            }
        }

        viewModelScope.launch {
            audioEngine.currentPositionMs.collectLatest { posMs ->
                currentPositionSeconds = (posMs / 1000L).toInt()
            }
        }

        viewModelScope.launch {
            audioEngine.isShuffle.collectLatest { shuffle ->
                isShuffle = shuffle
            }
        }

        viewModelScope.launch {
            audioEngine.repeatMode.collectLatest { mode ->
                repeatMode = mode
            }
        }

        viewModelScope.launch {
            audioEngine.volume.collectLatest { vol ->
                volume = vol
            }
        }

        viewModelScope.launch {
            audioEngine.errorMessage.collectLatest { err ->
                playbackErrorMessage = err
            }
        }

        viewModelScope.launch {
            audioEngine.isDspEnabled.collectLatest { enabled ->
                isDspEnabled = enabled
            }
        }

        viewModelScope.launch {
            audioEngine.equalizerPreset.collectLatest { preset ->
                equalizerPreset = preset
            }
        }

        viewModelScope.launch {
            audioEngine.bandLevels.collectLatest { levels ->
                bandLevels = levels
            }
        }

        viewModelScope.launch {
            audioEngine.bassBoostPercent.collectLatest { boost ->
                bassBoostPercent = boost
            }
        }

        viewModelScope.launch {
            audioEngine.virtualizerPercent.collectLatest { virt ->
                virtualizerPercent = virt
            }
        }

        viewModelScope.launch {
            audioEngine.queueFlow.collectLatest { q ->
                currentQueue = q
            }
        }

        viewModelScope.launch {
            audioEngine.playbackSpeed.collectLatest { spd ->
                playbackSpeed = spd
            }
        }
    }

    fun checkPermissionState() {
        val granted = AudioPermissionHelper.hasPermission(getApplication())
        isPermissionGranted = granted
        if (!granted && scanState is ScanState.Idle) {
            scanState = ScanState.PermissionRequired("Se requiere permiso para escanear tu música")
        }
    }

    fun onPermissionResult(granted: Boolean) {
        isPermissionGranted = granted
        if (granted) {
            scanLibrary()
            requestNotificationPermissionIfNeeded()
        } else {
            scanState = ScanState.PermissionRequired("Permiso denegado. No se puede acceder a la música local.")
        }
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            repository.allSongs.collectLatest { list ->
                songsList = list
                audioEngine.syncQueue(list)
                if (currentSong == null && list.isNotEmpty()) {
                    currentSong = list.first()
                } else if (currentSong != null && list.none { it.id == currentSong?.id }) {
                    currentSong = list.firstOrNull()
                }
            }
        }

        viewModelScope.launch {
            repository.favoriteSongs.collectLatest { favorites ->
                favoriteSongs = favorites
                favoriteSongIds = favorites.map { it.id }.toSet()
            }
        }

        viewModelScope.launch {
            repository.recentlyAddedSongs.collectLatest { recent ->
                if (recentlyPlayedSongs.isEmpty()) {
                    recentlyPlayedSongs = recent.take(6)
                }
            }
        }
    }

    /**
     * Escanea el almacenamiento local del dispositivo Android y actualiza Room.
     */
    fun scanLibrary() {
        if (!AudioPermissionHelper.hasPermission(getApplication())) {
            scanState = ScanState.PermissionRequired("Se requiere permiso para escanear tu música")
            return
        }

        scanState = ScanState.Scanning
        viewModelScope.launch {
            val result = repository.scanDeviceLibrary(getApplication())
            scanState = result
            if (result is ScanState.Success) {
                lastScanTimestamp = result.timestamp
            }
        }
    }

    /**
     * Muestra la bienvenida de Kalin seleccionando un mensaje aleatorio.
     */
    fun triggerKalinWelcome() {
        val message = KalinMessageProvider.getRandomMessage(kalinWelcomeMessage?.id)
        kalinWelcomeMessage = message
        isKalinWelcomeVisible = true

        kalinWelcomeJob?.cancel()
        kalinWelcomeJob = viewModelScope.launch {
            delay(5500L)
            isKalinWelcomeVisible = false
        }
    }

    fun dismissKalinWelcome() {
        kalinWelcomeJob?.cancel()
        isKalinWelcomeVisible = false
    }

    fun playSong(song: Song, queue: List<Song> = songsList) {
        requestNotificationPermissionIfNeeded()
        audioEngine.playSong(song, queue)

        viewModelScope.launch {
            repository.incrementPlayCount(song.id)
        }

        val updated = recentlyPlayedSongs.toMutableList()
        updated.remove(song)
        updated.add(0, song)
        recentlyPlayedSongs = updated.take(10)
    }

    fun togglePlayPause() {
        audioEngine.togglePlayPause()
    }

    fun playNext() {
        audioEngine.playNext()
    }

    fun playPrevious() {
        audioEngine.playPrevious()
    }

    fun seekTo(seconds: Int) {
        audioEngine.seekToSeconds(seconds)
    }

    fun seekForward(seconds: Int = 10) {
        audioEngine.seekForward(seconds)
    }

    fun seekBackward(seconds: Int = 10) {
        audioEngine.seekBackward(seconds)
    }

    fun toggleShuffle() {
        audioEngine.toggleShuffle()
    }

    fun toggleRepeat() {
        audioEngine.toggleRepeat()
    }

    fun updateVolume(vol: Float) {
        audioEngine.setVolume(vol)
    }

    fun dismissPlaybackError() {
        audioEngine.dismissErrorMessage()
        playbackErrorMessage = null
    }

    fun toggleFavorite(songId: String) {
        val currentlyFav = isSongFavorite(songId)
        val newFav = !currentlyFav
        val updated = favoriteSongIds.toMutableSet()
        if (newFav) updated.add(songId) else updated.remove(songId)
        favoriteSongIds = updated

        // Notificación de estado temporal
        val targetSong = songsList.firstOrNull { it.id == songId } ?: currentSong
        val songTitle = targetSong?.title ?: "Canción"
        favoriteFeedbackMessage = if (newFav) {
            "\"$songTitle\" guardada en Favoritos ❤️"
        } else {
            "\"$songTitle\" eliminada de Favoritos"
        }
        favoriteFeedbackJob?.cancel()
        favoriteFeedbackJob = viewModelScope.launch {
            delay(2500L)
            favoriteFeedbackMessage = null
        }

        viewModelScope.launch {
            repository.toggleFavorite(songId, newFav)
        }
    }

    fun playFavorites(shuffle: Boolean = false) {
        val favs = favoriteSongs.ifEmpty { songsList.filter { isSongFavorite(it.id) } }
        if (favs.isNotEmpty()) {
            if (shuffle) {
                if (!isShuffle) toggleShuffle()
                val randomSong = favs.random()
                playSong(randomSong, favs)
            } else {
                playSong(favs.first(), favs)
            }
        }
    }

    fun isSongFavorite(songId: String): Boolean {
        return favoriteSongIds.contains(songId)
    }

    fun setTheme(themeKey: AppThemeKey) {
        currentThemeKey = themeKey
    }

    fun setDestination(destination: NavDestination) {
        currentDestination = destination
    }

    fun setLibraryTab(tab: LibraryTab) {
        selectedLibraryTab = tab
    }

    fun showFullScreenPlayer(show: Boolean) {
        isFullScreenPlayerVisible = show
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun toggleBassBoost() {
        isBassBoostEnabled = !isBassBoostEnabled
    }

    fun updateBassBoostLevel(level: Int) {
        bassBoostLevel = level.coerceIn(0, 100)
    }

    fun setEqualizer(preset: String) {
        equalizerPreset = preset
        audioEngine.setEqualizerPreset(preset, bandLevels)
    }

    // --- YouTube Online Integration ---

    fun updateYouTubeSearchQuery(query: String) {
        youtubeSearchQuery = query
    }

    fun searchYouTube(customQuery: String? = null) {
        val queryToSearch = (customQuery ?: youtubeSearchQuery).trim()
        if (customQuery != null) {
            youtubeSearchQuery = customQuery
        }
        if (queryToSearch.isBlank()) {
            youtubeSearchState = YouTubeSearchState.Idle
            return
        }

        viewModelScope.launch {
            youtubeSearchState = YouTubeSearchState.Loading
            val result = youtubeRepository.search(queryToSearch)
            youtubeSearchState = result
        }
    }

    fun retryYouTubeSearch() {
        searchYouTube()
    }

    fun selectYouTubeVideo(video: YouTubeVideo) {
        // Iniciar directamente con el modo activo seleccionado, sin ventanas emergentes
        startYouTubePlayback(video, youtubePlaybackMode)
    }

    fun dismissModeSelection() {
        pendingModeSelectionVideo = null
    }

    fun startYouTubePlayback(video: YouTubeVideo, mode: YouTubePlaybackMode) {
        // Pausar reproducción local para evitar mezclar audios, manteniendo intacta la cola local
        if (isPlaying) {
            audioEngine.pause()
        }
        activeOnlineVideo = video
        youtubePlaybackMode = mode
        playbackPreferences.youtubePlaybackMode = mode
        pendingModeSelectionVideo = null
        isOnlinePlayerVisible = true
        isOnlineMiniPlayerActive = false
        isYouTubeAudioPlaying = true
    }

    fun playYouTubeVideo(video: YouTubeVideo) {
        startYouTubePlayback(video, youtubePlaybackMode)
    }

    fun setPlaybackMode(mode: YouTubePlaybackMode) {
        youtubePlaybackMode = mode
        playbackPreferences.youtubePlaybackMode = mode
    }

    fun minimizeOnlinePlayer() {
        isOnlinePlayerVisible = false
        isOnlineMiniPlayerActive = true
    }

    fun restoreOnlinePlayer() {
        isOnlinePlayerVisible = true
        isOnlineMiniPlayerActive = false
    }

    fun stopOnlinePlayback() {
        isOnlinePlayerVisible = false
        isOnlineMiniPlayerActive = false
        activeOnlineVideo = null
        isYouTubeAudioPlaying = false
    }

    fun toggleYouTubeAudioPlayback() {
        isYouTubeAudioPlaying = !isYouTubeAudioPlaying
    }

    fun updateYouTubeAudioPlaying(playing: Boolean) {
        isYouTubeAudioPlaying = playing
    }

    fun playNextYouTubeVideo() {
        val currentList = (youtubeSearchState as? YouTubeSearchState.Success)?.videos ?: return
        val currentIndex = currentList.indexOfFirst { it.id == activeOnlineVideo?.id }
        if (currentIndex != -1 && currentIndex < currentList.size - 1) {
            startYouTubePlayback(currentList[currentIndex + 1], youtubePlaybackMode)
        } else if (currentList.isNotEmpty()) {
            startYouTubePlayback(currentList.first(), youtubePlaybackMode)
        }
    }

    fun playPreviousYouTubeVideo() {
        val currentList = (youtubeSearchState as? YouTubeSearchState.Success)?.videos ?: return
        val currentIndex = currentList.indexOfFirst { it.id == activeOnlineVideo?.id }
        if (currentIndex > 0) {
            startYouTubePlayback(currentList[currentIndex - 1], youtubePlaybackMode)
        } else if (currentList.isNotEmpty()) {
            startYouTubePlayback(currentList.last(), youtubePlaybackMode)
        }
    }

    fun dismissOnlinePlayer() {
        stopOnlinePlayback()
    }

    // Métodos de Ecualizador DSP
    fun showEqualizerModal(show: Boolean) {
        isEqualizerModalVisible = show
    }

    fun enableDsp(enabled: Boolean) {
        audioEngine.setDspEnabled(enabled)
    }

    fun setBandLevel(bandIndex: Int, levelDb: Int) {
        audioEngine.setBandLevel(bandIndex, levelDb)
    }

    fun setEqualizerPreset(presetName: String, levels: List<Int>) {
        audioEngine.setEqualizerPreset(presetName, levels)
    }

    fun setBassBoost(percent: Int) {
        audioEngine.setBassBoost(percent)
    }

    fun setVirtualizer(percent: Int) {
        audioEngine.setVirtualizer(percent)
    }

    fun resetEqualizer() {
        audioEngine.resetEqualizer()
    }

    // Modo 3D / Vinilo Giratorio
    fun togglePlayer3DMode() {
        isPlayer3DMode = !isPlayer3DMode
        playerViewMode = if (isPlayer3DMode) PlayerViewMode.VINYL_3D else PlayerViewMode.ARTWORK
    }

    fun switchPlayer3DMode(enabled: Boolean) {
        isPlayer3DMode = enabled
        playerViewMode = if (enabled) PlayerViewMode.VINYL_3D else PlayerViewMode.ARTWORK
    }

    fun switchPlayerViewMode(mode: PlayerViewMode) {
        playerViewMode = mode
        isPlayer3DMode = (mode == PlayerViewMode.VINYL_3D)
    }

    // Control de modales inspirados en BitChord
    fun showStatsForNerds(show: Boolean) {
        isStatsForNerdsVisible = show
    }

    fun showSleepTimerModal(show: Boolean) {
        isSleepTimerModalVisible = show
    }

    fun showPlaybackSpeedModal(show: Boolean) {
        isPlaybackSpeedModalVisible = show
    }

    fun showQueueModal(show: Boolean) {
        isQueueModalVisible = show
    }

    fun changePlaybackSpeed(speed: Float) {
        audioEngine.setPlaybackSpeed(speed)
    }

    // Gestión interactiva de cola (Up Next Queue)
    fun addToQueue(song: Song) {
        audioEngine.addToQueue(song)
    }

    fun playNextInQueue(song: Song) {
        audioEngine.playNextInQueue(song)
    }

    fun removeQueueItem(index: Int) {
        audioEngine.removeQueueItem(index)
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        audioEngine.moveQueueItem(fromIndex, toIndex)
    }

    fun playQueueIndex(index: Int) {
        audioEngine.playQueueIndex(index)
    }

    fun clearQueueExceptCurrent() {
        audioEngine.clearQueueExceptCurrent()
    }

    // Temporizador de Apagado (Sleep Timer con Desvanecimiento Gradual)
    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes == 0) {
            sleepTimerRemainingSeconds = null
            audioEngine.setVolume(1.0f)
            return
        }

        // Caso especial: -1 = Fin de canción actual
        if (minutes == -1) {
            val remainingSec = ((currentSong?.durationSeconds ?: 0) - currentPositionSeconds).coerceAtLeast(1)
            sleepTimerRemainingSeconds = remainingSec
            sleepTimerJob = viewModelScope.launch {
                var remaining = remainingSec
                val originalVol = volume
                while (remaining > 0) {
                    delay(1000L)
                    remaining--
                    sleepTimerRemainingSeconds = remaining
                    if (remaining in 1..10) {
                        val factor = (remaining.toFloat() / 10f).coerceIn(0.1f, 1.0f)
                        audioEngine.setVolume(originalVol * factor)
                    }
                }
                audioEngine.pause()
                audioEngine.setVolume(originalVol)
                sleepTimerRemainingSeconds = null
            }
            return
        }

        val totalSeconds = minutes * 60
        sleepTimerRemainingSeconds = totalSeconds

        sleepTimerJob = viewModelScope.launch {
            var remaining = totalSeconds
            val originalVol = volume
            while (remaining > 0) {
                delay(1000L)
                remaining--
                sleepTimerRemainingSeconds = remaining
                // Desvanecimiento suave en los últimos 10 segundos
                if (remaining in 1..10) {
                    val factor = (remaining.toFloat() / 10f).coerceIn(0.1f, 1.0f)
                    audioEngine.setVolume(originalVol * factor)
                }
            }
            audioEngine.pause()
            audioEngine.setVolume(originalVol)
            sleepTimerRemainingSeconds = null
        }
    }
}
