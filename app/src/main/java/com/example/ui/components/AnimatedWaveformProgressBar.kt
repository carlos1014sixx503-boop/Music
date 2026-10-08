package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Estilos visuales de barra de progreso interactiva basados en la colección vectorial
 * de ondas y ecualizadores de audio (iStock 1289638909) con estética Liquid Glass:
 * - NEON_EQUALIZER_BARS: Barras HUD simétricas con cápsulas de cristal, glow de neón y llenado dinámico.
 * - LIQUID_GLASS_WAVE: Onda sinusal continua líquida y translúcida con brillo especular y reflejos acuáticos.
 * - FREQUENCY_PULSE: Picos de frecuencia de estudio y ecualizador espectral activo con destello pulsante.
 */
enum class WaveformProgressStyle {
    NEON_EQUALIZER_BARS,
    LIQUID_GLASS_WAVE,
    FREQUENCY_PULSE
}

/**
 * Barra de progreso / visualizador de audio con Canvas API y estética Liquid Glass para Distrito Music 503.
 *
 * Características:
 * - Detección táctil (Tap & Drag Seek) de alta precisión para avanzar/retroceder en la pista.
 * - A medida que avanza la canción se llena progresivamente con degradados vibrantes
 *   (Morado Neón 0xFF7C3AED -> Magenta 0xFFC026D3 -> Cyan 0xFF00E5FF).
 * - Animación en vivo cuando `isPlaying = true`: pulsación orgánica, modulación de frecuencias y reflejo líquido.
 * - Estética "Liquid Glass": cápsulas vítreas translúcidas con luz especular blanca en la parte superior,
 *   borde bioluminiscente y halo de dispersión de luz.
 */
