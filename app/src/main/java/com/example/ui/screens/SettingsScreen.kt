package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import com.example.R
import com.example.state.MusicPlayerState
import com.example.ui.theme.GlassSurfaceHigh
import com.example.ui.theme.GlassSurfaceUltraDark
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleGlow
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Screen 5: YouTube Account Synchronization & App Settings Screen (Sincronización y Ajustes)
 * - Vinculación y sincronización de cuenta de YouTube Music (Playlists, Me Gusta, Canales).
 * - Ajustes de streaming (320 kbps, 256 kbps, 128 kbps).
 * - Controles de audio: Normalización de volumen, reproducción continua, pausa por desconexión.
 * - Gestión de caché, almacenamiento y borrado de historial.
 * - Estética Liquid Glass con halos púrpuras y estilo auténtico.
 */
@Composable
fun SettingsScreen(
    playerState: MusicPlayerState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isSyncing by remember { mutableStateOf(false) }
    var syncStatusText by remember { mutableStateOf("Sincronizado recientemente") }
    var lastSyncTimestamp by remember { mutableStateOf("Hace 2 minutos") }

    // Preferencias de Streaming y Audio
    var selectedAudioQualityIndex by remember { mutableIntStateOf(0) } // 0: 320 kbps, 1: 256 kbps, 2: 128 kbps
    var autoSyncPlaylists by remember { mutableStateOf(true) }
    var syncWifiOnly by remember { mutableStateOf(true) }
    var normalizeVolume by remember { mutableStateOf(true) }
    var gaplessPlayback by remember { mutableStateOf(true) }
    var pauseOnUnplug by remember { mutableStateOf(true) }

    // Memoria caché
    var cacheSizeMb by remember { mutableStateOf("142.8 MB") }

    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val syncRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    fun performYouTubeSync() {
        coroutineScope.launch {
            isSyncing = true
            syncStatusText = "Sincronizando listas y favoritos de YouTube..."
            delay(1500)
            isSyncing = false
            lastSyncTimestamp = "Hace unos segundos"
            syncStatusText = "Sincronización exitosa con YouTube"
            Toast.makeText(context, "Cuenta de YouTube sincronizada: 18 playlists actualizadas", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 130.dp)
    ) {
        // Cabecera superior
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(12.dp, CircleShape, ambientColor = NeonPurple, spotColor = NeonPurple)
                            .clip(CircleShape)
                            .border(1.8.dp, NeonPurple, CircleShape)
                            .testTag("settings_avatar_alex"),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_avatar_alex),
                            contentDescription = "Alex Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column {
                        Text(
                            text = "Ajustes & Cuenta",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = TextWhite
                        )
                        Text(
                            text = "YouTube Sync • Configuración",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = NeonPurpleLight
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GlassSurfaceUltraDark)
                        .border(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.25f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configuración",
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Tarjeta Principal: Sincronización de Cuenta YouTube
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .shadow(18.dp, RoundedCornerShape(24.dp), ambientColor = Color.Black, spotColor = NeonPurpleGlow)
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        1.2.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color(0x99A855F7),
                                Color(0x33FF26C2)
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xF2160630),
                                Color(0xE60E0322)
                            )
                        )
                    )
                    .padding(18.dp)
                    .testTag("youtube_account_sync_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Icono estilizado de YouTube
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE50914))
                                    .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = Color.Red, spotColor = Color.Red),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "YT",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp
                                    ),
                                    color = Color.White
                                )
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Alex Martinez",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        color = TextWhite
                                    )
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Vinculado",
                                        tint = NeonPurpleLight,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Text(
                                    text = "alex.music@gmail.com",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = TextMuted
                                )
                            }
                        }

                        // Estado de sincronización
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x33A855F7))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Activo",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = NeonPurpleLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Estadísticas de sincronización en la nube
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SyncMetricBox(
                            title = "Playlists",
                            value = "18",
                            modifier = Modifier.weight(1f)
                        )
                        SyncMetricBox(
                            title = "Me Gusta",
                            value = "142",
                            modifier = Modifier.weight(1f)
                        )
                        SyncMetricBox(
                            title = "Suscripciones",
                            value = "45",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botón Sincronizar Ahora
                    Button(
                        onClick = { if (!isSyncing) performYouTubeSync() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFA855F7),
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_sync_youtube")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sincronizar",
                                modifier = Modifier
                                    .size(18.dp)
                                    .then(if (isSyncing) Modifier.rotate(syncRotation) else Modifier)
                            )
                            Text(
                                text = if (isSyncing) "Sincronizando biblioteca..." else "Sincronizar con YouTube",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Última sincronización: $lastSyncTimestamp",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextMuted,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        // Sección: Ajustes de Sincronización en Segundo Plano
        item {
            SettingsSectionHeader(title = "Opciones de Sincronización")
        }

        item {
            SettingsCard {
                SettingsSwitchRow(
                    icon = Icons.Default.CloudSync,
                    title = "Sincronización Automática",
                    subtitle = "Actualizar listas de reproducción al abrir la app",
                    checked = autoSyncPlaylists,
                    onCheckedChange = { autoSyncPlaylists = it },
                    testTag = "switch_auto_sync"
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsSwitchRow(
                    icon = Icons.Default.Wifi,
                    title = "Solo con Conexión Wi-Fi",
                    subtitle = "Evitar consumo de datos móviles en sincronización",
                    checked = syncWifiOnly,
                    onCheckedChange = { syncWifiOnly = it },
                    testTag = "switch_wifi_only"
                )
            }
        }

        // Sección: Calidad de Audio & Streaming
        item {
            SettingsSectionHeader(title = "Calidad de Streaming de Audio")
        }

        item {
            SettingsCard {
                Text(
                    text = "Tasa de Bits (Bitrate)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(8.dp))

                val qualities = listOf(
                    "Ultra (320 kbps)",
                    "Alta (256 kbps)",
                    "Ahorro (128 kbps)"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    qualities.forEachIndexed { index, quality ->
                        val isSelected = selectedAudioQualityIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(
                                                        Color(0xFFB042FF),
                                                        Color(0xFF7E22CE)
                                                    )
                                                )
                                            )
                                            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                    } else {
                                        Modifier
                                            .background(Color(0x33000000))
                                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                                    }
                                )
                                .clickable { selectedAudioQualityIndex = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = quality,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = if (isSelected) TextWhite else TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Sección: Motor de Reproducción & Comportamiento
        item {
            SettingsSectionHeader(title = "Motor de Reproducción")
        }

        item {
            SettingsCard {
                SettingsSwitchRow(
                    icon = Icons.Default.VolumeUp,
                    title = "Normalización de Volumen",
                    subtitle = "Ajustar volumen constante entre canciones dispares",
                    checked = normalizeVolume,
                    onCheckedChange = { normalizeVolume = it },
                    testTag = "switch_normalize"
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsSwitchRow(
                    icon = Icons.Default.Speed,
                    title = "Reproducción Sin Pausas (Gapless)",
                    subtitle = "Transición suave y sin silencios entre pistas",
                    checked = gaplessPlayback,
                    onCheckedChange = { gaplessPlayback = it },
                    testTag = "switch_gapless"
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsSwitchRow(
                    icon = Icons.Default.Headphones,
                    title = "Pausa al Desconectar Auriculares",
                    subtitle = "Pausar inmediatamente si se desconectan audífonos o Bluetooth",
                    checked = pauseOnUnplug,
                    onCheckedChange = { pauseOnUnplug = it },
                    testTag = "switch_pause_unplug"
                )
            }
        }

        // Sección: Almacenamiento & Mantenimiento
        item {
            SettingsSectionHeader(title = "Almacenamiento & Caché")
        }

        item {
            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Caché de Canciones y Carátulas",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = TextWhite
                        )
                        Text(
                            text = "$cacheSizeMb utilizados",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = TextMuted
                        )
                    }

                    Button(
                        onClick = {
                            cacheSizeMb = "0.0 MB"
                            Toast.makeText(context, "Caché temporal liberada con éxito", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x33FFFFFF),
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(text = "Limpiar", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Historial de Búsquedas",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = TextWhite
                        )
                        Text(
                            text = "Búsquedas recientes guardadas en Room",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = TextMuted
                        )
                    }

                    Button(
                        onClick = {
                            playerState.clearSearchHistory()
                            Toast.makeText(context, "Historial de búsquedas eliminado", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x33FF26C2),
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(text = "Borrar", fontSize = 12.sp)
                    }
                }
            }
        }

        // Sección: Información de la Aplicación
        item {
            SettingsSectionHeader(title = "Acerca de la App")
        }

        item {
            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Distrito Music Player",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = TextWhite
                        )
                        Text(
                            text = "Versión 2.5.0 Pro • Edición Liquid Glass",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = TextMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x33A855F7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "PRO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            ),
                            color = NeonPurpleLight
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        ),
        color = Color(0xFFDDD2F3),
        modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xE6130528))
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.03f)
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x33A855F7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NeonPurpleLight,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = TextWhite
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextMuted
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextWhite,
                checkedTrackColor = NeonPurple,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = GlassSurfaceUltraDark
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun SyncMetricBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x33000000))
            .border(
                1.dp,
                Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(14.dp)
            )
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextMuted
            )
        }
    }
}
