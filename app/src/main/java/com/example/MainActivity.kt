package com.example

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.state.MusicPlayerViewModel
import com.example.state.NavDestination
import com.example.ui.components.DistritoBottomNavigationBar
import com.example.ui.components.EqualizerDspModal
import com.example.ui.components.FullScreenPlayer
import com.example.ui.components.KalinWelcomeBanner
import com.example.ui.components.LiquidGlassSpheresBackground
import com.example.ui.components.LiquidGlassSpheresPalette
import com.example.ui.components.MiniPlayer
import com.example.ui.components.PlayQueueSheet
import com.example.ui.components.PlaybackSpeedDialog
import com.example.ui.components.SleepTimerDialog
import com.example.ui.components.SongActionMenuModal
import com.example.ui.components.StatsForNerdsModal
import com.example.ui.components.YouTubeOnlineMiniBar
import com.example.ui.components.YouTubePlayerModal
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DistritoMusicTheme
import com.example.ui.theme.LocalDistritoTheme
import com.example.util.AudioPermissionHelper

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val playerViewModel: MusicPlayerViewModel = viewModel()
      DistritoMusicTheme(themeConfig = playerViewModel.currentTheme) {
        DistritoMusicApp(viewModel = playerViewModel)
      }
    }
  }
}

@Composable
fun DistritoMusicApp(viewModel: MusicPlayerViewModel) {
  val theme = LocalDistritoTheme.current
  val context = LocalContext.current

  // Permission Launcher for Audio Library
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    viewModel.onPermissionResult(isGranted)
  }

  // Permission Launcher for Notifications (Android 13+)
  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    Log.d("DistritoMediaPlayback", "Resultado de solicitud POST_NOTIFICATIONS: concedido=$isGranted")
    viewModel.updateNotificationPermissionState(isGranted)
  }

  val requestAudioPermission: () -> Unit = {
    permissionLauncher.launch(AudioPermissionHelper.requiredPermission)
  }

  // Sincronizar estado y disparador del permiso de notificación con el ViewModel
  LaunchedEffect(viewModel) {
    val initialGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
    } else {
      true
    }
    Log.d(
      "DistritoMediaPlayback",
      "Estado inicial de POST_NOTIFICATIONS: concedido=$initialGranted (SDK ${Build.VERSION.SDK_INT})"
    )
    viewModel.updateNotificationPermissionState(initialGranted)

    viewModel.requestNotificationPermissionAction = {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val currentGranted = ContextCompat.checkSelfPermission(
          context,
          android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.updateNotificationPermissionState(currentGranted)
        if (!currentGranted) {
          Log.d("DistritoMediaPlayback", "Solicitando permiso POST_NOTIFICATIONS al usuario...")
          notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
      }
    }
  }

  // Check and request permission on first launch if not already granted
  LaunchedEffect(Unit) {
    viewModel.checkPermissionState()
    if (!viewModel.isPermissionGranted) {
      permissionLauncher.launch(AudioPermissionHelper.requiredPermission)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val notifGranted = ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
      if (!notifGranted) {
        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }

  val currentDest = viewModel.currentDestination
  val wallpaperRes = viewModel.customWallpaperRes ?: when (currentDest) {
    NavDestination.HOME -> R.drawable.bg_batman_cave
    NavDestination.LIBRARY -> R.drawable.bg_batman_smoke
    NavDestination.FAVORITES -> R.drawable.bg_batman_crimson
    NavDestination.SEARCH -> R.drawable.bg_batman_smoke
    NavDestination.SETTINGS -> R.drawable.bg_batman_cave
  }

  val primarySphere = when (currentDest) {
    NavDestination.FAVORITES -> LiquidGlassSpheresPalette.CrimsonRed
    NavDestination.LIBRARY -> LiquidGlassSpheresPalette.NeonViolet
    NavDestination.SEARCH -> LiquidGlassSpheresPalette.SkyAzure
    else -> LiquidGlassSpheresPalette.ElectricCyan
  }

  val secondarySphere = when (currentDest) {
    NavDestination.FAVORITES -> LiquidGlassSpheresPalette.RadiantMagenta
    NavDestination.LIBRARY -> LiquidGlassSpheresPalette.SkyAzure
    NavDestination.SEARCH -> LiquidGlassSpheresPalette.NeonViolet
    else -> LiquidGlassSpheresPalette.NeonViolet
  }

  val tertiarySphere = when (currentDest) {
    NavDestination.FAVORITES -> LiquidGlassSpheresPalette.NeonViolet
    NavDestination.LIBRARY -> LiquidGlassSpheresPalette.RadiantMagenta
    else -> LiquidGlassSpheresPalette.RadiantMagenta
  }

  LiquidGlassSpheresBackground(
    modifier = Modifier.fillMaxSize(),
    backgroundImageRes = wallpaperRes,
    backgroundAlpha = viewModel.wallpaperAlpha,
    backgroundBlurRadius = viewModel.wallpaperBlurDp.dp,
    primarySphereColor = primarySphere,
    secondarySphereColor = secondarySphere,
    tertiarySphereColor = tertiarySphere,
    quaternarySphereColor = LiquidGlassSpheresPalette.SkyAzure
  ) {
    // Screen Content
    Box(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
    ) {
      AnimatedContent(
        targetState = viewModel.currentDestination,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ScreenTransition"
      ) { destination ->
        when (destination) {
          NavDestination.HOME -> HomeScreen(
            viewModel = viewModel,
            onRequestPermission = requestAudioPermission
          )
          NavDestination.LIBRARY -> LibraryScreen(
            viewModel = viewModel,
            onRequestPermission = requestAudioPermission
          )
          NavDestination.SEARCH -> SearchScreen(viewModel = viewModel)
          NavDestination.FAVORITES -> FavoritesScreen(viewModel = viewModel)
          NavDestination.SETTINGS -> SettingsScreen(
            viewModel = viewModel,
            onRequestPermission = requestAudioPermission
          )
        }
      }
    }

    // Floating Bottom Controls: Mini Player + Bottom Nav Bar
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
    ) {
      if (viewModel.isOnlineMiniPlayerActive && viewModel.activeOnlineVideo != null) {
        YouTubeOnlineMiniBar(
          video = viewModel.activeOnlineVideo,
          mode = viewModel.youtubePlaybackMode,
          visible = viewModel.isOnlineMiniPlayerActive,
          isPlaying = viewModel.isYouTubeAudioPlaying,
          onTogglePlayPause = { viewModel.toggleYouTubeAudioPlayback() },
          onClickBar = { viewModel.restoreOnlinePlayer() },
          onClose = { viewModel.stopOnlinePlayback() }
        )
      } else {
        MiniPlayer(
          currentSong = viewModel.currentSong,
          isPlaying = viewModel.isPlaying,
          currentPositionSeconds = viewModel.currentPositionSeconds,
          onTogglePlayPause = { viewModel.togglePlayPause() },
          onPlayNext = { viewModel.playNext() },
          onOpenFullScreen = { viewModel.showFullScreenPlayer(true) },
          onPlayPrevious = { viewModel.playPrevious() },
          isFavorite = viewModel.currentSong?.let { viewModel.isSongFavorite(it.id) } ?: false,
          onToggleFavorite = { viewModel.currentSong?.let { viewModel.toggleFavorite(it.id) } }
        )
      }

      DistritoBottomNavigationBar(
        currentDestination = viewModel.currentDestination,
        onDestinationSelected = { viewModel.setDestination(it) }
      )
    }

    // Floating Top Welcome Greeting from Kalin (Preserved completely)
    KalinWelcomeBanner(
      visible = viewModel.isKalinWelcomeVisible,
      message = viewModel.kalinWelcomeMessage,
      onDismiss = { viewModel.dismissKalinWelcome() },
      modifier = Modifier.align(Alignment.TopCenter)
    )

    // Floating Error Banner for unplayable/missing files (Requirement 14)
    AnimatedVisibility(
      visible = viewModel.playbackErrorMessage != null,
      enter = slideInVertically { -it } + fadeIn(),
      exit = slideOutVertically { -it } + fadeOut(),
      modifier = Modifier
        .align(Alignment.TopCenter)
        .statusBarsPadding()
        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
      val errMsg = viewModel.playbackErrorMessage ?: ""
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(12.dp, RoundedCornerShape(16.dp))
          .clip(RoundedCornerShape(16.dp))
          .background(Color(0xFF221515))
          .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
          .padding(horizontal = 14.dp, vertical = 10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.WarningAmber,
              contentDescription = null,
              tint = Color(0xFFFF5252),
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = errMsg,
              color = Color(0xFFFFCCCC),
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          }

          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Cerrar",
            tint = Color(0xFFAAAAAA),
            modifier = Modifier
              .size(20.dp)
              .clickable { viewModel.dismissPlaybackError() }
          )
        }
      }
    }

    // Floating Favorite Notification Banner (Feedback instantáneo de guardado en Room)
    AnimatedVisibility(
      visible = viewModel.favoriteFeedbackMessage != null,
      enter = slideInVertically { -it } + fadeIn(),
      exit = slideOutVertically { -it } + fadeOut(),
      modifier = Modifier
        .align(Alignment.TopCenter)
        .statusBarsPadding()
        .padding(top = 16.dp, start = 20.dp, end = 20.dp)
    ) {
      val message = viewModel.favoriteFeedbackMessage ?: ""
      val isHeart = message.contains("guardada") || message.contains("❤️")
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(16.dp, RoundedCornerShape(20.dp), ambientColor = if (isHeart) Color(0xFFFF3366).copy(alpha = 0.5f) else Color.Black)
          .clip(RoundedCornerShape(20.dp))
          .background(Color(0xFF1B1B24).copy(alpha = 0.95f))
          .border(
            1.2.dp,
            if (isHeart) Color(0xFFFF3366).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.2f),
            RoundedCornerShape(20.dp)
          )
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (isHeart) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
              contentDescription = null,
              tint = if (isHeart) Color(0xFFFF3366) else Color.White.copy(alpha = 0.7f),
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = message,
              color = Color.White,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 2
            )
          }

          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Cerrar",
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier
              .size(18.dp)
              .clickable { viewModel.dismissFavoriteFeedback() }
          )
        }
      }
    }

    // Full Screen Player Modal / Sheet
    FullScreenPlayer(
      visible = viewModel.isFullScreenPlayerVisible,
      currentSong = viewModel.currentSong,
      isPlaying = viewModel.isPlaying,
      currentPositionSeconds = viewModel.currentPositionSeconds,
      isShuffle = viewModel.isShuffle,
      repeatMode = viewModel.repeatMode,
      isFavorite = viewModel.currentSong?.let { viewModel.isSongFavorite(it.id) } ?: false,
      onDismiss = { viewModel.showFullScreenPlayer(false) },
      onTogglePlayPause = { viewModel.togglePlayPause() },
      onPlayNext = { viewModel.playNext() },
      onPlayPrevious = { viewModel.playPrevious() },
      onSeekTo = { viewModel.seekTo(it) },
      onSeekForward = { viewModel.seekForward() },
      onSeekBackward = { viewModel.seekBackward() },
      onToggleShuffle = { viewModel.toggleShuffle() },
      onToggleRepeat = { viewModel.toggleRepeat() },
      onToggleFavorite = { viewModel.currentSong?.let { viewModel.toggleFavorite(it.id) } },
      onOpenEqualizer = { viewModel.showEqualizerModal(true) },
      isPlayer3DMode = viewModel.isPlayer3DMode,
      onTogglePlayer3DMode = { viewModel.togglePlayer3DMode() },
      sleepTimerRemainingSeconds = viewModel.sleepTimerRemainingSeconds,
      onSetSleepTimer = { viewModel.setSleepTimer(it) },
      playerViewMode = viewModel.playerViewMode,
      onSetPlayerViewMode = { viewModel.switchPlayerViewMode(it) },
      lyrics = viewModel.currentLyrics,
      queue = viewModel.currentQueue,
      playbackSpeed = viewModel.playbackSpeed,
      onOpenSpeedModal = { viewModel.showPlaybackSpeedModal(true) },
      onOpenSleepTimerModal = { viewModel.showSleepTimerModal(true) },
      onOpenStatsForNerds = { viewModel.showStatsForNerds(true) },
      onOpenQueueModal = { viewModel.showQueueModal(true) }
    )

    // Modal de Ecualizador DSP 503 (5 Bandas, Graves, Virtualizador)
    EqualizerDspModal(
      visible = viewModel.isEqualizerModalVisible,
      isDspEnabled = viewModel.isDspEnabled,
      equalizerPreset = viewModel.equalizerPreset,
      bandLevels = viewModel.bandLevels,
      bassBoostPercent = viewModel.bassBoostPercent,
      virtualizerPercent = viewModel.virtualizerPercent,
      onToggleDsp = { viewModel.enableDsp(it) },
      onSetPreset = { name, levels -> viewModel.setEqualizerPreset(name, levels) },
      onBandChange = { band, level -> viewModel.setBandLevel(band, level) },
      onBassBoostChange = { viewModel.setBassBoost(it) },
      onVirtualizerChange = { viewModel.setVirtualizer(it) },
      onReset = { viewModel.resetEqualizer() },
      onDismiss = { viewModel.showEqualizerModal(false) }
    )

    // Modal Stats for Nerds 🤓 (BitChord)
    StatsForNerdsModal(
      visible = viewModel.isStatsForNerdsVisible,
      stats = viewModel.audioStats,
      onDismiss = { viewModel.showStatsForNerds(false) }
    )

    // Modal Cola de Reproducción "A continuación" (BitChord)
    PlayQueueSheet(
      visible = viewModel.isQueueModalVisible,
      currentSong = viewModel.currentSong,
      queue = viewModel.currentQueue,
      isPlaying = viewModel.isPlaying,
      onPlayQueueIndex = { viewModel.playQueueIndex(it) },
      onMoveQueueItem = { from, to -> viewModel.moveQueueItem(from, to) },
      onRemoveQueueItem = { viewModel.removeQueueItem(it) },
      onClearQueue = { viewModel.clearQueueExceptCurrent() },
      onDismiss = { viewModel.showQueueModal(false) }
    )

    // Selector de Velocidad de Reproducción (BitChord)
    PlaybackSpeedDialog(
      visible = viewModel.isPlaybackSpeedModalVisible,
      currentSpeed = viewModel.playbackSpeed,
      onSpeedChange = { viewModel.changePlaybackSpeed(it) },
      onDismiss = { viewModel.showPlaybackSpeedModal(false) }
    )

    // Temporizador de Apagado con Desvanecimiento Suave (BitChord)
    SleepTimerDialog(
      visible = viewModel.isSleepTimerModalVisible,
      remainingSeconds = viewModel.sleepTimerRemainingSeconds,
      onSetTimer = { viewModel.setSleepTimer(it) },
      onDismiss = { viewModel.showSleepTimerModal(false) }
    )

    // YouTube Online Player Modal (Video & Solo Audio modes)
    YouTubePlayerModal(
      video = viewModel.activeOnlineVideo,
      mode = viewModel.youtubePlaybackMode,
      visible = viewModel.isOnlinePlayerVisible,
      isPlaying = viewModel.isYouTubeAudioPlaying,
      onModeChange = { viewModel.setPlaybackMode(it) },
      onTogglePlayPause = { viewModel.toggleYouTubeAudioPlayback() },
      onPlayPrevious = { viewModel.playPreviousYouTubeVideo() },
      onPlayNext = { viewModel.playNextYouTubeVideo() },
      onMinimize = { viewModel.minimizeOnlinePlayer() },
      onDismiss = { viewModel.stopOnlinePlayback() }
    )

    // Menú Contextual de Opciones de Canción (3 Puntos ⋮)
    SongActionMenuModal(
      visible = viewModel.isSongActionMenuVisible,
      song = viewModel.songForActionMenu,
      isFavorite = viewModel.songForActionMenu?.let { viewModel.isSongFavorite(it.id) } ?: false,
      onDismiss = { viewModel.dismissSongActionMenu() },
      onPlayNow = { viewModel.playSong(it) },
      onPlayNext = { viewModel.playNextInQueue(it) },
      onAddToQueue = { viewModel.addToQueue(it) },
      onToggleFavorite = { viewModel.toggleFavorite(it) },
      onOpenStatsForNerds = { viewModel.showStatsForNerds(true) },
      onOpenEqualizer = { viewModel.showEqualizerModal(true) },
      onOpenSleepTimer = { viewModel.showSleepTimerModal(true) }
    )
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}

