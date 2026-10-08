package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Song
import com.example.ui.theme.LocalDistritoTheme

/**
 * Componente 'Mini Player' flotante Liquid Glass situado en la parte inferior de la pantalla.
 *
 * Características:
 * - Implementa el estilo base 'Liquid Glass' con fondo desenfocado (blur) y baja opacidad.
 * - Muestra la carátula, el título de la pista actual, artista y badge de formato de audio.
 * - Indicador de ecualizador animado en tiempo real cuando se está reproduciendo.
 * - Controles táctiles accesibles (tamaño táctil mínimo de 48dp) para Play/Pause y Siguiente canción.
 * - Barra de progreso interactiva y estilizada en la base con degradado neón.
 * - Tocar el reproductor abre la vista completa (FullScreenPlayer).
 */
@Composable
fun MiniPlayer(
    currentSong: Song?,
    isPlaying: Boolean,
    currentPositionSeconds: Int,
    onTogglePlayPause: () -> Unit,
    onPlayNext: () -> Unit,
    onOpenFullScreen: () -> Unit,
    modifier: Modifier = Modifier,
    onPlayPrevious: (() -> Unit)? = null,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null
) {
    val theme = LocalDistritoTheme.current

    AnimatedVisibility(
        visible = currentSong != null,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier
    ) {
        if (currentSong == null) return@AnimatedVisibility

        val duration = currentSong.durationSeconds.coerceAtLeast(1)
        val progress = (currentPositionSeconds.toFloat() / duration.toFloat()).coerceIn(0f, 1f)

        // Contenedor principal con arquitectura Liquid Glass (desenfoque y baja opacidad)
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp)
                .testTag("mini_player"),
            cornerRadius = 20.dp,
            blurRadius = 18.dp,
            backgroundAlpha = 0.58f,
            borderWidth = 1.2.dp,
            ambientGlow = theme.glowColor.copy(alpha = 0.45f),
            onClick = onOpenFullScreen
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 8.dp, top = 8.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Carátula del álbum con sombra difusa, animación sutil de reproducción y borde fino
                    val miniArtworkScale = if (isPlaying) 1.03f else 1.0f
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer {
                                scaleX = miniArtworkScale
                                scaleY = miniArtworkScale
                            }
                            .shadow(
                                elevation = if (isPlaying) 10.dp else 6.dp,
                                shape = RoundedCornerShape(12.dp),
                                ambientColor = theme.primaryAccent,
                                spotColor = theme.secondaryAccent
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.surfaceVariantColor)
                            .border(
                                1.dp,
                                if (isPlaying) Brush.horizontalGradient(
                                    listOf(theme.primaryAccent, Color(0xFF00E5FF))
                                ) else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.05f))),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        SongArtworkImage(
                            albumArtUri = currentSong.albumArtUri,
                            coverResId = currentSong.coverResId,
                            contentDescription = "Carátula de ${currentSong.album}",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Información de la pista actual (Título, Artista, Ecualizador animado)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = currentSong.title,
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            if (isPlaying) {
                                MiniEqualizerIndicator(tint = theme.primaryAccent)
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = currentSong.artist,
                                color = theme.textSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            // Badge de formato / calidad Hi-Res
                            val formatTag = if (currentSong.audioFormat.isNotBlank()) currentSong.audioFormat else "AUDIO"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(theme.primaryAccent.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = formatTag.uppercase(),
                                    color = theme.primaryAccent,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Controles de reproducción con touch target accesible (>= 48dp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Botón anterior opcional
                        if (onPlayPrevious != null) {
                            IconButton(
                                onClick = onPlayPrevious,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("mini_player_previous")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Canción anterior",
                                    tint = theme.textSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Botón Play / Pause prominente con efecto Liquid Glass y glow
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onTogglePlayPause)
                                .testTag("mini_player_play_pause"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .shadow(8.dp, CircleShape, ambientColor = theme.primaryAccent, spotColor = theme.primaryAccent)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                theme.primaryAccent.copy(alpha = 0.85f),
                                                theme.primaryAccent
                                            )
                                        )
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                                    tint = theme.materialColorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Botón Siguiente Canción
                        IconButton(
                            onClick = onPlayNext,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("mini_player_next")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Siguiente canción",
                                tint = theme.textPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Botón Favorito opcional
                        if (onToggleFavorite != null) {
                            IconButton(
                                onClick = onToggleFavorite,
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("mini_player_favorite")
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (isFavorite) "Quitar de favoritos" else "Guardar en favoritos",
                                    tint = if (isFavorite) Color(0xFFFF3366) else theme.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Indicador de progreso estilizado con degradado multicolor reactivo (Morado -> Magenta -> Cyan)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.5.dp)
                        .background(Color.White.copy(alpha = 0.08f))
                        .testTag("mini_player_progress")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progress)
                            .height(3.5.dp)
                            .shadow(
                                elevation = if (isPlaying) 6.dp else 2.dp,
                                ambientColor = theme.primaryAccent,
                                spotColor = Color(0xFF00E5FF)
                            )
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF7C3AED), // Morado eléctrico
                                        Color(0xFFC026D3), // Fucsia brillante
                                        Color(0xFF38BDF8), // Azul cyan
                                        Color(0xFF00E5FF)  // Aqua neón
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}

/**
 * Ecualizador animado que oscila en tiempo real mientras el audio está reproduciéndose.
 */
@Composable
fun MiniEqualizerIndicator(tint: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "MiniEqualizerAnim")

    val h1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq_bar1"
    )

    val h2 by infiniteTransition.animateFloat(
        initialValue = 13f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(360, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq_bar2"
    )

    val h3 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq_bar3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(14.dp)
    ) {
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(h1.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(tint)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(h2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(tint)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(h3.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(tint)
        )
    }
}