@Composable
fun AnimatedWaveformProgressBar(
    progress: Float, // 0f .. 1f
    isPlaying: Boolean,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    style: WaveformProgressStyle = WaveformProgressStyle.NEON_EQUALIZER_BARS
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(progress) }

    val effectiveProgress = (if (isDragging) dragProgress else progress).coerceIn(0f, 1f)

    // Animación continua para modulación de audio y flujo de luz
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidWaveformTransition")

    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulsePhase"
    )

    val secondaryPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SecondaryPulse"
    )

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulse"
    )

    val glassShimmerX by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GlassShimmer"
    )

    // Paleta Liquid Glass Neón (Violeta Eléctrico, Fucsia Neón, Aqua Glacial)
    val liquidViolet = remember { Color(0xFF7C3AED) }
    val liquidMagenta = remember { Color(0xFFD946EF) }
    val liquidCyan = remember { Color(0xFF00E5FF) }
    val liquidSkyBlue = remember { Color(0xFF38BDF8) }

    val inactiveGlassFill = remember { Color(0xFF1E293B).copy(alpha = 0.35f) }
    val inactiveGlassBorder = remember { Color(0xFF64748B).copy(alpha = 0.28f) }

    // Amplitudes armónicas tipo ecualizador de audio (44 barras)
    val barAmplitudes = remember {
        floatArrayOf(
            0.20f, 0.32f, 0.48f, 0.36f, 0.68f, 0.88f, 0.56f, 0.40f,
            0.75f, 0.98f, 0.70f, 0.42f, 0.85f, 0.62f, 0.38f, 0.60f,
            0.92f, 0.74f, 0.50f, 0.95f, 1.00f, 0.88f, 0.65f, 0.46f,
            0.80f, 0.94f, 0.60f, 0.38f, 0.72f, 0.85f, 0.56f, 0.40f,
            0.82f, 0.96f, 0.74f, 0.48f, 0.88f, 0.66f, 0.44f, 0.62f,
            0.82f, 0.68f, 0.45f, 0.28f
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek(newProgress)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        isDragging = false
                        onSeek(dragProgress)
                    },
                    onDragCancel = {
                        isDragging = false
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                    }
                )
            }
            .testTag("animated_waveform_progress_bar")
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
            val width = size.width
            val canvasHeight = size.height
            val centerY = canvasHeight / 2f
            val activeX = width * effectiveProgress

            when (style) {
                WaveformProgressStyle.NEON_EQUALIZER_BARS -> {
                    val barCount = barAmplitudes.size
                    val spacingRatio = 0.38f
                    val slotWidth = width / barCount
                    val barWidth = slotWidth * (1f - spacingRatio)
                    val barSpacing = slotWidth * spacingRatio
                    val maxBarHeight = canvasHeight * 0.92f
                    val minBarHeight = 6.dp.toPx()

                    // Pista de fondo de cristal (Liquid Glass Base)
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.04f),
                        topLeft = Offset(0f, centerY - 2.dp.toPx()),
                        size = Size(width, 4.dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )

                    for (i in 0 until barCount) {
                        val baseX = i * (barWidth + barSpacing) + barSpacing / 2f
                        val baseAmp = barAmplitudes[i]

                        // Animación viva de modulación de audio si está en reproducción
                        val animatedAmp = if (isPlaying) {
                            val wave1 = sin(pulsePhase + (i * 0.28f)).toFloat() * 0.22f
                            val wave2 = cos(secondaryPulse + (i * 0.45f)).toFloat() * 0.12f
                            (baseAmp + wave1 + wave2).coerceIn(0.16f, 1.0f)
                        } else {
                            baseAmp
                        }

                        val h = (maxBarHeight * animatedAmp).coerceAtLeast(minBarHeight)
                        val top = centerY - h / 2f
                        val isFilled = (baseX + barWidth / 2f) <= activeX
                        val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

                        if (isFilled) {
                            // Proporción horizontal para degradado general
                            val progressFactor = (baseX / width).coerceIn(0f, 1f)

                            // 1. Halo difuso exterior bioluminiscente (Glow)
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        liquidCyan.copy(alpha = glowPulse * 0.40f),
                                        liquidMagenta.copy(alpha = glowPulse * 0.30f),
                                        liquidViolet.copy(alpha = glowPulse * 0.20f)
                                    ),
                                    startY = top - 2.dp.toPx(),
                                    endY = top + h + 2.dp.toPx()
                                ),
                                topLeft = Offset(baseX - 1.5.dp.toPx(), top - 1.5.dp.toPx()),
                                size = Size(barWidth + 3.dp.toPx(), h + 3.dp.toPx()),
                                cornerRadius = CornerRadius((barWidth + 3.dp.toPx()) / 2f, (barWidth + 3.dp.toPx()) / 2f)
                            )

                            // 2. Relleno principal de la barra con degradado Liquid Neón
                            val barBrush = Brush.verticalGradient(
                                colors = listOf(
                                    liquidCyan,
                                    liquidSkyBlue,
                                    liquidMagenta,
                                    liquidViolet,
                                    liquidMagenta,
                                    liquidCyan
                                ),
                                startY = top,
                                endY = top + h
                            )
                            drawRoundRect(
                                brush = barBrush,
                                topLeft = Offset(baseX, top),
                                size = Size(barWidth, h),
                                cornerRadius = cornerRadius
                            )

                            // 3. Efecto Liquid Glass: Reflejo especular blanco en la parte superior de la cápsula
                            val highlightHeight = (h * 0.32f).coerceAtLeast(3.dp.toPx())
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.75f),
                                        Color.White.copy(alpha = 0.05f)
                                    ),
                                    startY = top,
                                    endY = top + highlightHeight
                                ),
                                topLeft = Offset(baseX + 0.5.dp.toPx(), top + 0.5.dp.toPx()),
                                size = Size(barWidth - 1.dp.toPx(), highlightHeight),
                                cornerRadius = CornerRadius((barWidth - 1.dp.toPx()) / 2f, (barWidth - 1.dp.toPx()) / 2f)
                            )

                            // 4. Borde fino de cristal líquido
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.5f),
                                        liquidCyan.copy(alpha = 0.6f)
                                    )
                                ),
                                topLeft = Offset(baseX, top),
                                size = Size(barWidth, h),
                                cornerRadius = cornerRadius,
                                style = Stroke(width = 0.75.dp.toPx())
                            )
                        } else {
                            // Barra inactiva: cápsula de vidrio translúcido ahumado (Frosted Glass)
                            drawRoundRect(
                                color = inactiveGlassFill,
                                topLeft = Offset(baseX, top),
                                size = Size(barWidth, h),
                                cornerRadius = cornerRadius
                            )
                            // Reflejo muy suave inactivo
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.10f),
                                topLeft = Offset(baseX + 0.5.dp.toPx(), top + 0.5.dp.toPx()),
                                size = Size(barWidth - 1.dp.toPx(), (h * 0.25f).coerceAtLeast(2.dp.toPx())),
                                cornerRadius = CornerRadius((barWidth - 1.dp.toPx()) / 2f, (barWidth - 1.dp.toPx()) / 2f)
                            )
                            // Borde fino
                            drawRoundRect(
                                color = inactiveGlassBorder,
                                topLeft = Offset(baseX, top),
                                size = Size(barWidth, h),
                                cornerRadius = cornerRadius,
                                style = Stroke(width = 0.75.dp.toPx())
                            )
                        }
                    }

                    // Línea central de guía HUD sutil
                    drawLine(
                        color = Color.White.copy(alpha = 0.12f),
                        start = Offset(0f, centerY),
                        end = Offset(width, centerY),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Cabezal / cursor de reproducción Liquid Crystal Glowing Orbs
                    if (effectiveProgress > 0.005f && effectiveProgress < 0.995f) {
                        // Halo radial de dispersión
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    liquidCyan.copy(alpha = glowPulse * 0.85f),
                                    liquidMagenta.copy(alpha = glowPulse * 0.45f),
                                    Color.Transparent
                                )
                            ),
                            radius = 12.dp.toPx(),
                            center = Offset(activeX, centerY)
                        )

                        // Núcleo de cristal blanco
                        drawCircle(
                            color = Color.White,
                            radius = 3.5.dp.toPx(),
                            center = Offset(activeX, centerY)
                        )

                        // Anillo de cristal líquido
                        drawCircle(
                            brush = Brush.sweepGradient(
                                listOf(liquidCyan, liquidMagenta, liquidViolet, liquidCyan),
                                center = Offset(activeX, centerY)
                            ),
                            radius = 5.5.dp.toPx(),
                            center = Offset(activeX, centerY),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                WaveformProgressStyle.LIQUID_GLASS_WAVE -> {
                    // Visualizador de onda continua líquida con envolvente superior e inferior translúcida
                    val samples = 140
                    val stepX = width / samples
                    val pathFilled = Path()
                    val pathEmpty = Path()

                    val upperFilled = Path()
                    val lowerFilled = Path()

                    upperFilled.moveTo(0f, centerY)
                    lowerFilled.moveTo(0f, centerY)

                    pathFilled.moveTo(0f, centerY)
                    pathEmpty.moveTo(activeX, centerY)

                    var startedEmpty = false

                    for (i in 0..samples) {
                        val currentX = i * stepX
                        val norm = i.toFloat() / samples

                        val baseWave = sin(norm * PI.toFloat() * 6f) * 0.45f +
                                sin(norm * PI.toFloat() * 12f) * 0.30f +
                                cos(norm * PI.toFloat() * 3f) * 0.20f

                        val animModifier = if (isPlaying) {
                            sin(pulsePhase + norm * 7f).toFloat() * 0.24f +
                                    cos(secondaryPulse + norm * 11f).toFloat() * 0.12f
                        } else 0f

                        val waveH = (canvasHeight * 0.40f) * (baseWave + animModifier).coerceIn(-0.95f, 0.95f)
                        val y = centerY + waveH

                        if (currentX <= activeX) {
                            pathFilled.lineTo(currentX, y)
                            upperFilled.lineTo(currentX, centerY - kotlin.math.abs(waveH))
                            lowerFilled.lineTo(currentX, centerY + kotlin.math.abs(waveH))
                        } else {
                            if (!startedEmpty) {
                                pathEmpty.moveTo(currentX, y)
                                startedEmpty = true
                            } else {
                                pathEmpty.lineTo(currentX, y)
                            }
                        }
                    }

                    // 1. Relleno acuático translúcido interior (Liquid Wave Body)
                    val liquidBodyPath = Path().apply {
                        addPath(upperFilled)
                        lineTo(activeX, centerY)
                        addPath(lowerFilled)
                        close()
                    }
                    drawPath(
                        path = liquidBodyPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                liquidCyan.copy(alpha = 0.25f),
                                liquidMagenta.copy(alpha = 0.35f),
                                liquidViolet.copy(alpha = 0.20f)
                            ),
                            startY = centerY - canvasHeight * 0.4f,
                            endY = centerY + canvasHeight * 0.4f
                        )
                    )

                    // 2. Trazo inactivo (cristal esmerilado translúcido)
                    drawPath(
                        path = pathEmpty,
                        color = inactiveGlassBorder.copy(alpha = 0.6f),
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 3. Trazo activo con degradado neón brillante y resplandor
                    drawPath(
                        path = pathFilled,
                        brush = Brush.horizontalGradient(
                            listOf(liquidViolet, liquidMagenta, liquidCyan, Color.White),
                            startX = 0f,
                            endX = activeX.coerceAtLeast(1f)
                        ),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 4. Cursor esférico
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(liquidCyan.copy(alpha = glowPulse), Color.Transparent),
                            center = Offset(activeX, centerY),
                            radius = 12.dp.toPx()
                        ),
                        center = Offset(activeX, centerY),
                        radius = 12.dp.toPx()
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(activeX, centerY)
                    )
                }

                WaveformProgressStyle.FREQUENCY_PULSE -> {
                    // Ecualizador de frecuencia espectral con picos y valles iluminados
                    val barCount = 36
                    val slotWidth = width / barCount
                    val barWidth = slotWidth * 0.55f
                    val maxH = canvasHeight * 0.88f

                    for (i in 0 until barCount) {
                        val baseX = i * slotWidth + slotWidth * 0.22f
                        val norm = i.toFloat() / barCount
                        val freqFactor = sin(norm * PI.toFloat() * 2f).let { kotlin.math.abs(it) } * 0.6f +
                                sin(norm * PI.toFloat() * 5f).let { kotlin.math.abs(it) } * 0.4f

                        val anim = if (isPlaying) {
                            sin(pulsePhase * 1.5f + (i * 0.4f)).toFloat() * 0.22f
                        } else 0f

                        val barH = (maxH * (freqFactor + anim).coerceIn(0.12f, 1.0f)).coerceAtLeast(4.dp.toPx())
                        val isFilled = (baseX + barWidth) <= activeX

                        if (isFilled) {
                            // Barra desde la base inferior hacia arriba (HUD Spectrum)
                            val topY = canvasHeight - barH
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    listOf(liquidCyan, liquidMagenta, liquidViolet),
                                    startY = topY,
                                    endY = canvasHeight
                                ),
                                topLeft = Offset(baseX, topY),
                                size = Size(barWidth, barH),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                            // Reflejo superior
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.8f),
                                topLeft = Offset(baseX, topY),
                                size = Size(barWidth, 2.dp.toPx()),
                                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                            )
                        } else {
                            val topY = canvasHeight - barH
                            drawRoundRect(
                                color = inactiveGlassFill,
                                topLeft = Offset(baseX, topY),
                                size = Size(barWidth, barH),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                            drawRoundRect(
                                color = inactiveGlassBorder,
                                topLeft = Offset(baseX, topY),
                                size = Size(barWidth, barH),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                                style = Stroke(0.75.dp.toPx())
                            )
                        }
                    }

                    // Línea de base brillante
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(liquidViolet, liquidMagenta, liquidCyan),
                            startX = 0f,
                            endX = activeX.coerceAtLeast(1f)
                        ),
                        start = Offset(0f, canvasHeight - 1.dp.toPx()),
                        end = Offset(activeX, canvasHeight - 1.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }
    }
}
