package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.ScanState
import com.example.state.MusicPlayerViewModel
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.DistritoThemeConfig
import com.example.ui.theme.DistritoThemePresets
import com.example.ui.theme.LocalDistritoTheme

@Composable
fun SettingsScreen(
    viewModel: MusicPlayerViewModel,
    onRequestPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentTheme = LocalDistritoTheme.current
    val scanState = viewModel.scanState
    val isScanning = scanState is ScanState.Scanning

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        item {
            // Header
            Text(
                text = "Ajustes y Configuración",
                color = currentTheme.textPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Real Library Sync & Scan Section (Prompt Requirement 8)
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    tint = currentTheme.primaryAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BIBLIOTECA MUSICAL Y ESCANEO",
                    color = currentTheme.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("library_sync_card"),
                cornerRadius = 18.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Escanear Música del Dispositivo",
                                color = currentTheme.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Detecta canciones en almacenamiento interno y descargas",
                                color = currentTheme.textSecondary,
                                fontSize = 12.sp
                            )
                        }

                        if (isScanning) {
                            CircularProgressIndicator(
                                color = currentTheme.primaryAccent,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats row: Canciones encontradas & Última sincronización
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(currentTheme.surfaceVariantColor.copy(alpha = 0.5f))
                            .border(1.dp, currentTheme.surfaceBorderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Canciones encontradas",
                                color = currentTheme.textMuted,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${viewModel.songsList.size} canciones",
                                color = currentTheme.primaryAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Última sincronización",
                                color = currentTheme.textMuted,
                                fontSize = 11.sp
                            )
                            Text(
                                text = viewModel.lastScanFormatted,
                                color = currentTheme.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Estado actual / Botón de acción
                    Button(
                        onClick = {
                            if (!viewModel.isPermissionGranted) {
                                onRequestPermission()
                            } else {
                                viewModel.scanLibrary()
                            }
                        },
                        enabled = !isScanning,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = currentTheme.primaryAccent,
                            contentColor = currentTheme.materialColorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rescan_library_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isScanning) "Escaneando archivos..." else "Actualizar Biblioteca Musical",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    if (scanState is ScanState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aviso: ${scanState.error}",
                            color = currentTheme.materialColorScheme.error,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Themes Section
        item {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = currentTheme.primaryAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TEMAS Y APARIENCIA",
                    color = currentTheme.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = "Personaliza el estilo visual del reproductor. El tema Liquid Glass es translúcido y fluido.",
                color = currentTheme.textSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Theme Cards
        items(DistritoThemePresets.allThemes.size) { index ->
            val themeConfig = DistritoThemePresets.allThemes[index]
            val isSelected = viewModel.currentThemeKey == themeConfig.key

            ThemeSelectionCard(
                themeConfig = themeConfig,
                isSelected = isSelected,
                onSelect = { viewModel.setTheme(themeConfig.key) },
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        // Audio Engine Section (Poweramp Inspired)
        item {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = null,
                    tint = currentTheme.primaryAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MOTOR DE AUDIO Y ECUALIZADOR",
                    color = currentTheme.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Equalizer Preset Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Perfil de Ecualización",
                                color = currentTheme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = viewModel.equalizerPreset,
                                color = currentTheme.primaryAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = currentTheme.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Preset Chips
                    val presets = listOf(
                        "Club 503", "Graves", "Cristalino", "Voz", "Dinámico"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.forEach { preset ->
                            val active = viewModel.equalizerPreset.contains(preset)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (active) currentTheme.primaryAccent.copy(alpha = 0.25f)
                                        else currentTheme.surfaceVariantColor.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        1.dp,
                                        if (active) currentTheme.primaryAccent else currentTheme.surfaceBorderColor.copy(alpha = 0.4f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.setEqualizer("Preset: $preset") }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = preset,
                                    color = if (active) currentTheme.primaryAccent else currentTheme.textSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bass Boost Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Refuerzo de Graves (Bass Boost)",
                                color = currentTheme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Resonancia acústica profunda a 60Hz",
                                color = currentTheme.textSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Switch(
                            checked = viewModel.isBassBoostEnabled,
                            onCheckedChange = { viewModel.toggleBassBoost() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = currentTheme.materialColorScheme.onPrimary,
                                checkedTrackColor = currentTheme.primaryAccent,
                                uncheckedTrackColor = currentTheme.surfaceBorderColor
                            ),
                            modifier = Modifier.testTag("bass_boost_switch")
                        )
                    }

                    if (viewModel.isBassBoostEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${viewModel.bassBoostLevel}%",
                                color = currentTheme.primaryAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(40.dp)
                            )
                            Slider(
                                value = viewModel.bassBoostLevel.toFloat(),
                                onValueChange = { viewModel.updateBassBoostLevel(it.toInt()) },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = currentTheme.primaryAccent,
                                    activeTrackColor = currentTheme.primaryAccent
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botón para abrir el Ecualizador DSP Gráfico 503
                    Button(
                        onClick = { viewModel.showEqualizerModal(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = currentTheme.primaryAccent.copy(alpha = 0.2f),
                            contentColor = currentTheme.primaryAccent
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, currentTheme.primaryAccent.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .testTag("open_dsp_from_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Abrir Ecualizador DSP 503 (5 Bandas)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.showStatsForNerds(true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentTheme.surfaceColor,
                                contentColor = currentTheme.textPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, currentTheme.surfaceBorderColor, RoundedCornerShape(14.dp))
                                .testTag("open_stats_from_settings")
                        ) {
                            Text(text = "Stats for Nerds 🤓", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.showPlaybackSpeedModal(true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentTheme.surfaceColor,
                                contentColor = currentTheme.textPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, currentTheme.surfaceBorderColor, RoundedCornerShape(14.dp))
                                .testTag("open_speed_from_settings")
                        ) {
                            Text(text = "Velocidad (${viewModel.playbackSpeed}x)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Temporizador de Apagado (Sleep Timer)
        item {
            Spacer(modifier = Modifier.height(18.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = currentTheme.secondaryAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TEMPORIZADOR DE APAGADO",
                    color = currentTheme.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sleep_timer_card"),
                cornerRadius = 18.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pausa Automática al Dormir",
                                color = currentTheme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (viewModel.sleepTimerRemainingSeconds != null) {
                                    val mins = (viewModel.sleepTimerRemainingSeconds ?: 0) / 60
                                    val secs = (viewModel.sleepTimerRemainingSeconds ?: 0) % 60
                                    "Apagando la música en %02d:%02d".format(mins, secs)
                                } else {
                                    "Selecciona cuándo pausar la reproducción"
                                },
                                color = if (viewModel.sleepTimerRemainingSeconds != null) currentTheme.secondaryAccent else currentTheme.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (viewModel.sleepTimerRemainingSeconds != null) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        if (viewModel.sleepTimerRemainingSeconds != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(currentTheme.secondaryAccent.copy(alpha = 0.2f))
                                    .border(1.dp, currentTheme.secondaryAccent, RoundedCornerShape(10.dp))
                                    .clickable { viewModel.setSleepTimer(0) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Cancelar",
                                    color = currentTheme.secondaryAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset chips de temporizador: 15, 30, 45, 60 min
                    val timerOptions = listOf(15, 30, 45, 60)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        timerOptions.forEach { minutes ->
                            val isSelected = viewModel.sleepTimerRemainingSeconds?.let { it / 60 == minutes } == true
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) currentTheme.secondaryAccent.copy(alpha = 0.25f)
                                        else currentTheme.surfaceVariantColor.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) currentTheme.secondaryAccent else currentTheme.surfaceBorderColor.copy(alpha = 0.4f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setSleepTimer(minutes) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$minutes min",
                                    color = if (isSelected) currentTheme.secondaryAccent else currentTheme.textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(currentTheme.surfaceVariantColor.copy(alpha = 0.4f))
                            .border(1.dp, currentTheme.surfaceBorderColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { viewModel.showSleepTimerModal(true) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Opciones Avanzadas de Apagado (Fin de pista, etc.) 🌙",
                            color = currentTheme.secondaryAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Fondos de Pantalla Traslúcidos (Batman Edition)
        item {
            Spacer(modifier = Modifier.height(18.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = currentTheme.primaryAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FONDOS TRASLÚCIDOS POR PANTALLA (BATMAN)",
                    color = currentTheme.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("batman_wallpapers_card"),
                cornerRadius = 18.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Fondos de Pantalla Traslúcidos y Difuminados",
                        color = currentTheme.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tus 3 imágenes de Batman aparecen como fondo en cada pantalla. Puedes seleccionar una fija para toda la app o dejar el modo automático (una distinta en cada pantalla).",
                        color = currentTheme.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botón para modo Automático por Pantalla
                    val isAutoMode = viewModel.customWallpaperRes == null
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isAutoMode) currentTheme.primaryAccent.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.04f))
                            .border(1.2.dp, if (isAutoMode) currentTheme.primaryAccent else Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .clickable { viewModel.updateCustomWallpaper(null) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "✨ Automático (Una imagen en cada pantalla)",
                                color = if (isAutoMode) currentTheme.primaryAccent else currentTheme.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Home: Caverna Azul • Biblioteca: Tormenta de Humo • Favoritos: Aura Carmesí",
                                color = currentTheme.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                        if (isAutoMode) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(currentTheme.primaryAccent)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "ACTIVO",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    listOf(
                        Triple(
                            "Pantalla de Inicio (Home)",
                            "Caverna Subterránea con Murciélagos y Luz Cyan",
                            R.drawable.bg_batman_cave
                        ),
                        Triple(
                            "Pantalla de Favoritos",
                            "Batman en Aura Carmesí Neón con Lluvia",
                            R.drawable.bg_batman_crimson
                        ),
                        Triple(
                            "Biblioteca y Búsqueda",
                            "Silueta de Batman en Tormenta de Humo",
                            R.drawable.bg_batman_smoke
                        )
                    ).forEach { (screenTitle, desc, imageRes) ->
                        val isSelected = viewModel.customWallpaperRes == imageRes
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) currentTheme.primaryAccent.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                                .border(1.dp, if (isSelected) currentTheme.primaryAccent else Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .clickable { viewModel.updateCustomWallpaper(imageRes) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.5.dp, if (isSelected) currentTheme.primaryAccent else currentTheme.primaryAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = imageRes),
                                    contentDescription = screenTitle,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = screenTitle,
                                        color = currentTheme.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (isSelected || (isAutoMode && (
                                        (imageRes == R.drawable.bg_batman_cave && screenTitle.contains("Inicio")) ||
                                        (imageRes == R.drawable.bg_batman_crimson && screenTitle.contains("Favoritos")) ||
                                        (imageRes == R.drawable.bg_batman_smoke && screenTitle.contains("Biblioteca"))
                                    ))) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(currentTheme.primaryAccent.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isSelected) "FIJADO" else "ASIGNADO",
                                                color = currentTheme.primaryAccent,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    color = currentTheme.textSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Controles de Visibilidad y Difuminado (Blur)
                    Text(
                        text = "Visibilidad / Traslucidez de la Imagen",
                        color = currentTheme.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            0.85f to "85% (Muy Visible)",
                            0.70f to "70% (Equilibrado)",
                            0.50f to "50% (Sutil)"
                        ).forEach { (alphaVal, label) ->
                            val isChosen = (viewModel.wallpaperAlpha == alphaVal)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChosen) currentTheme.primaryAccent.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, if (isChosen) currentTheme.primaryAccent else Color.White.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.updateWallpaperAlpha(alphaVal) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isChosen) currentTheme.primaryAccent else currentTheme.textSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Efecto de Difuminado (Desenfoque / Blur)",
                        color = currentTheme.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            0 to "Nítido (0 dp)",
                            2 to "Suave (2 dp)",
                            5 to "Medio (5 dp)"
                        ).forEach { (blurVal, label) ->
                            val isChosen = (viewModel.wallpaperBlurDp == blurVal)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChosen) currentTheme.primaryAccent.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, if (isChosen) currentTheme.primaryAccent else Color.White.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.updateWallpaperBlur(blurVal) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isChosen) currentTheme.primaryAccent else currentTheme.textSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Servicio de Red YouTube Data API v3 (Retrofit)
        item {
            Spacer(modifier = Modifier.height(18.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SmartDisplay,
                    contentDescription = null,
                    tint = Color(0xFFFF334B),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SERVICIO DE RED YOUTUBE DATA API",
                    color = currentTheme.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("youtube_api_status_card"),
                cornerRadius = 18.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Búsqueda Musical con Retrofit & Moshi",
                                color = currentTheme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Cliente HTTP OkHttpClient optimizado con timeouts, reintentos y mapeo reactivo a modelos de dominio.",
                                color = currentTheme.textSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFF334B).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFFFF334B), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ACTIVO",
                                color = Color(0xFFFF5566),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Características del servicio
                    listOf(
                        "Filtro estricto de pistas musicales (Categoría 10) y videos integrables",
                        "Resolución de duraciones ISO 8601 y contador de vistas por pista",
                        "Integración de clave mediante Secrets Gradle Plugin y BuildConfig.YOUTUBE_API_KEY",
                        "Fallback automático con pistas demostrativas oficiales si la clave no está configurada"
                    ).forEach { feature ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✓", color = Color(0xFFFF5566), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feature,
                                color = currentTheme.textMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Descarga e Instalación del APK en Móvil (Respuesta a usuario en móvil)
        item {
            Spacer(modifier = Modifier.height(18.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Smartphone,
                    contentDescription = null,
                    tint = currentTheme.primaryAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DESCARGA Y EXPORTACIÓN EN TU MÓVIL",
                    color = currentTheme.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mobile_apk_guide_card"),
                cornerRadius = 18.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(currentTheme.primaryAccent.copy(alpha = 0.2f))
                                .border(1.dp, currentTheme.primaryAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = currentTheme.primaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "¿Cómo descargar el APK desde tu celular?",
                                color = currentTheme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Instrucciones paso a paso para Google AI Studio",
                                color = currentTheme.primaryAccent,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Si estás usando Google AI Studio desde el navegador de tu teléfono móvil:\n\n" +
                                "1. En la esquina superior derecha de la pantalla de Google AI Studio, busca el botón de ajustes/menú (ícono de tres puntos o engranaje).\n" +
                                "2. Elige 'Export' -> 'Download ZIP' para descargar todo el código compilable de tu app.\n" +
                                "3. Consejo Móvil: Si no ves la opción de descarga porque la barra está oculta en pantallas pequeñas, gira tu teléfono a modo horizontal o activa en Chrome la opción 'Sitio para computadora' (Vista de escritorio) en el menú del navegador para ver todos los botones de exportación y generar tu APK.\n" +
                                "4. Estado: Distrito Music 503 está 100% optimizado, compilado y con soporte completo para instalación en Android.",
                        color = currentTheme.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // App Branding / About Section
        item {
            Spacer(modifier = Modifier.height(20.dp))

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .shadow(12.dp, CircleShape, ambientColor = currentTheme.glowColor, spotColor = currentTheme.glowColor)
                            .clip(CircleShape)
                            .background(currentTheme.surfaceColor)
                            .border(2.dp, currentTheme.primaryAccent, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.distrito_logo),
                            contentDescription = "Distrito Music 503",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Distrito Music 503",
                            color = currentTheme.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Versión 1.0.0 • Audio Engine Nativo",
                            color = currentTheme.textSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Desarrollado con Jetpack Compose & Material 3",
                            color = currentTheme.textMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}

@Composable
fun ThemeSelectionCard(
    themeConfig: DistritoThemeConfig,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTheme = LocalDistritoTheme.current

    GlassmorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("theme_card_${themeConfig.key.name.lowercase()}"),
        cornerRadius = 18.dp,
        borderWidth = if (isSelected) 2.dp else 1.dp,
        customBackgroundColor = if (isSelected) currentTheme.surfaceColor.copy(alpha = 0.9f) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Theme Swatch Palette Preview
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(themeConfig.backgroundBrush)
                    .border(1.dp, themeConfig.surfaceBorderColor, RoundedCornerShape(12.dp))
                    .padding(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(themeConfig.primaryAccent)
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(themeConfig.secondaryAccent)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = themeConfig.name,
                        color = currentTheme.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ACTIVO",
                            color = currentTheme.primaryAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = themeConfig.description,
                    color = currentTheme.textSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Seleccionado",
                    tint = currentTheme.primaryAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
