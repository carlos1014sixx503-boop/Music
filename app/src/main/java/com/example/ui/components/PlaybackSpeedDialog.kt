package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalDistritoTheme

/**
 * Selector interactivo de velocidad de reproducción (0.5x a 2.0x)
 * basado en la funcionalidad avanzada de BitChord.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSpeedDialog(
    visible: Boolean,
    currentSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    val theme = LocalDistritoTheme.current
    val speedPresets = listOf(0.5f, 0.75f, 0.9f, 1.0f, 1.15f, 1.25f, 1.5f, 1.75f, 2.0f)

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .fillMaxWidth(0.92f)
            .testTag("playback_speed_dialog")
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
                // Header Bar
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
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = theme.primaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "VELOCIDAD DE AUDIO",
                                color = theme.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Control de tempo sin distorsión de tono",
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

            // Display de Velocidad Actual Gigante
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .border(1.dp, theme.surfaceBorderColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.2fx".format(currentSpeed),
                        color = theme.primaryAccent,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = when {
                            currentSpeed < 1.0f -> "Tempo lento / Desacelerado"
                            currentSpeed > 1.0f -> "Tempo rápido / Acelerado"
                            else -> "Velocidad Normal (1.00x)"
                        },
                        color = theme.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Slider de Ajuste Continuo
            Slider(
                value = currentSpeed,
                onValueChange = { onSpeedChange(it) },
                valueRange = 0.5f..2.0f,
                steps = 14,
                colors = SliderDefaults.colors(
                    thumbColor = theme.primaryAccent,
                    activeTrackColor = theme.primaryAccent,
                    inactiveTrackColor = theme.surfaceBorderColor.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth().testTag("speed_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "0.50x", color = theme.textMuted, fontSize = 11.sp)
                Text(text = "1.00x (Normal)", color = theme.primaryAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = "2.00x", color = theme.textMuted, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cuadrícula de Botones Preset
            Text(
                text = "PREAJUSTES RÁPIDOS",
                color = theme.textMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                speedPresets.take(5).forEach { preset ->
                    val isSelected = Math.abs(currentSpeed - preset) < 0.04f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) theme.primaryAccent else theme.surfaceColor)
                            .border(
                                1.dp,
                                if (isSelected) theme.primaryAccent else theme.surfaceBorderColor.copy(alpha = 0.5f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onSpeedChange(preset) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${preset}x",
                            color = if (isSelected) theme.materialColorScheme.onPrimary else theme.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                speedPresets.drop(5).forEach { preset ->
                    val isSelected = Math.abs(currentSpeed - preset) < 0.04f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) theme.primaryAccent else theme.surfaceColor)
                            .border(
                                1.dp,
                                if (isSelected) theme.primaryAccent else theme.surfaceBorderColor.copy(alpha = 0.5f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onSpeedChange(preset) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${preset}x",
                            color = if (isSelected) theme.materialColorScheme.onPrimary else theme.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón restablecer Liquid Glass
            LiquidGlassButton(
                onClick = { onSpeedChange(1.0f) },
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 14.dp,
                contentPadding = PaddingValues(vertical = 11.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = theme.primaryAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Restablecer a 1.00x Normal",
                    color = theme.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
}
