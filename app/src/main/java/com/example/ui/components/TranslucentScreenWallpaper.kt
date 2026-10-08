package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Componente que proyecta una imagen artística de fondo de forma difuminada (blur)
 * y traslúcida (baja opacidad), aplicando un degradado atmosférico oscuro para que
 * los textos, tarjetas Liquid Glass y botones destaquen con nitidez y elegancia.
 */
@Composable
fun TranslucentScreenWallpaper(
    @DrawableRes drawableRes: Int,
    modifier: Modifier = Modifier,
    blurRadius: Dp = 12.dp,
    translucencyAlpha: Float = 0.42f,
    tintColor: Color = Color(0xFF0C0C12),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Imagen artística de fondo difuminada y traslúcida
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = blurRadius)
                .alpha(translucencyAlpha)
        )

        // 2. Capa de degradado cinematográfico para profundidad, legibilidad y contraste M3
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            tintColor.copy(alpha = 0.48f),
                            tintColor.copy(alpha = 0.70f),
                            tintColor.copy(alpha = 0.88f)
                        )
                    )
                )
        )

        // 3. Contenido interactivo de la pantalla
        content()
    }
}
