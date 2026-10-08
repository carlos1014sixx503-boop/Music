package com.example.ui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adaptador de compatibilidad para GlassmorphicCard basado en el componente base LiquidGlassCard.
 * Permite que todas las tarjetas existentes se beneficien de desenfoque (blur), baja opacidad y reflejo especular.
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(cornerRadius),
    customBackgroundColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    LiquidGlassCard(
        modifier = modifier,
        cornerRadius = cornerRadius,
        borderWidth = borderWidth,
        onClick = onClick,
        shape = shape,
        tintColor = customBackgroundColor,
        content = content
    )
}
