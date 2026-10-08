package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalDistritoTheme

data class EqualizerPresetItem(
    val name: String,
    val levels: List<Int>
)

object EqualizerPresets {
    val presets = listOf(
        EqualizerPresetItem("Club 503", listOf(5, 3, -1, 2, 4)),
        EqualizerPresetItem("Plano", listOf(0, 0, 0, 0, 0)),
        EqualizerPresetItem("Rock", listOf(4, 2, -2, 2, 5)),
        EqualizerPresetItem("Pop", listOf(-1, 2, 5, 2, -1)),
        EqualizerPresetItem("Jazz", listOf(3, 2, 0, 2, 3)),
        EqualizerPresetItem("Clásica", listOf(4, 3, 0, 2, 4)),
        EqualizerPresetItem("Bajo Potente", listOf(8, 5, 0, 0, -1)),
        EqualizerPresetItem("Electrónica", listOf(6, 4, -1, 3, 5)),
        EqualizerPresetItem("Vocal", listOf(-2, -1, 5, 3, 1)),
        EqualizerPresetItem("Acústico", listOf(3, 2, 1, 3, 2))
    )
}

val BAND_LABELS = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
val BAND_NAMES = listOf("Sub", "Graves", "Medios", "Med-Altos", "Agudos")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerDspModal(
    visible: Boolean,
    isDspEnabled: Boolean,
    equalizerPreset: String,
    bandLevels: List<Int>,
    bassBoostPercent: Int,
    virtualizerPercent: Int,
    onToggleDsp: (Boolean) -> Unit,
    onSetPreset: (String, List<Int>) -> Unit,
    onBandChange: (Int, Int) -> Unit,
    onBassBoostChange: (Int) -> Unit,
    onVirtualizerChange: (Int) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalDistritoTheme.current

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.backgroundBrush)
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("equalizer_dsp_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LiquidGlassIconButton(
                        onClick = onDismiss,
                        icon = Icons.Default.Close,
                        contentDescription = "Cerrar ecualizador",
                        size = 42.dp,
                        iconSize = 22.dp,
                        tint = theme.textPrimary
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ECUALIZADOR DSP 503",
                            color = theme.primaryAccent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Procesamiento de Audio 32-bit Hi-Res",
                            color = theme.textSecondary,
                            fontSize = 11.sp
                        )
                    }

                    // DSP Power Switch
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(0.35f), Color.White.copy(0.08f))), RoundedCornerShape(20.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Switch(
                            checked = isDspEnabled,
                            onCheckedChange = onToggleDsp,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = theme.materialColorScheme.onPrimary,
                                checkedTrackColor = theme.primaryAccent,
                                uncheckedTrackColor = theme.surfaceBorderColor
                            ),
                            modifier = Modifier.testTag("dsp_master_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                LiquidGlassDivider(accentGlow = theme.primaryAccent)
                Spacer(modifier = Modifier.height(14.dp))

                // Presets Horizontal Carousel
                Text(
                    text = "PERFILES PREESTABLECIDOS",
                    color = theme.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(EqualizerPresets.presets) { item ->
                        val isSelected = equalizerPreset == item.name
                        LiquidGlassPillButton(
                            onClick = { onSetPreset(item.name, item.levels) },
                            isSelected = isSelected,
                            text = item.name,
                            accentColor = theme.primaryAccent,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Visual Frequency Curve Display (Poweramp style EQ Curve)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = theme.glowColor)
                        .clip(RoundedCornerShape(20.dp))
                        .background(theme.surfaceColor.copy(alpha = 0.8f))
                        .border(1.dp, theme.surfaceBorderColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val midY = height / 2f

                        // Grid lines
                        drawLine(
                            color = Color.White.copy(alpha = 0.08f),
                            start = Offset(0f, midY),
                            end = Offset(width, midY),
                            strokeWidth = 1.5f
                        )

                        val points = mutableListOf<Offset>()
                        val stepX = width / 6f

                        // Add starting zero edge
                        points.add(Offset(0f, midY))
                        for (i in 0 until 5) {
                            val level = bandLevels.getOrElse(i) { 0 }.coerceIn(-12, 12)
                            val normalizedY = midY - (level / 12f) * (height * 0.42f)
                            val x = stepX * (i + 1)
                            points.add(Offset(x, normalizedY))
                        }
                        points.add(Offset(width, midY))

                        // Path smooth curve
                        val curvePath = Path().apply {
                            moveTo(points[0].x, points[0].y)
                            for (i in 0 until points.size - 1) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val controlX1 = (p0.x + p1.x) / 2f
                                cubicTo(controlX1, p0.y, controlX1, p1.y, p1.x, p1.y)
                            }
                        }

                        // Gradient fill under curve
                        val fillPath = Path().apply {
                            addPath(curvePath)
                            lineTo(width, height)
                            lineTo(0f, height)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    theme.primaryAccent.copy(alpha = if (isDspEnabled) 0.35f else 0.08f),
                                    Color.Transparent
                                )
                            )
                        )

                        // Neon Curve line
                        drawPath(
                            path = curvePath,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    theme.primaryAccent,
                                    theme.secondaryAccent,
                                    theme.primaryAccent
                                )
                            ),
                            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                        )

                        // Node dots
                        for (i in 1..5) {
                            val pt = points[i]
                            drawCircle(
                                color = theme.primaryAccent,
                                radius = 5f,
                                center = pt
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.5f,
                                center = pt
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 5 Bands Sliders
                Text(
                    text = "BANDAS DE FRECUENCIA",
                    color = theme.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                for (bandIdx in 0 until 5) {
                    val level = bandLevels.getOrElse(bandIdx) { 0 }
                    val freqLabel = BAND_LABELS.getOrElse(bandIdx) { "" }
                    val nameLabel = BAND_NAMES.getOrElse(bandIdx) { "" }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(theme.surfaceColor.copy(alpha = 0.65f))
                            .border(1.dp, theme.surfaceBorderColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = freqLabel,
                                        color = theme.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• $nameLabel",
                                        color = theme.textMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                Text(
                                    text = if (level > 0) "+$level dB" else "$level dB",
                                    color = if (level != 0) theme.primaryAccent else theme.textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Slider(
                                value = level.toFloat(),
                                onValueChange = { onBandChange(bandIdx, it.toInt()) },
                                valueRange = -12f..12f,
                                steps = 23, // 1 dB per step
                                enabled = isDspEnabled,
                                colors = SliderDefaults.colors(
                                    thumbColor = theme.primaryAccent,
                                    activeTrackColor = theme.primaryAccent,
                                    inactiveTrackColor = theme.surfaceBorderColor.copy(alpha = 0.4f),
                                    disabledThumbColor = Color.Gray,
                                    disabledActiveTrackColor = Color.DarkGray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(26.dp)
                                    .testTag("eq_slider_band_$bandIdx")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Boosters: Bass Boost and 3D Virtualizer
                Text(
                    text = "MEJORAS ACÚSTICAS",
                    color = theme.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bass Boost Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(theme.surfaceColor.copy(alpha = 0.65f))
                            .border(1.dp, theme.surfaceBorderColor.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = theme.primaryAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Bass Boost",
                                        color = theme.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "$bassBoostPercent%",
                                    color = theme.primaryAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = bassBoostPercent.toFloat(),
                                onValueChange = { onBassBoostChange(it.toInt()) },
                                valueRange = 0f..100f,
                                enabled = isDspEnabled,
                                colors = SliderDefaults.colors(
                                    thumbColor = theme.primaryAccent,
                                    activeTrackColor = theme.primaryAccent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // 3D Virtualizer Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(theme.surfaceColor.copy(alpha = 0.65f))
                            .border(1.dp, theme.surfaceBorderColor.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SurroundSound,
                                        contentDescription = null,
                                        tint = theme.secondaryAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "3D Surround",
                                        color = theme.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "$virtualizerPercent%",
                                    color = theme.secondaryAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = virtualizerPercent.toFloat(),
                                onValueChange = { onVirtualizerChange(it.toInt()) },
                                valueRange = 0f..100f,
                                enabled = isDspEnabled,
                                colors = SliderDefaults.colors(
                                    thumbColor = theme.secondaryAccent,
                                    activeTrackColor = theme.secondaryAccent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Reset button
                Button(
                    onClick = onReset,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.surfaceColor,
                        contentColor = theme.textSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, theme.surfaceBorderColor, RoundedCornerShape(14.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Restablecer a Plano", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
