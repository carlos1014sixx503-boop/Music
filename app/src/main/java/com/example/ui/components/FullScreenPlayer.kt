package com.example.ui.components

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.state.MusicPlayerState
import com.example.ui.theme.AccentPink
import com.example.ui.theme.DeepVioletBackground
import com.example.ui.theme.DeepVioletCard
import com.example.ui.theme.GlassSurfaceHigh
import com.example.ui.theme.GlassSurfaceUltraDark
import com.example.ui.theme.NeonOrbCyan
import com.example.ui.theme.NeonOrbPink
import com.example.ui.theme.NeonOrbPurple
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

/**
 * Screen 3: Fullscreen Now Playing matching reference image:
 * - Top bar: circular back button `<` on left, "Now Playing" centered, circular share on right.
 * - 3D Album Cover Carousel:
 *   - Center glossy 3D glass framed card featuring Blinding Lights album artwork.
 *   - Peeking side cards on left and right.
 * - Track Info:
 *   - "The Weeknd" (artist subtitle).
 *   - "Blinding Lights" (headline).
 *   - Heart favorite circular glass button on right.
 * - Audio Waveform progress bar with time markers.
 * - Controls row:
 *   - Loop/Repeat toggle.
 *   - Previous `<|`.
 *   - Center 3D Swirling Neon Liquid Orb Play/Pause button (`||`).
 *   - Next `|>`.
 *   - Queue playlist icon.
 */
