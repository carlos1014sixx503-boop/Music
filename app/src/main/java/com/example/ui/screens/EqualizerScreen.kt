package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

/**
 * Screen 4: Dedicated Professional Equalizer & Audio DSP Screen (Pantalla de Ecualizador)
 * - Curva de respuesta en frecuencia reactiva dibujada en tiempo real sobre Canvas.
 * - Ecualizador gráfico de 5 bandas (-12 dB a +12 dB) con etiquetas en Hz.
 * - Selector interactivo de Presets profesionales ("Club 503", "Rock", "Pop", "Bass Boost", etc.).
 * - Controles giratorios/deslizantes para Refuerzo de Graves (Bass Boost) y Virtualizador 3D.
 * - Interruptor maestro DSP con halo neón Liquid Glass.
 */
@Composable
fun EqualizerScreen(
    playerState: MusicPlayerState,
    modifier: Modifier = Modifier
) {
    val isDspEnabled by playerState.isDspEnabled.collectAsState()
    val currentPreset by playerState.equalizerPreset.collectAsState()
    val bandLevels by playerState.bandLevels.collectAsState()
    val bassBoostPercent by playerState.bassBoostPercent.collectAsState()
    val virtualizerPercent by playerState.virtualizerPercent.collectAsState()

    val bandFrequencies = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
    val bandDescriptions = listOf("Sub-Bass", "Bass", "Mids", "High Mid", "Treble")

    val presets = listOf(
        "Club 503" to listOf(5, 3, -1, 2, 4),
        "Bass Boost" to listOf(7, 5, 1, 0, -1),
        "Rock" to listOf(4, 2, -2, 3, 5),
        "Pop" to listOf(-1, 2, 5, 3, -1),
        "Electrónica" to listOf(6, 4, 0, 2, 5),
        "Voz Clara" to listOf(-2, 0, 4, 5, 2),
        "Jazz" to listOf(3, 1, 2, 3, 3),
        "Plano" to listOf(0, 0, 0, 0, 0)
    )

    var selectedReverbIndex by remember { mutableIntStateOf(1) }
    val reverbPresets = listOf("Desactivado", "Estudio", "Sala Concierto", "Club", "Catedral")

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 130.dp)
    ) {
        // Cabecera superior con interruptor DSP Master
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
                            .testTag("eq_header_icon"),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_avatar_alex),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column {
                        Text(
                            text = "Ecualizador DSP",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = TextWhite
                        )
                        Text(
                            text = if (isDspEnabled) "Procesador activo • 32-bit" else "Modo bypass sin procesar",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = if (isDspEnabled) NeonPurpleLight else TextMuted
                        )
                    }
                }

                // Switch Maestro DSP
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Switch(
                        checked = isDspEnabled,
                        onCheckedChange = { playerState.setDspEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextWhite,
                            checkedTrackColor = NeonPurple,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = GlassSurfaceUltraDark
                        ),
                        modifier = Modifier.testTag("switch_master_dsp")
                    )
                }
            }
        }

        // Tarjeta de la Curva de Frecuencia Sonora Dinámica (Canvas Visualizer)
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
                                Color(0x3338BDF8)
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xF2150530),
                                Color(0xE60D021F)
                            )
                        )
                    )
                    .padding(18.dp)
                    .testTag("frequency_curve_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = NeonPurpleLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Curva Acústica",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = TextWhite
                            )
                        }

                        // Preset Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x33A855F7))
                                .border(1.dp, Color(0x66A855F7), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = currentPreset,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = NeonPurpleLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Lienzo Canvas interactivo de onda sonora
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x55070212))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val midY = h / 2f

                            // Líneas de referencia de cuadrícula acústica
                            drawLine(
                                color = Color.White.copy(alpha = 0.08f),
                                start = Offset(0f, midY),
                                end = Offset(w, midY),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.04f),
                                start = Offset(0f, midY - h * 0.35f),
                                end = Offset(w, midY - h * 0.35f),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.04f),
                                start = Offset(0f, midY + h * 0.35f),
                                end = Offset(w, midY + h * 0.35f),
                                strokeWidth = 1.dp.toPx()
                            )

                            // Construcción de la curva basada en los 5 niveles de banda (-12 a +12 dB)
                            val points = mutableListOf<Offset>()
                            val count = bandLevels.size
                            val stepX = w / (count + 1)

                            points.add(Offset(0f, midY))
                            bandLevels.forEachIndexed { idx, level ->
                                val x = stepX * (idx + 1)
                                val normalizedGain = (level / 12f).coerceIn(-1f, 1f)
                                val y = midY - (normalizedGain * (h * 0.42f))
                                points.add(Offset(x, y))
                            }
                            points.add(Offset(w, midY))

                            // Trazar curva Bezier suave
                            val path = Path()
                            val fillPath = Path()

                            if (points.isNotEmpty()) {
                                path.moveTo(points.first().x, points.first().y)
                                fillPath.moveTo(0f, h)
                                fillPath.lineTo(points.first().x, points.first().y)

                                for (i in 0 until points.size - 1) {
                                    val p0 = points[i]
                                    val p1 = points[i + 1]
                                    val controlX = (p0.x + p1.x) / 2f
                                    path.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                                    fillPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                                }

                                fillPath.lineTo(w, h)
                                fillPath.close()

                                // Relleno de degradado debajo de la curva
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            Color(0x66A855F7),
                                            Color(0x1138BDF8),
                                            Color(0x00000000)
                                        )
                                    )
                                )

                                // Trazo de la curva neón
                                drawPath(
                                    path = path,
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFFFF26C2),
                                            Color(0xFFA855F7),
                                            Color(0xFF38BDF8)
                                        )
                                    ),
                                    style = Stroke(
                                        width = 3.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                )

                                // Nodos brillantes en cada frecuencia
                                points.drop(1).dropLast(1).forEach { pt ->
                                    drawCircle(
                                        color = Color(0xFFA855F7),
                                        radius = 5.dp.toPx(),
                                        center = pt
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 2.5.dp.toPx(),
                                        center = pt
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selector de Presets de Ecualización (Pills Carousel)
        item {
            Column(modifier = Modifier.padding(vertical = 10.dp)) {
                Text(
                    text = "Ajustes Preestablecidos",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = TextWhite,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    presets.forEach { (presetName, levels) ->
                        val isSelected = currentPreset == presetName
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier
                                            .shadow(10.dp, RoundedCornerShape(20.dp), ambientColor = NeonPurple, spotColor = NeonPurple)
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(
                                                        Color(0xFFB042FF),
                                                        Color(0xFF7E22CE)
                                                    )
                                                )
                                            )
                                            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                    } else {
                                        Modifier
                                            .background(GlassSurfaceUltraDark)
                                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                                    }
                                )
                                .clickable { playerState.setEqualizerPreset(presetName, levels) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("eq_preset_$presetName"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = presetName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = if (isSelected) TextWhite else TextMuted
                            )
                        }
                    }
                }
            }
        }

        // 5 Sliders de Frecuencia del Ecualizador
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xE6120529))
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.16f),
                                Color.White.copy(alpha = 0.04f)
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(18.dp)
                    .testTag("eq_sliders_container")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bandas de Frecuencia",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = TextWhite
                        )

                        // Botón restablecer a plano
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(GlassSurfaceUltraDark)
                                .clickable { playerState.resetEqualizer() }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("btn_reset_eq")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Restablecer",
                                tint = NeonPurpleLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Restablecer",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextWhite
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    bandFrequencies.forEachIndexed { index, freq ->
                        val currentVal = bandLevels.getOrElse(index) { 0 }
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = freq,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        ),
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "(${bandDescriptions.getOrElse(index) { "" }})",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = TextMuted
                                    )
                                }

                                Text(
                                    text = if (currentVal > 0) "+$currentVal dB" else "$currentVal dB",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    color = if (currentVal > 0) NeonPurpleLight else if (currentVal < 0) Color(0xFF38BDF8) else TextMuted
                                )
                            }

                            Slider(
                                value = currentVal.toFloat(),
                                onValueChange = { playerState.setBandLevel(index, it.toInt()) },
                                valueRange = -12f..12f,
                                steps = 23,
                                colors = SliderDefaults.colors(
                                    thumbColor = TextWhite,
                                    activeTrackColor = NeonPurple,
                                    inactiveTrackColor = Color(0x33FFFFFF)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("slider_band_$index")
                            )
                        }
                    }
                }
            }
        }

        // Efectos Espaciales: Refuerzo de Graves & Virtualizador 3D
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Tarjeta 1: Refuerzo de Bajos (Bass Boost)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xE614062E))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                        .testTag("card_bass_boost")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Refuerzo de Bajos",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = TextWhite,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0x33A855F7))
                                .border(2.dp, NeonPurple, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$bassBoostPercent%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = TextWhite
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = bassBoostPercent.toFloat(),
                            onValueChange = { playerState.setBassBoost(it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = TextWhite,
                                activeTrackColor = NeonPurple,
                                inactiveTrackColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("slider_bass_boost")
                        )
                    }
                }

                // Tarjeta 2: Virtualizador 3D Surround
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xE614062E))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                        .testTag("card_virtualizer")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Virtualizador 3D",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = TextWhite,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0x3338BDF8))
                                .border(2.dp, Color(0xFF38BDF8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$virtualizerPercent%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = TextWhite
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = virtualizerPercent.toFloat(),
                            onValueChange = { playerState.setVirtualizer(it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = TextWhite,
                                activeTrackColor = Color(0xFF38BDF8),
                                inactiveTrackColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("slider_virtualizer")
                        )
                    }
                }
            }
        }

        // Simulación de Ambiente / Reverb Acústico
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Text(
                    text = "Ambiente Acústico (Reverb)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = TextWhite,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    reverbPresets.forEachIndexed { idx, name ->
                        val isSelected = selectedReverbIndex == idx
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color(0x55A855F7) else GlassSurfaceUltraDark)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonPurple else Color.White.copy(alpha = 0.10f),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedReverbIndex = idx }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                ),
                                color = if (isSelected) TextWhite else TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
