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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.youtube.YouTubePlaybackMode
import com.example.data.youtube.YouTubeVideo
import com.example.ui.theme.LocalDistritoTheme

@Composable
fun YouTubeModeSelectionDialog(
    video: YouTubeVideo?,
    onSelectMode: (YouTubePlaybackMode) -> Unit,
    onDismiss: () -> Unit
) {
    if (video == null) return
    val theme = LocalDistritoTheme.current

    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassModalFrame(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("youtube_mode_selection_dialog"),
            cornerRadius = 24.dp,
            accentGlow = Color(0xFFFF334B).copy(alpha = 0.45f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Modo de Reproducción",
                            color = theme.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Selecciona cómo deseas disfrutar este contenido",
                            color = theme.textMuted,
                            fontSize = 12.sp
                        )
                    }

                    LiquidGlassIconButton(
                        onClick = onDismiss,
                        icon = Icons.Default.Close,
                        contentDescription = "Cancelar",
                        size = 32.dp,
                        iconSize = 16.dp,
                        tint = theme.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                LiquidGlassDivider(accentGlow = Color(0xFFFF334B))
                Spacer(modifier = Modifier.height(12.dp))

                // Video info snippet
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.surfaceColor)
                        .border(1.dp, theme.surfaceBorderColor, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = video.title,
                            color = theme.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${video.channelTitle} ${if (!video.durationFormatted.isNullOrBlank()) "• ${video.durationFormatted}" else ""}",
                            color = theme.primaryAccent,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Option 1: 🎬 VER VIDEO
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFFF0000).copy(alpha = 0.15f),
                                    theme.surfaceColor.copy(alpha = 0.6f)
                                )
                            )
                        )
                        .border(1.dp, Color(0xFFFF334B).copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                        .clickable { onSelectMode(YouTubePlaybackMode.VIDEO) }
                        .padding(16.dp)
                        .testTag("select_mode_video")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF334B))
                                .shadow(8.dp, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartDisplay,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🎬 VER VIDEO",
                                    color = Color(0xFFFF6677),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Reproduce el video completo con imagen, sonido y controles integrados.",
                                color = theme.textSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option 2: 🎧 SOLO AUDIO
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    theme.primaryAccent.copy(alpha = 0.15f),
                                    theme.surfaceColor.copy(alpha = 0.6f)
                                )
                            )
                        )
                        .border(1.dp, theme.primaryAccent.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                        .clickable { onSelectMode(YouTubePlaybackMode.AUDIO_ONLY) }
                        .padding(16.dp)
                        .testTag("select_mode_audio_only")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(theme.primaryAccent)
                                .shadow(8.dp, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🎧 SOLO AUDIO",
                                    color = theme.primaryAccent,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Interfaz de reproductor musical con carátula, ecualizador y minimizable para navegar.",
                                color = theme.textSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Nota: Puedes alternar entre Video y Audio en cualquier momento desde el reproductor.",
                    color = theme.textMuted,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
