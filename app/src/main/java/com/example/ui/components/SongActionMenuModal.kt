package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Song
import com.example.ui.theme.LocalDistritoTheme

/**
 * Menú contextual flotante Liquid Glass activado al pulsar los tres puntos (⋮) de cualquier canción.
 * Contiene las acciones exactas:
 * - Reproducir ahora
 * - Reproducir a continuación (Play Next)
 * - Agregar a la cola (Add to Queue)
 * - Favorito / Quitar de favorito
 * - Stats for Nerds (Inspector de Audio HUD)
 * - Ecualizador DSP 503
 * - Temporizador de apagado
 * - Ver detalles técnicos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongActionMenuModal(
    visible: Boolean,
    song: Song?,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onPlayNow: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenStatsForNerds: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible || song == null) return

    val theme = LocalDistritoTheme.current
    var showDetailsDialog by remember { mutableStateOf(false) }

    val glassBorderBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.28f),
            Color.White.copy(alpha = 0.08f)
        )
    )

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .fillMaxWidth(0.94f)
            .testTag("song_action_menu_modal")
    ) {
        LiquidGlassModalFrame(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 28.dp,
            accentGlow = theme.primaryAccent.copy(alpha = 0.4f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Header: Portada, Título, Artista y botón cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.2.dp, Brush.verticalGradient(listOf(Color.White.copy(0.4f), Color.White.copy(0.1f))), RoundedCornerShape(14.dp))
                    ) {
                        SongArtworkImage(
                            albumArtUri = song.albumArtUri,
                            coverResId = song.coverResId,
                            contentDescription = song.title,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            color = theme.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${song.artist} • ${song.album}",
                            color = theme.textSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                theme.primaryAccent.copy(alpha = 0.25f),
                                                theme.secondaryAccent.copy(alpha = 0.15f)
                                            )
                                        )
                                    )
                                    .border(
                                        1.dp,
                                        Brush.verticalGradient(
                                            listOf(Color.White.copy(alpha = 0.35f), theme.primaryAccent.copy(alpha = 0.3f))
                                        ),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "DISTRITO 503 • LIQUID GLASS",
                                    color = theme.primaryAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    LiquidGlassIconButton(
                        onClick = onDismiss,
                        icon = Icons.Default.Close,
                        contentDescription = "Cerrar menú",
                        size = 36.dp,
                        iconSize = 18.dp,
                        tint = theme.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                LiquidGlassDivider(accentGlow = theme.primaryAccent)
                Spacer(modifier = Modifier.height(8.dp))

            // Acciones principales
            ActionMenuItem(
                icon = Icons.Default.PlayArrow,
                title = "Reproducir ahora",
                subtitle = "Inicia la reproducción inmediatamente",
                onClick = {
                    onPlayNow(song)
                    onDismiss()
                }
            )

            ActionMenuItem(
                icon = Icons.Default.SkipNext,
                title = "Reproducir a continuación",
                subtitle = "Se reproducirá justo después de la pista actual",
                onClick = {
                    onPlayNext(song)
                    onDismiss()
                }
            )

            ActionMenuItem(
                icon = Icons.AutoMirrored.Filled.QueueMusic,
                title = "Agregar a la cola",
                subtitle = "Añadir al final de la lista de reproducción actual",
                onClick = {
                    onAddToQueue(song)
                    onDismiss()
                }
            )

            ActionMenuItem(
                icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                title = if (isFavorite) "Quitar de favoritos" else "Guardar en favoritos",
                subtitle = if (isFavorite) "En tu lista de canciones favoritas" else "Toca para agregar a tus preferidas",
                iconTint = if (isFavorite) Color(0xFFFF3366) else theme.primaryAccent,
                onClick = {
                    onToggleFavorite(song.id)
                }
            )

            LiquidGlassDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                accentGlow = theme.primaryAccent
            )

            ActionMenuItem(
                icon = Icons.Default.DeveloperBoard,
                title = "Stats for Nerds (Inspector de Audio)",
                subtitle = "Telemetría técnica, códec, tasa de muestreo y DSP",
                onClick = {
                    onDismiss()
                    onOpenStatsForNerds()
                }
            )

            ActionMenuItem(
                icon = Icons.Default.Equalizer,
                title = "Ecualizador DSP 503",
                subtitle = "Ajustar bandas, Bass Boost y Virtualizador 3D",
                onClick = {
                    onDismiss()
                    onOpenEqualizer()
                }
            )

            ActionMenuItem(
                icon = Icons.Default.Bedtime,
                title = "Temporizador de apagado",
                subtitle = "Pausar música automáticamente con desvanecimiento",
                onClick = {
                    onDismiss()
                    onOpenSleepTimer()
                }
            )

            ActionMenuItem(
                icon = Icons.Default.Info,
                title = "Detalles técnicos",
                subtitle = "Duración: %d:%02d • %s".format(
                    song.durationSeconds / 60,
                    song.durationSeconds % 60,
                    if (song.folderName.isNotBlank()) song.folderName else "Local"
                ),
                onClick = {
                    showDetailsDialog = !showDetailsDialog
                }
            )

            if (showDetailsDialog) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(0.25f), Color.White.copy(0.05f))), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        DetailTextRow("Archivo:", song.filePath.ifBlank { "Almacenamiento del teléfono" })
                        DetailTextRow("Carpeta:", song.folderPath.ifBlank { song.folderName })
                        DetailTextRow("Duración:", "${song.durationSeconds} segundos")
                        DetailTextRow("Año:", if (song.year > 0) "${song.year}" else "No disponible")
                    }
                }
            }
        }
    }
}
}

@Composable
private fun ActionMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color? = null,
    onClick: () -> Unit
) {
    val theme = LocalDistritoTheme.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.04f))
                ),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.14f), Color.White.copy(alpha = 0.06f))
                    )
                )
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.08f))
                    ),
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint ?: theme.primaryAccent,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = theme.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = theme.textSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DetailTextRow(label: String, value: String) {
    val theme = LocalDistritoTheme.current
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = theme.primaryAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = value,
            color = theme.textPrimary,
            fontSize = 11.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
