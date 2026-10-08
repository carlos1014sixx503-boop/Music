package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playback.AudioStats
import com.example.ui.theme.LocalDistritoTheme

/**
 * Modal técnico de estadísticas avanzadas "Stats for Nerds" inspirado en BitChord / YouTube Music,
 * diseñado con la estética visual Liquid Glass de Distrito Music 503.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsForNerdsModal(
    visible: Boolean,
    stats: AudioStats,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    val theme = LocalDistritoTheme.current

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .fillMaxWidth(0.95f)
            .testTag("stats_for_nerds_modal")
    ) {
        LiquidGlassModalFrame(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp,
            accentGlow = theme.primaryAccent.copy(alpha = 0.45f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Cabecera BitChord Style
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(theme.primaryAccent.copy(alpha = 0.2f))
                                .border(1.dp, theme.primaryAccent.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeveloperBoard,
                                contentDescription = null,
                                tint = theme.primaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "STATS FOR NERDS 🤓",
                                color = theme.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Pipeline Técnico de Audio • BitChord Spec",
                                color = theme.primaryAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    LiquidGlassIconButton(
                        onClick = onDismiss,
                        icon = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        size = 32.dp,
                        iconSize = 16.dp,
                        tint = theme.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                LiquidGlassDivider(accentGlow = theme.primaryAccent)
                Spacer(modifier = Modifier.height(12.dp))

            // Tarjeta de la pista actual
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, theme.surfaceBorderColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = stats.title,
                        color = theme.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${stats.artist} • ${stats.album}",
                        color = theme.textSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lista scrolleable de métricas técnicas detalladas
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Métricas de Códec y Formato
                item {
                    MetricRowItem(
                        icon = Icons.Default.GraphicEq,
                        label = "Códec & Contenedor",
                        value = "${stats.codec} (${stats.containerFormat})",
                        accentColor = theme.primaryAccent
                    )
                }

                item {
                    MetricRowItem(
                        icon = Icons.Default.Memory,
                        label = "Resolución & Muestreo",
                        value = "${stats.formattedBitDepth} • ${stats.formattedSampleRate}",
                        accentColor = theme.secondaryAccent
                    )
                }

                item {
                    MetricRowItem(
                        icon = Icons.Default.Speed,
                        label = "Tasa de Bits (Bitrate)",
                        value = stats.formattedBitrate,
                        accentColor = theme.primaryAccent
                    )
                }

                item {
                    MetricRowItem(
                        icon = Icons.Default.VolumeUp,
                        label = "Canales de Salida",
                        value = stats.formattedChannels,
                        accentColor = theme.secondaryAccent
                    )
                }

                item {
                    MetricRowItem(
                        icon = Icons.Default.DeveloperBoard,
                        label = "Audio Session ID (Hardware)",
                        value = if (stats.audioSessionId > 0) "#${stats.audioSessionId} (ExoPlayer DSP)" else "Session Compartida",
                        accentColor = theme.primaryAccent
                    )
                }

                item {
                    MetricRowItem(
                        icon = Icons.Default.Equalizer,
                        label = "Motor DSP 503",
                        value = if (stats.isDspActive) "Activo: ${stats.equalizerPreset} • Bass +${stats.bassBoostPercent}%" else "Bypass (Desactivado)",
                        accentColor = if (stats.isDspActive) Color(0xFF00E676) else theme.textMuted
                    )
                }

                item {
                    MetricRowItem(
                        icon = Icons.Default.Speed,
                        label = "Velocidad de Reproducción",
                        value = "%.2fx Tempo".format(stats.playbackSpeed),
                        accentColor = theme.primaryAccent
                    )
                }

                // Buffer Health
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.35f))
                            .border(1.dp, theme.surfaceBorderColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Salud de Buffer (Caché)",
                                    color = theme.textSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${stats.bufferHealthPercent}%",
                                    color = theme.primaryAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (stats.bufferHealthPercent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = theme.primaryAccent,
                                trackColor = theme.surfaceBorderColor.copy(alpha = 0.3f)
                            )
                        }
                    }
                }

                item {
                    MetricRowItem(
                        icon = Icons.Default.Storage,
                        label = "Tamaño de Archivo",
                        value = stats.formattedFileSize,
                        accentColor = theme.textSecondary
                    )
                }

                item {
                    MetricRowItem(
                        icon = Icons.Default.Info,
                        label = "Ruta de Audio",
                        value = stats.sourcePath.substringAfterLast('/'),
                        accentColor = theme.textMuted
                    )
                }
            }
        }
    }
}
}

@Composable
private fun MetricRowItem(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color
) {
    val theme = LocalDistritoTheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .border(1.dp, theme.surfaceBorderColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = theme.textSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = value,
            color = theme.textPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