@Composable
fun FullScreenPlayer(
    playerState: MusicPlayerState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val track = playerState.currentTrack.collectAsState().value ?: return
    val playlist by playerState.playlist.collectAsState()
    val isPlaying by playerState.isPlaying.collectAsState()
    val currentPositionMs by playerState.playbackPositionMs.collectAsState()
    val durationMs by playerState.durationMs.collectAsState()
    val isRepeat by playerState.isRepeat.collectAsState()
    val isFavorite = playerState.isTrackFavorite(track.id)

    BackHandler {
        onDismiss()
    }

    val currentIndex = playlist.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
    val prevTrack = if (playlist.isNotEmpty()) {
        val prevIdx = if (currentIndex <= 0) playlist.size - 1 else currentIndex - 1
        playlist[prevIdx]
    } else null
    val nextTrack = if (playlist.isNotEmpty()) {
        val nextIdx = (currentIndex + 1) % playlist.size
        playlist[nextIdx]
    } else null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DeepVioletBackground
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Liquid Silk Wallpaper
                Image(
                    painter = painterResource(id = R.drawable.bg_liquid_purple_silk),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alpha = 0.85f
                )

                // Dark obsidian vignette for high clarity
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xCC0D041A),
                                    Color(0x99120626),
                                    Color(0xEE090214)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Top Bar: Back Circle, "Now Playing", Share Circle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button in translucent circle
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GlassSurfaceUltraDark)
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.18f),
                                    CircleShape
                                )
                                .clickable { onDismiss() }
                                .testTag("now_playing_back_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Back",
                                tint = TextWhite,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = "Now Playing",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = TextWhite
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Equalizer DSP Button (Quick access in player)
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(GlassSurfaceUltraDark)
                                    .border(
                                        1.dp,
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.28f),
                                                NeonPurple.copy(alpha = 0.35f)
                                            )
                                        ),
                                        CircleShape
                                    )
                                    .clickable { playerState.showEqualizer() }
                                    .testTag("now_playing_equalizer_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Ecualizador y DSP",
                                    tint = NeonPurpleLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Share / Export Button
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(GlassSurfaceUltraDark)
                                    .border(
                                        1.dp,
                                        Color.White.copy(alpha = 0.18f),
                                        CircleShape
                                    )
                                    .clickable {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Listening to \"${track.title}\" by ${track.artist} on SoundWave!"
                                            )
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Track"))
                                    }
                                    .testTag("now_playing_share_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.IosShare,
                                    contentDescription = "Share",
                                    tint = TextWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3D Beveled Carousel Artwork Row matching Screen 3
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(290.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Left peeking album cover
                        if (prevTrack != null) {
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height(230.dp)
                                    .clip(RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 18.dp, bottomEnd = 18.dp))
                                    .border(
                                        1.dp,
                                        Color.White.copy(alpha = 0.15f),
                                        RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 18.dp, bottomEnd = 18.dp)
                                    )
                                    .background(DeepVioletCard)
                                    .clickable { playerState.playPrevious() }
                            ) {
                                AsyncImage(
                                    model = prevTrack.albumArtUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    alpha = 0.35f,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Center Active Artwork Card with 3D Glossy Liquid Glass Frame
                        Box(
                            modifier = Modifier
                                .size(270.dp)
                                .shadow(32.dp, RoundedCornerShape(26.dp), ambientColor = Color.Black, spotColor = NeonPurple)
                                .clip(RoundedCornerShape(26.dp))
                                .border(
                                    2.5.dp,
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.45f),
                                            Color.White.copy(alpha = 0.10f),
                                            Color(0x55A855F7)
                                        )
                                    ),
                                    RoundedCornerShape(26.dp)
                                )
                                .background(Color(0xFF140728))
                                .testTag("active_artwork_card")
                        ) {
                            if (track.title.contains("Blinding", ignoreCase = true)) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_cover_blinding_lights),
                                    contentDescription = "Blinding Lights Album Cover",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                AsyncImage(
                                    model = track.albumArtUrl,
                                    contentDescription = "Album Art",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Inner specular reflection overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.12f),
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.30f)
                                            )
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Right peeking album cover
                        if (nextTrack != null) {
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height(230.dp)
                                    .clip(RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp, topEnd = 0.dp, bottomEnd = 0.dp))
                                    .border(
                                        1.dp,
                                        Color.White.copy(alpha = 0.15f),
                                        RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp, topEnd = 0.dp, bottomEnd = 0.dp)
                                    )
                                    .background(DeepVioletCard)
                                    .clickable { playerState.playNext() }
                            ) {
                                AsyncImage(
                                    model = nextTrack.albumArtUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    alpha = 0.35f,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Track Title, Artist & Favorite Heart matching Screen 3
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.artist,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Circular Heart Favorite Button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GlassSurfaceUltraDark)
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.18f),
                                    CircleShape
                                )
                                .clickable { playerState.toggleFavorite(track.id) }
                                .testTag("now_playing_heart_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) AccentPink else TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Waveform Audio Visualizer
                    AnimatedWaveformProgressBar(
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        isPlaying = isPlaying,
                        onSeek = { playerState.seekTo(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Playback Controls Row matching Screen 3:
                    // Loop/Repeat, Prev, 3D Swirling Orb Play/Pause, Next, Queue
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Repeat toggle
                        IconButton(
                            onClick = { playerState.toggleRepeat() },
                            modifier = Modifier.testTag("now_playing_repeat_btn")
                        ) {
                            Icon(
                                imageVector = if (isRepeat) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                contentDescription = "Repeat",
                                tint = if (isRepeat) NeonPurpleLight else TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Previous Track
                        IconButton(
                            onClick = { playerState.playPrevious() },
                            modifier = Modifier.testTag("now_playing_prev_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous Track",
                                tint = TextWhite,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // 3D Swirling Neon Liquid Orb Play/Pause Button from Screen 3
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .shadow(24.dp, CircleShape, ambientColor = NeonOrbPink, spotColor = NeonOrbPurple)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF00F0FF), // Glowing cyan inner swirl
                                            NeonOrbPink,       // Vibrant magenta swirl
                                            Color(0xFF7928CA), // Deep violet
                                            Color(0xFF1E0741)  // Obsidian rim
                                        )
                                    )
                                )
                                .border(
                                    2.dp,
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.70f),
                                            Color(0x66EC4899),
                                            Color.Transparent
                                        )
                                    ),
                                    CircleShape
                                )
                                .clickable { playerState.togglePlayPause() }
                                .testTag("now_playing_play_pause_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = TextWhite,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Next Track
                        IconButton(
                            onClick = { playerState.playNext() },
                            modifier = Modifier.testTag("now_playing_next_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Track",
                                tint = TextWhite,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Queue button
                        IconButton(
                            onClick = { /* Queue list or action */ },
                            modifier = Modifier.testTag("now_playing_queue_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = "Queue",
                                tint = TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
