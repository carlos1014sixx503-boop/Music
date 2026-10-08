package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.lyrics.SongLyrics
import com.example.ui.theme.LocalDistritoTheme

/**
 * Vista de letras sincronizadas con desplazamiento automático, iluminación karaoke
 * y salto táctil de compás (inspirada en la experiencia de BitChord y Apple Music).
 */
@Composable
fun SyncedLyricsView(
    lyrics: SongLyrics?,
    currentPositionSeconds: Int,
    onSeekTo: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalDistritoTheme.current
    val listState = rememberLazyListState()

    val lines = lyrics?.lines ?: emptyList()

    // Encontrar el índice de la línea activa actualmente
    val activeIndex by remember(lines, currentPositionSeconds) {
        derivedStateOf {
            val idx = lines.indexOfLast { it.timestampSeconds <= currentPositionSeconds }
            if (idx == -1 && lines.isNotEmpty()) 0 else idx
        }
    }

    // Auto-scroll suave para centrar la línea activa como en Apple Music / BitChord
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0 && lines.isNotEmpty()) {
            val targetScroll = (activeIndex - 1).coerceAtLeast(0)
            listState.animateScrollToItem(targetScroll)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(theme.surfaceColor.copy(alpha = 0.55f))
            .border(1.dp, theme.surfaceBorderColor.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .padding(vertical = 12.dp)
            .testTag("synced_lyrics_view")
    ) {
        if (lines.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = theme.primaryAccent.copy(alpha = 0.6f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Letras instrumentales",
                    color = theme.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Disfruta de la producción en alta definición de Distrito Music 503",
                    color = theme.textSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header badge informativo
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(theme.primaryAccent.copy(alpha = 0.15f))
                                .border(1.dp, theme.primaryAccent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = theme.primaryAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LETRAS SINCRONIZADAS 503 • TOCA PARA SALTAR",
                                    color = theme.primaryAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                itemsIndexed(lines) { index, line ->
                    val isActive = index == activeIndex
                    val isPast = index < activeIndex

                    val animatedAlpha by animateFloatAsState(
                        targetValue = when {
                            isActive -> 1.0f
                            isPast -> 0.65f
                            else -> 0.40f
                        },
                        animationSpec = tween(durationMillis = 300),
                        label = "lyric_alpha"
                    )

                    val textColor by animateColorAsState(
                        targetValue = when {
                            isActive -> theme.primaryAccent
                            isPast -> theme.textPrimary
                            else -> theme.textSecondary
                        },
                        animationSpec = tween(durationMillis = 300),
                        label = "lyric_color"
                    )

                    val fontSize = if (isActive) 21.sp else 16.sp
                    val fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isActive) theme.primaryAccent.copy(alpha = 0.12f)
                                else Color.Transparent
                            )
                            .border(
                                width = if (isActive) 1.dp else 0.dp,
                                color = if (isActive) theme.primaryAccent.copy(alpha = 0.35f) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                onSeekTo(line.timestampSeconds)
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .alpha(animatedAlpha)
                            .testTag("lyric_line_$index")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = line.text,
                                    color = textColor,
                                    fontSize = fontSize,
                                    fontWeight = fontWeight,
                                    lineHeight = (fontSize.value * 1.35).sp
                                )
                                if (line.translation != null) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = line.translation,
                                        color = theme.textMuted,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }

                            if (isActive) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(theme.primaryAccent)
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Posición actual",
                                        tint = theme.materialColorScheme.onPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = line.formattedTime,
                                    color = theme.textMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Footer de fuente
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Fuente: ${lyrics?.source ?: "Distrito 503 Sync Engine"}",
                        color = theme.textMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
