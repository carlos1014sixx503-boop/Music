package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.Song
import com.example.ui.theme.LocalDistritoTheme
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Vista de Reproductor 3D / Vinilo Giratorio Interactivo inspirado en el diseño Stitch.
 * Ofrece:
 * - Disco de vinilo hiperrealista con surcos concéntricos y reflejos de luz especular a 45°.
 * - Giro continuo y suave sincronizado con el estado de reproducción (`isPlaying`).
 * - Brazo / aguja (tonearm) metálico con pivote animado que se posa al reproducir y se levanta al pausar.
 * - Anillo espectral visualizador de audio alrededor del vinilo con ondas reactivas de neón.
 * - Soporte de interacción táctil (scratching / arrastre giratorio manual).
 */
@Composable
fun VinylPlayer3DView(
    currentSong: Song?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val theme = LocalDistritoTheme.current

    // Ángulo de rotación continuo
    var manualDragOffset by remember { mutableFloatStateOf(0f) }
    val rotationAnim = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                rotationAnim.animateTo(
                    targetValue = rotationAnim.value + 360f,
                    animationSpec = tween(durationMillis = 4000, easing = LinearEasing)
                )
            }
        }
    }

    // Ángulo del brazo fonocaptor (tonearm)
    val tonearmAngle by animateFloatAsState(
        targetValue = if (isPlaying) 28f else 0f,
        animationSpec = tween(durationMillis = 650),
        label = "TonearmAngle"
    )

    // Pulsación continua para ondas reactivas del visualizador
    val infiniteTransition = rememberInfiniteTransition(label = "VinylWaveTransition")
    val wavePulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WavePulse"
    )

    val currentRotation = (rotationAnim.value + manualDragOffset) % 360f

    Box(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(32.dp))
            .background(theme.surfaceColor.copy(alpha = 0.45f))
            .border(1.5.dp, theme.surfaceBorderColor.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
            .padding(16.dp)
            .testTag("vinyl_player_3d_container"),
        contentAlignment = Alignment.Center
    ) {
        // 1. Anillo visualizador de espectro de audio (ondas de neón pulsantes)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = (size.minDimension / 2f) * 0.96f
            val baseRadius = outerRadius * 0.88f

            if (isPlaying) {
                // 48 rayos / barras espectrales reactivas
                val rayCount = 48
                for (i in 0 until rayCount) {
                    val angleRad = (i.toFloat() / rayCount) * 2f * PI.toFloat() + (currentRotation * PI.toFloat() / 180f)
                    val freqFactor = ((sin(i * 0.8f + currentRotation * 0.05f) + 1f) / 2f) * (wavePulse - 0.7f)
                    val rayLength = 6f + freqFactor * 22f

                    val startX = center.x + cos(angleRad) * (baseRadius + 6f)
                    val startY = center.y + sin(angleRad) * (baseRadius + 6f)
                    val endX = center.x + cos(angleRad) * (baseRadius + 6f + rayLength)
                    val endY = center.y + sin(angleRad) * (baseRadius + 6f + rayLength)

                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                theme.primaryAccent.copy(alpha = 0.85f),
                                theme.secondaryAccent.copy(alpha = 0.3f)
                            )
                        ),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 3.5f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // 2. Disco de vinilo giratorio con surcos y detección de gesto de arrastre
        Box(
            modifier = Modifier
                .fillMaxSize(0.88f)
                .aspectRatio(1f)
                .shadow(elevation = 20.dp, shape = CircleShape, ambientColor = theme.glowColor, spotColor = Color.Black)
                .clip(CircleShape)
                .pointerInput(Unit) {
                    var lastAngle = 0f
                    detectDragGestures(
                        onDragStart = { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val diff = offset - center
                            lastAngle = atan2(diff.y, diff.x) * 180f / PI.toFloat()
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val diff = change.position - center
                            val currentAngle = atan2(diff.y, diff.x) * 180f / PI.toFloat()
                            val delta = currentAngle - lastAngle
                            manualDragOffset += delta
                            lastAngle = currentAngle
                        }
                    )
                }
                .rotate(currentRotation),
            contentAlignment = Alignment.Center
        ) {
            // Fondo del vinilo con surcos concéntricos
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f

                // Base negra profunda de acetato
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1C1C22),
                            Color(0xFF101014),
                            Color(0xFF09090C),
                            Color(0xFF000000)
                        ),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )

                // Reflejos especulares de luz en X (estilo vinilo clásico)
                rotate(degrees = 45f, pivot = center) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius
                        ),
                        size = size
                    )
                }
                rotate(degrees = -45f, pivot = center) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.06f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius
                        ),
                        size = size
                    )
                }

                // Micro-surcos concéntricos
                val startR = radius * 0.42f
                val endR = radius * 0.94f
                val step = 5f
                var currR = startR
                while (currR < endR) {
                    val alpha = if ((currR / step).toInt() % 4 == 0) 0.16f else 0.08f
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = currR,
                        center = center,
                        style = Stroke(width = 1f)
                    )
                    currR += step
                }

                // Borde exterior biselado
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            theme.primaryAccent.copy(alpha = 0.4f),
                            Color(0xFF333340),
                            theme.secondaryAccent.copy(alpha = 0.4f),
                            Color(0xFF1A1A22),
                            theme.primaryAccent.copy(alpha = 0.4f)
                        ),
                        center = center
                    ),
                    radius = radius - 1f,
                    center = center,
                    style = Stroke(width = 2.5f)
                )
            }

            // 3. Etiqueta central con la portada del álbum y orificio del eje
            Box(
                modifier = Modifier
                    .fillMaxSize(0.40f)
                    .clip(CircleShape)
                    .border(3.dp, Color(0xFF1E1E26), CircleShape)
                    .border(1.5.dp, theme.primaryAccent.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                SongArtworkImage(
                    albumArtUri = currentSong?.albumArtUri,
                    coverResId = currentSong?.coverResId,
                    contentDescription = currentSong?.title ?: "Vinilo 503",
                    modifier = Modifier.fillMaxSize()
                )

                // Orificio central del eje con acabado metálico
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF111116))
                        .border(2.dp, Color(0xFFCCCCCC), CircleShape)
                )
            }
        }

        // 4. Brazo fonocaptor / Tonearm metálico interactivo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp, end = 8.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .size(width = 110.dp, height = 170.dp)
                    .align(Alignment.TopEnd)
            ) {
                val pivot = Offset(size.width - 24f, 24f)

                // Base / rodamiento del brazo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFEEEEEE), Color(0xFF888899), Color(0xFF333340)),
                        center = pivot,
                        radius = 20f
                    ),
                    radius = 20f,
                    center = pivot
                )
                drawCircle(
                    color = theme.primaryAccent,
                    radius = 6f,
                    center = pivot
                )

                // Rotar brazo según ángulo animado
                rotate(degrees = tonearmAngle, pivot = pivot) {
                    val elbow = Offset(pivot.x - 38f, pivot.y + 80f)
                    val cartridge = Offset(elbow.x - 28f, elbow.y + 55f)

                    // Tubo del brazo (metálico cromado)
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFFFFFF), Color(0xFFAAAAAA), Color(0xFF666677))
                        ),
                        start = pivot,
                        end = elbow,
                        strokeWidth = 6.5f,
                        cap = StrokeCap.Round
                    )

                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFAAAAAA), Color(0xFFEEEEEE), Color(0xFF555566))
                        ),
                        start = elbow,
                        end = cartridge,
                        strokeWidth = 5.5f,
                        cap = StrokeCap.Round
                    )

                    // Cabezal / Cápsula fonocaptora
                    drawCircle(
                        color = theme.primaryAccent,
                        radius = 7.5f,
                        center = cartridge
                    )

                    // Aguja con brillo
                    drawLine(
                        color = Color.White,
                        start = cartridge,
                        end = Offset(cartridge.x - 6f, cartridge.y + 8f),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
