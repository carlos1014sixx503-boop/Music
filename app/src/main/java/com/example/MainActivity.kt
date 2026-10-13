package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.state.MusicPlayerState
import com.example.ui.components.EqualizerDspModal
import com.example.ui.components.FullScreenPlayer
import com.example.ui.components.YouTubeOnlineMiniBar
import com.example.ui.components.YouTubePlayerModal
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BottomDockBackground
import com.example.ui.theme.DeepVioletBackground
import com.example.ui.theme.NeonOrbCyan
import com.example.ui.theme.NeonOrbPink
import com.example.ui.theme.NeonOrbPurple
import com.example.ui.theme.NeonOrbViolet
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SoundWaveTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoundWaveTheme {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent(playerState: MusicPlayerState = viewModel()) {
    var selectedScreen by remember { mutableIntStateOf(0) }

    val activeYouTubeVideo by playerState.activeYouTubeVideo.collectAsState()
    val isYouTubeModalVisible by playerState.isYouTubeModalVisible.collectAsState()
    val isFullScreenPlayerVisible by playerState.isFullScreenPlayerVisible.collectAsState()

    val isEqualizerVisible by playerState.isEqualizerVisible.collectAsState()
    val isDspEnabled by playerState.isDspEnabled.collectAsState()
    val equalizerPreset by playerState.equalizerPreset.collectAsState()
    val bandLevels by playerState.bandLevels.collectAsState()
    val bassBoostPercent by playerState.bassBoostPercent.collectAsState()
    val virtualizerPercent by playerState.virtualizerPercent.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepVioletBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Liquid Purple Silk Wallpaper matching reference image
            Image(
                painter = painterResource(id = R.drawable.bg_liquid_purple_silk),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.88f
            )

            // Obsidian Vignette Overlay for maximum UI contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x990A0314),
                                Color(0x660D051C),
                                Color(0xCC07020F)
                            )
                        )
                    )
            )

            // Main Screen Content Switcher
            AnimatedContent(
                targetState = selectedScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition",
                modifier = Modifier.fillMaxSize()
            ) { targetScreen ->
                when (targetScreen) {
                    0 -> HomeScreen(
                        playerState = playerState,
                        onNavigateToSearch = { selectedScreen = 1 },
                        onNavigateToLibrary = { selectedScreen = 2 },
                        onNavigateToEqualizer = { selectedScreen = 3 },
                        onNavigateToSettings = { selectedScreen = 4 }
                    )
                    1 -> SearchScreen(
                        playerState = playerState
                    )
                    2 -> LibraryScreen(
                        playerState = playerState,
                        onNavigateToSearch = { selectedScreen = 1 }
                    )
                    3 -> EqualizerScreen(
                        playerState = playerState
                    )
                    4 -> SettingsScreen(
                        playerState = playerState
                    )
                }
            }

            // Bottom Floating Controls Container: MiniPlayer + Floating Navigation Capsule
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Persistent Mini Player (Tap to expand to Screen 3 FullScreenPlayer)
                YouTubeOnlineMiniBar(
                    playerState = playerState,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Floating Pill Navigation Dock with 3D Glowing Neon Orb from reference mockup
                FloatingNavigationDock(
                    selectedScreen = selectedScreen,
                    onSelectScreen = { selectedScreen = it }
                )
            }

            // YouTube Video Player Modal Dialog
            val currentYtVideo = activeYouTubeVideo
            if (isYouTubeModalVisible && currentYtVideo != null) {
                YouTubePlayerModal(
                    video = currentYtVideo,
                    playerState = playerState,
                    onDismiss = { playerState.closeYouTubePlayer() }
                )
            }

            // Screen 3: Full-Screen Player Dialog
            if (isFullScreenPlayerVisible) {
                FullScreenPlayer(
                    playerState = playerState,
                    onDismiss = { playerState.hideFullScreenPlayer() }
                )
            }

            // Always Accessible Equalizer & DSP Modal Dialog
            EqualizerDspModal(
                visible = isEqualizerVisible,
                isDspEnabled = isDspEnabled,
                equalizerPreset = equalizerPreset,
                bandLevels = bandLevels,
                bassBoostPercent = bassBoostPercent,
                virtualizerPercent = virtualizerPercent,
                onToggleDsp = { playerState.setDspEnabled(it) },
                onSetPreset = { name, levels -> playerState.setEqualizerPreset(name, levels) },
                onBandChange = { idx, lvl -> playerState.setBandLevel(idx, lvl) },
                onBassBoostChange = { playerState.setBassBoost(it) },
                onVirtualizerChange = { playerState.setVirtualizer(it) },
                onReset = { playerState.resetEqualizer() },
                onDismiss = { playerState.hideEqualizer() }
            )
        }
    }
}

