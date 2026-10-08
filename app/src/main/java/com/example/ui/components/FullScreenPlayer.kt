package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimationRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.RepeatMode
import com.example.data.Song
import com.example.data.lyrics.SongLyrics
import com.example.state.PlayerViewMode
import com.example.ui.theme.LocalDistritoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenPlayer(
    visible: Boolean,
    currentSong: Song?,
    isPlaying: Boolean,
    currentPositionSeconds: Int,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    onSeekTo: (Int) -> Unit,
    onSeekForward: () -> Unit = {},
    onSeekBackward: () -> Unit = {},
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenEqualizer: () -> Unit = {},
    isPlayer3DMode: Boolean = false,
    onTogglePlayer3DMode: () -> Unit = {},
    sleepTimerRemainingSeconds: Int? = null,
    onSetSleepTimer: (Int) -> Unit = {},
    playerViewMode: PlayerViewMode = if (isPlayer3DMode) PlayerViewMode.VINYL_3D else PlayerViewMode.ARTWORK,
    onSetPlayerViewMode: (PlayerViewMode) -> Unit = {},
    lyrics: SongLyrics? = null,
    queue: List<Song> = emptyList(),
    playbackSpeed: Float = 1.0f,
    onOpenSpeedModal: () -> Unit = {},
    onOpenSleepTimerModal: () -> Unit = {},
    onOpenStatsForNerds: () -> Unit = {},
    onOpenQueueModal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val theme = LocalDistritoTheme.current

    AnimatedVisibility(
        visible = visible && currentSong != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        if (currentSong == null) return@AnimatedVisibility

        var isScrubbing by remember { mutableStateOf(false) }
        var scrubPosition by remember { mutableFloatStateOf(0f) }

        val totalDuration = currentSong.durationSeconds.toFloat()
        val currentProgress = if (isScrubbing) scrubPosition else currentPositionSeconds.toFloat()

        val remainingSeconds = (currentSong.durationSeconds - currentPositionSeconds).coerceAtLeast(0)
        val remainingFormatted = "-%d:%02d".format(remainingSeconds / 60, remainingSeconds % 60)
        val currentFormatted = "%d:%02d".format(
            (if (isScrubbing) scrubPosition.toInt() else currentPositionSeconds) / 60,
            (if (isScrubbing) scrubPosition.toInt() else currentPositionSeconds) % 60
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF06080E))
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("fullscreen_player")
        ) {
            // Fondo difuminado y traslúcido cinematográfico Batman
            val playerBgRes = if (isFavorite) R.drawable.bg_batman_crimson else R.drawable.bg_batman_cave
            Image(
                painter = painterResource(id = playerBgRes),
                contentDescription = "Fondo Reproductor Batman",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(2.dp)
                    .alpha(0.82f)
            )

            // Velo de degradado suave para mantener óptima legibilidad
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF04060A).copy(alpha = 0.28f),
                                Color(0xFF080B12).copy(alpha = 0.15f),
                                Color(0xFF04060A).copy(alpha = 0.50f)
                            )
                        )
                    )
            )

            // Ambient background glow effect
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(320.dp)
                    .padding(top = 40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                theme.primaryAccent.copy(alpha = 0.25f),
                                theme.secondaryAccent.copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(theme.surfaceColor)
                            .border(1.dp, theme.surfaceBorderColor, CircleShape)
                            .testTag("dismiss_player_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Cerrar reproductor",
                            tint = theme.textPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(theme.surfaceColor.copy(alpha = 0.7f))
                            .border(1.dp, theme.surfaceBorderColor, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .border(1.dp, theme.primaryAccent, CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.distrito_logo),
                                contentDescription = "Distrito Music 503",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "DISTRITO MUSIC 503",
                                color = theme.primaryAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = currentSong.album,
                                color = theme.textSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Botón Stats for Nerds 🤓 (BitChord)
                        IconButton(
                            onClick = onOpenStatsForNerds,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(theme.surfaceColor)
                                .border(1.dp, theme.surfaceBorderColor, CircleShape)
                                .testTag("open_stats_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeveloperBoard,
                                contentDescription = "Stats for Nerds",
                                tint = theme.textSecondary,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // Botón Velocidad de reproducción (BitChord)
                        IconButton(
                            onClick = onOpenSpeedModal,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (playbackSpeed != 1.0f) theme.primaryAccent.copy(alpha = 0.2f) else theme.surfaceColor)
                                .border(1.dp, if (playbackSpeed != 1.0f) theme.primaryAccent else theme.surfaceBorderColor, CircleShape)
                                .testTag("open_speed_button")
                        ) {
                            if (playbackSpeed != 1.0f) {
                                Text(
                                    text = "%.1fx".format(playbackSpeed),
                                    color = theme.primaryAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Velocidad de reproducción",
                                    tint = theme.textSecondary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }

                        // Botón Temporizador de apagado (BitChord)
                        IconButton(
                            onClick = onOpenSleepTimerModal,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (sleepTimerRemainingSeconds != null) theme.secondaryAccent.copy(alpha = 0.2f) else theme.surfaceColor)
                                .border(1.dp, if (sleepTimerRemainingSeconds != null) theme.secondaryAccent else theme.surfaceBorderColor, CircleShape)
                                .testTag("open_sleep_timer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = "Temporizador de apagado",
                                tint = if (sleepTimerRemainingSeconds != null) theme.secondaryAccent else theme.textSecondary,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // Botón Ecualizador DSP 503
                        IconButton(
                            onClick = onOpenEqualizer,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(theme.surfaceColor)
                                .border(1.dp, theme.surfaceBorderColor, CircleShape)
                                .testTag("open_equalizer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Equalizer,
                                contentDescription = "Ecualizador DSP",
                                tint = theme.primaryAccent,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                // Selector de Vistas BitChord: Carátula, Vinilo 3D, Letras Sincronizadas, Cola
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(theme.surfaceColor.copy(alpha = 0.6f))
                        .border(1.dp, theme.surfaceBorderColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabs = listOf(
                        PlayerViewMode.ARTWORK to "Carátula",
                        PlayerViewMode.VINYL_3D to "3D Vinilo",
                        PlayerViewMode.LYRICS to "Letras 🎤",
                        PlayerViewMode.QUEUE to "Cola (${queue.size})"
                    )

                    tabs.forEach { (mode, label) ->
                        val isSelected = playerViewMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) theme.primaryAccent else Color.Transparent)
                                .clickable {
                                    onSetPlayerViewMode(mode)
                                    if (mode == PlayerViewMode.VINYL_3D && !isPlayer3DMode) onTogglePlayer3DMode()
                                    else if (mode != PlayerViewMode.VINYL_3D && isPlayer3DMode) onTogglePlayer3DMode()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) theme.materialColorScheme.onPrimary else theme.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (sleepTimerRemainingSeconds != null) {
                        val mins = sleepTimerRemainingSeconds / 60
                        val secs = sleepTimerRemainingSeconds % 60
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(theme.secondaryAccent.copy(alpha = 0.18f))
                                .border(1.dp, theme.secondaryAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable { onOpenSleepTimerModal() }
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "🌙 Apagado en: %02d:%02d".format(mins, secs),
                                color = theme.secondaryAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (playbackSpeed != 1.0f) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(theme.primaryAccent.copy(alpha = 0.18f))
                                .border(1.dp, theme.primaryAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable { onOpenSpeedModal() }
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "⏱️ %.2fx".format(playbackSpeed),
                                color = theme.primaryAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Modo Visual Central (Carátula, Vinilo 3D, Letras Sincronizadas, Cola de Reproducción)
                when (playerViewMode) {
                    PlayerViewMode.VINYL_3D -> {
                        VinylPlayer3DView(
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            modifier = Modifier.fillMaxWidth(0.92f)
                        )
                    }
                    PlayerViewMode.LYRICS -> {
                        SyncedLyricsView(
                            lyrics = lyrics,
                            currentPositionSeconds = currentPositionSeconds,
                            onSeekTo = onSeekTo,
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .height(320.dp)
                        )
                    }
                    PlayerViewMode.QUEUE -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .height(320.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(theme.surfaceColor.copy(alpha = 0.55f))
                                .border(1.dp, theme.surfaceBorderColor.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "A CONTINUACIÓN (${queue.size})",
                                        color = theme.primaryAccent,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Gestionar Cola ↗",
                                        color = theme.secondaryAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { onOpenQueueModal() }
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                if (queue.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("La cola está vacía", color = theme.textMuted, fontSize = 13.sp)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        itemsIndexed(queue.take(8)) { idx, qSong ->
                                            val isCur = qSong.id == currentSong.id
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (isCur) theme.primaryAccent.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.25f))
                                                    .clickable { onSeekTo(0) }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${idx + 1}",
                                                    color = if (isCur) theme.primaryAccent else theme.textMuted,
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.width(20.dp)
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = qSong.title,
                                                        color = if (isCur) theme.primaryAccent else theme.textPrimary,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = qSong.artist,
                                                        color = theme.textSecondary,
                                                        fontSize = 10.sp,
                                                        maxLines = 1
                                                    )
                                                }
                                                Text(
                                                    text = qSong.durationFormatted,
                                                    color = theme.textMuted,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    PlayerViewMode.ARTWORK -> {
                        // Animación suave de escala y resplandor para la carátula durante la reproducción
                        val artworkInfiniteTransition = rememberInfiniteTransition(label = "ArtworkPulse")
                        val artworkPulseScale by artworkInfiniteTransition.animateFloat(
                            initialValue = 1.0f,
                            targetValue = if (isPlaying) 1.025f else 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2200, easing = FastOutSlowInEasing),
                                repeatMode = AnimationRepeatMode.Reverse
                            ),
                            label = "ArtworkScale"
                        )
                        val artworkGlowAlpha by artworkInfiniteTransition.animateFloat(
                            initialValue = 0.45f,
                            targetValue = if (isPlaying) 0.85f else 0.40f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1800, easing = LinearEasing),
                                repeatMode = AnimationRepeatMode.Reverse
                            ),
                            label = "ArtworkGlow"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .aspectRatio(1f)
                                .graphicsLayer {
                                    scaleX = artworkPulseScale
                                    scaleY = artworkPulseScale
                                }
                                .shadow(
                                    elevation = if (isPlaying) 32.dp else 20.dp,
                                    shape = RoundedCornerShape(28.dp),
                                    ambientColor = theme.primaryAccent.copy(alpha = artworkGlowAlpha),
                                    spotColor = theme.secondaryAccent.copy(alpha = artworkGlowAlpha)
                                )
                                .clip(RoundedCornerShape(28.dp))
                                .background(theme.surfaceColor)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            theme.primaryAccent.copy(alpha = artworkGlowAlpha),
                                            Color(0xFF00E5FF).copy(alpha = 0.8f),
                                            Color(0xFFC026D3).copy(alpha = 0.6f),
                                            theme.surfaceBorderColor.copy(alpha = 0.8f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(28.dp)
                                )
                        ) {
                            SongArtworkImage(
                                albumArtUri = currentSong.albumArtUri,
                                coverResId = currentSong.coverResId,
                                contentDescription = "Carátula de ${currentSong.album}",
                                modifier = Modifier.fillMaxSize()
                            )

                            // Reflejo de cristal superior (Liquid Glass)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.16f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            // Audio Format Pill Badge (Poweramp style)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(14.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.65f))
                                    .border(1.dp, theme.surfaceBorderColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = theme.primaryAccent,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = currentSong.audioFormat,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Song Title, Artist and Favorite Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentSong.title,
                            color = theme.textPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${currentSong.artist} • ${currentSong.genre}",
                            color = theme.textSecondary,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFavorite) theme.primaryAccent.copy(alpha = 0.2f)
                                else theme.surfaceColor
                            )
                            .border(
                                1.dp,
                                if (isFavorite) theme.primaryAccent else theme.surfaceBorderColor,
                                CircleShape
                            )
                            .testTag("favorite_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                            tint = if (isFavorite) Color(0xFFFF3366) else theme.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar & Time Labels (Animada estilo iStock 1289638909 con llenado multicolor reactivo)
                var waveformStyle by remember { mutableStateOf(WaveformProgressStyle.NEON_EQUALIZER_BARS) }

                Column(modifier = Modifier.fillMaxWidth()) {
                    // Selector sutil de estilos de onda (Ecualizador HUD, Onda Espectral, Pulso Neón)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VISUALIZADOR HUD 503",
                            color = theme.primaryAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val styles = listOf(
                                WaveformProgressStyle.NEON_EQUALIZER_BARS to "Barras 📊",
                                WaveformProgressStyle.LIQUID_GLASS_WAVE to "Liquid 💧",
                                WaveformProgressStyle.FREQUENCY_PULSE to "Pulso ⚡"
                            )
                            styles.forEach { (st, label) ->
                                val isSelected = waveformStyle == st
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) theme.primaryAccent.copy(alpha = 0.25f) else Color.Transparent)
                                        .clickable { waveformStyle = st }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) theme.primaryAccent else theme.textMuted,
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // Barra interactiva de onda con colores que se van llenando conforme avanza la canción
                    AnimatedWaveformProgressBar(
                        progress = if (totalDuration > 0f) currentProgress / totalDuration else 0f,
                        isPlaying = isPlaying,
                        onSeek = { targetFraction ->
                            onSeekTo((targetFraction * totalDuration).toInt())
                        },
                        height = 42.dp,
                        style = waveformStyle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentFormatted,
                            color = theme.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LiquidGlassPillButton(
                                onClick = onSeekBackward,
                                text = "⏪ -10s",
                                accentColor = theme.primaryAccent,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                modifier = Modifier.testTag("rewind_10s_button")
                            )

                            LiquidGlassPillButton(
                                onClick = onSeekForward,
                                text = "+10s ⏩",
                                accentColor = theme.primaryAccent,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                modifier = Modifier.testTag("forward_10s_button")
                            )
                        }

                        Text(
                            text = remainingFormatted,
                            color = theme.textMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Playback Control Cluster (Previous, Play/Pause, Next, Shuffle, Repeat)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle Toggle
                    IconButton(
                        onClick = onToggleShuffle,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("shuffle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Modo aleatorio",
                            tint = if (isShuffle) theme.primaryAccent else theme.textMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Previous Button Liquid Glass
                    LiquidGlassIconButton(
                        onClick = onPlayPrevious,
                        icon = Icons.Default.SkipPrevious,
                        contentDescription = "Canción anterior",
                        size = 54.dp,
                        iconSize = 28.dp,
                        tint = theme.textPrimary,
                        modifier = Modifier.testTag("prev_button")
                    )

                    // Big Circular Play/Pause with Liquid Glass Specular Rim and Glowing Core
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .shadow(
                                elevation = 20.dp,
                                shape = CircleShape,
                                ambientColor = theme.glowColor,
                                spotColor = theme.glowColor
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        theme.primaryAccent,
                                        theme.secondaryAccent
                                    )
                                )
                            )
                            .border(
                                width = 1.5.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.65f),
                                        Color.White.copy(alpha = 0.15f)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .clickable(onClick = onTogglePlayPause)
                            .testTag("play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        // Reflejo especular superior tenue
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.28f),
                                            Color.Transparent
                                        ),
                                        startY = 0f,
                                        endY = 38f
                                    )
                                )
                        )

                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint = theme.materialColorScheme.onPrimary,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // Next Button Liquid Glass
                    LiquidGlassIconButton(
                        onClick = onPlayNext,
                        icon = Icons.Default.SkipNext,
                        contentDescription = "Siguiente canción",
                        size = 54.dp,
                        iconSize = 28.dp,
                        tint = theme.textPrimary,
                        modifier = Modifier.testTag("next_button")
                    )

                    // Repeat Mode Toggle
                    IconButton(
                        onClick = onToggleRepeat,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("repeat_button")
                    ) {
                        val repeatIcon = when (repeatMode) {
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        }
                        val tint = when (repeatMode) {
                            RepeatMode.OFF -> theme.textMuted
                            else -> theme.primaryAccent
                        }
                        Icon(
                            imageVector = repeatIcon,
                            contentDescription = "Repetir canción",
                            tint = tint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Audio Status Info Card (Poweramp style: Equalizer + Bass Boost info)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(theme.surfaceColor.copy(alpha = 0.5f))
                        .border(1.dp, theme.surfaceBorderColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = theme.primaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Motor de Audio 503 • Salida Hi-Res Direct",
                                color = theme.textSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = currentSong.folderName.substringAfterLast('/'),
                            color = theme.textMuted,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
