package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlin.math.sin

/**
 * Visualizador interactivo de forma de onda (Waveform) inspirado fielmente en la referencia visual:
 * - Barras verticales con silueta de onda sonora armónica.
 * - Animación sutil y fluida cuando la reproducción está activa.
 * - Progreso iluminado (blanco puro / acento neón) y restante tenue.
 * - Desplazamiento táctil (arrastrar o tocar) para avanzar / retroceder en la pista.
 * - Marcadores de tiempo transcurrido y duración total.
 */
@Composable
fun AnimatedWaveformProgressBar(
    currentPositionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    barCount: Int = 42
) {
    val effectiveDuration = durationMs.coerceAtLeast(1000L)
    val progress = (currentPositionMs.toFloat() / effectiveDuration).coerceIn(0f, 1f)

    // Animated phase for subtle dancing waves when playing
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformPulse")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulsePhase"
    )

    // Pre-computed normalized height variations for realistic waveform silhouette
    val baseHeights = remember(barCount) {
        val list = FloatArray(barCount)
        for (i in 0 until barCount) {
            val normalizedX = i.toFloat() / barCount
            // Harmonic wave formula producing organic peak-and-valley audio shapes
            val primary = sin(normalizedX * Math.PI.toFloat())
            val harmonic1 = 0.35f * sin(normalizedX * 12.0f + 0.4f)
            val harmonic2 = 0.20f * sin(normalizedX * 24.0f + 1.2f)
            val raw = (primary + harmonic1 + harmonic2).coerceIn(0.18f, 1.0f)
            list[i] = raw
        }
        list
    }

    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val activeProgress = if (isDragging) dragProgress else progress

    Column(
        modifier = modifier.testTag("animated_waveform_progress_bar")
    ) {
        // Waveform Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .pointerInput(effectiveDuration) {
                    detectTapGestures { offset ->
                        val tappedRatio = (offset.x / size.width).coerceIn(0f, 1f)
                        val targetMs = (tappedRatio * effectiveDuration).toLong()
                        onSeek(targetMs)
                    }
                }
                .pointerInput(effectiveDuration) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            isDragging = false
                            val targetMs = (dragProgress * effectiveDuration).toLong()
                            onSeek(targetMs)
                        },
                        onDragCancel = {
                            isDragging = false
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val spacing = 3.dp.toPx()
            val totalSpacing = spacing * (barCount - 1)
            val barWidth = ((width - totalSpacing) / barCount).coerceAtLeast(2.dp.toPx())
            val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

            for (i in 0 until barCount) {
                val barProgressRatio = i.toFloat() / barCount
                val isPlayed = barProgressRatio <= activeProgress

                // Height calculation with animated modulation when playing
                val baseH = baseHeights[i]
                val dynamicH = if (isPlaying && isPlayed) {
                    val modulation = 0.12f * sin(pulsePhase + i * 0.45f)
                    (baseH + modulation).coerceIn(0.20f, 1.0f)
                } else {
                    baseH
                }

                val barHeight = (height * dynamicH).coerceAtLeast(6.dp.toPx())
                val x = i * (barWidth + spacing)
                val y = (height - barHeight) / 2f

                val barColor = when {
                    isPlayed -> TextWhite
                    else -> Color.White.copy(alpha = 0.22f)
                }

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = cornerRadius
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Time labels row (Elapsed on left, Total on right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val displayCurrentMs = if (isDragging) {
                (dragProgress * effectiveDuration).toLong()
            } else {
                currentPositionMs
            }

            Text(
                text = formatMs(displayCurrentMs),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp
                ),
                color = TextMuted,
                modifier = Modifier.testTag("waveform_elapsed_time")
            )

            Text(
                text = formatMs(effectiveDuration),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp
                ),
                color = TextMuted,
                modifier = Modifier.testTag("waveform_total_time")
            )
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