/**
 * Floating Pill Navigation Dock matching reference mockup:
 * - Ultra-glossy black liquid capsule with soft glass rim.
 * - Glowing 3D radial neon orb behind the active icon.
 * - 5 authentic destinations: Inicio, Búsqueda YouTube, Música Local, Ecualizador, Ajustes.
 */
@Composable
fun FloatingNavigationDock(
    selectedScreen: Int,
    onSelectScreen: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(20.dp, RoundedCornerShape(36.dp), ambientColor = Color.Black, spotColor = NeonPurple)
            .clip(RoundedCornerShape(36.dp))
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.28f),
                        Color.White.copy(alpha = 0.06f)
                    )
                ),
                RoundedCornerShape(36.dp)
            )
            .background(BottomDockBackground)
            .testTag("floating_nav_dock"),
        color = BottomDockBackground,
        shape = RoundedCornerShape(36.dp),
        tonalElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1: Inicio / Tendencias
            DockItem(
                icon = Icons.Default.Home,
                contentDescription = "Inicio",
                isSelected = selectedScreen == 0,
                onClick = { onSelectScreen(0) },
                testTag = "nav_item_home"
            )

            // 2: Búsqueda YouTube
            DockItem(
                icon = Icons.Default.Search,
                contentDescription = "Búsqueda YouTube",
                isSelected = selectedScreen == 1,
                onClick = { onSelectScreen(1) },
                testTag = "nav_item_search"
            )

            // 3: Escaneo de Música Local / Carpetas A-Z
            DockItem(
                icon = Icons.Default.FolderOpen,
                contentDescription = "Música Local",
                isSelected = selectedScreen == 2,
                onClick = { onSelectScreen(2) },
                testTag = "nav_item_library"
            )

            // 4: Ecualizador DSP
            DockItem(
                icon = Icons.Default.GraphicEq,
                contentDescription = "Ecualizador",
                isSelected = selectedScreen == 3,
                onClick = { onSelectScreen(3) },
                testTag = "nav_item_equalizer"
            )

            // 5: Sincronización y Ajustes
            DockItem(
                icon = Icons.Default.Settings,
                contentDescription = "Ajustes",
                isSelected = selectedScreen == 4,
                onClick = { onSelectScreen(4) },
                testTag = "nav_item_settings"
            )
        }
    }
}

@Composable
fun DockItem(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .then(
                if (isSelected) {
                    Modifier
                        .shadow(16.dp, CircleShape, ambientColor = NeonOrbPink, spotColor = NeonOrbPurple)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFF26C2), // Hot vibrant magenta core
                                    Color(0xFFA855F7), // Neon purple
                                    Color(0xFF6B21A8), // Deep purple
                                    Color(0xFF3B0764)  // Dark rim
                                )
                            )
                        )
                        .border(
                            1.2.dp,
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.65f),
                                    Color.White.copy(alpha = 0.15f)
                                )
                            ),
                            CircleShape
                        )
                } else Modifier
            )
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isSelected) TextWhite else Color(0xFF8C79A9),
            modifier = Modifier.size(22.dp)
        )
    }
}
