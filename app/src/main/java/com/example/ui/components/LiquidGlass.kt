package com.example.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalDistritoTheme

/**
 * Paleta de colores para las esferas luminosas en estilo Liquid Glass.
 * Seleccionada con una armonía vibrante de Cyan Eléctrico (#00F5D4),
 * Violeta Neón (#8B5CF6), Azul Cielo (#38BDF8) y Magenta (#F43F5E).
 */
object LiquidGlassSpheresPalette {
    // Esferas cromáticas principales
    val ElectricCyan = Color(0xFF00F5D4)
    val NeonViolet = Color(0xFF8B5CF6)
    val SkyAzure = Color(0xFF38BDF8)
    val RadiantMagenta = Color(0xFFF43F5E)
    val EmeraldTeal = Color(0xFF10B981)
    val AmberGlow = Color(0xFFF59E0B)
    val CrimsonRed = Color(0xFFFF2A55)

    // Gradiente especular para bordes y biseles de vidrio ("Rim Light")
    val RimLightGradient = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.38f),
            Color.White.copy(alpha = 0.12f),
            Color.White.copy(alpha = 0.04f)
        )
    )

    // Gradiente para botones Liquid Glass activos
    fun activeRimBrush(accent: Color): Brush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.65f),
            accent.copy(alpha = 0.45f),
            Color.White.copy(alpha = 0.15f)
        )
    )

    // Gradiente para divisiones de interfaz de vidrio biselado
    fun dividerBrush(accent: Color? = null): Brush {
        val centerColor = accent?.copy(alpha = 0.35f) ?: Color.White.copy(alpha = 0.28f)
        return Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.12f),
                centerColor,
                Color.White.copy(alpha = 0.12f),
                Color.Transparent
            )
        )
    }
}

/**
 * Fondo con esferas luminosas estilo Liquid Glass ("Liquid Glass Spheres Background").
 * Dibuja orbes esféricos flotantes con gradientes radiales y suave desenfoque que producen
 * la refracción y profundidad cromática característica de la interfaz Liquid Glass.
 */
@Composable
fun LiquidGlassSpheresBackground(
    modifier: Modifier = Modifier,
    @DrawableRes backgroundImageRes: Int? = null,
    backgroundAlpha: Float = 0.85f,
    backgroundBlurRadius: Dp = 2.dp,
    primarySphereColor: Color = LiquidGlassSpheresPalette.ElectricCyan,
    secondarySphereColor: Color = LiquidGlassSpheresPalette.NeonViolet,
    tertiarySphereColor: Color = LiquidGlassSpheresPalette.RadiantMagenta,
    quaternarySphereColor: Color = LiquidGlassSpheresPalette.SkyAzure,
    ambientDarkBackground: Color = Color(0xFF06080E),
    enableMotion: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidGlassSpheres")

    val floatOffset1 by if (enableMotion) {
        infiniteTransition.animateFloat(
            initialValue = -25f,
            targetValue = 25f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "Sphere1Float"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    val floatOffset2 by if (enableMotion) {
        infiniteTransition.animateFloat(
            initialValue = 20f,
            targetValue = -20f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 11000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "Sphere2Float"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    val pulseScale by if (enableMotion) {
        infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 6500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "SpherePulse"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ambientDarkBackground)
    ) {
        // Imagen artística de fondo difuminada y traslúcida (Batman) con suave transición
        if (backgroundImageRes != null) {
            Crossfade(
                targetState = backgroundImageRes,
                animationSpec = tween(durationMillis = 400),
                label = "WallpaperCrossfade"
            ) { targetRes ->
                val imageMod = if (backgroundBlurRadius > 0.dp) {
                    Modifier
                        .fillMaxSize()
                        .blur(radius = backgroundBlurRadius)
                        .alpha(backgroundAlpha)
                } else {
                    Modifier
                        .fillMaxSize()
                        .alpha(backgroundAlpha)
                }

                Image(
                    painter = painterResource(id = targetRes),
                    contentDescription = "Fondo de Pantalla Batman",
                    contentScale = ContentScale.Crop,
                    modifier = imageMod
                )
            }

            // Velo sutil traslúcido para asegurar máxima legibilidad de textos y botones sin opacar a Batman
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF04060A).copy(alpha = 0.28f),
                                Color(0xFF060910).copy(alpha = 0.12f),
                                Color(0xFF04060A).copy(alpha = 0.48f)
                            )
                        )
                    )
            )
        }

        // Esferas Liquid Glass sutiles que aportan destellos y profundidad luminosa sin tapar a Batman
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Esfera Superior Izquierda
            val sphere1Radius = (width * 0.45f) * pulseScale
            val sphere1Center = Offset(
                x = width * 0.15f + floatOffset1,
                y = height * 0.12f + floatOffset2
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primarySphereColor.copy(alpha = 0.18f),
                        primarySphereColor.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = sphere1Center,
                    radius = sphere1Radius
                ),
                radius = sphere1Radius,
                center = sphere1Center
            )

            // 2. Esfera Media Derecha
            val sphere2Radius = (width * 0.48f) * (2f - pulseScale)
            val sphere2Center = Offset(
                x = width * 0.88f - floatOffset2,
                y = height * 0.38f + floatOffset1
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        secondarySphereColor.copy(alpha = 0.15f),
                        secondarySphereColor.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = sphere2Center,
                    radius = sphere2Radius
                ),
                radius = sphere2Radius,
                center = sphere2Center
            )

            // 3. Esfera Inferior Izquierda
            val sphere3Radius = (width * 0.42f) * pulseScale
            val sphere3Center = Offset(
                x = width * 0.18f - floatOffset1,
                y = height * 0.86f - floatOffset2
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        tertiarySphereColor.copy(alpha = 0.14f),
                        tertiarySphereColor.copy(alpha = 0.04f),
                        Color.Transparent
                    ),
                    center = sphere3Center,
                    radius = sphere3Radius
                ),
                radius = sphere3Radius,
                center = sphere3Center
            )

            // 4. Halo central suave
            val sphere4Radius = (width * 0.35f)
            val sphere4Center = Offset(
                x = width * 0.55f + floatOffset2 * 0.5f,
                y = height * 0.60f + floatOffset1 * 0.5f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        quaternarySphereColor.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = sphere4Center,
                    radius = sphere4Radius
                ),
                radius = sphere4Radius,
                center = sphere4Center
            )
        }

        // Contenido encima del fondo Liquid Glass
        content()
    }
}

/**
 * Configuración paramétrica del estilo Liquid Glass para tarjetas y contenedores.
 */
data class LiquidGlassConfig(
    val cornerRadius: Dp = 20.dp,
    val blurRadius: Dp = 16.dp,
    val backgroundAlpha: Float = 0.52f,
    val borderAlpha: Float = 0.28f,
    val borderWidth: Dp = 1.2.dp,
    val elevation: Dp = 12.dp,
    val tintColor: Color? = null,
    val ambientGlow: Color? = null,
    val hasSpecularHighlight: Boolean = true
) {
    companion object {
        val Default = LiquidGlassConfig()
        val Card = LiquidGlassConfig(
            cornerRadius = 18.dp,
            blurRadius = 14.dp,
            backgroundAlpha = 0.58f,
            elevation = 10.dp
        )
        val Modal = LiquidGlassConfig(
            cornerRadius = 28.dp,
            blurRadius = 24.dp,
            backgroundAlpha = 0.84f,
            borderAlpha = 0.36f,
            borderWidth = 1.4.dp,
            elevation = 24.dp
        )
        val Pill = LiquidGlassConfig(
            cornerRadius = 32.dp,
            blurRadius = 12.dp,
            backgroundAlpha = 0.65f,
            elevation = 8.dp
        )
        val Subtle = LiquidGlassConfig(
            cornerRadius = 14.dp,
            blurRadius = 8.dp,
            backgroundAlpha = 0.35f,
            borderAlpha = 0.15f,
            borderWidth = 1.dp,
            elevation = 4.dp
        )
    }
}

/**
 * Componente base "Liquid Glass" adaptable para tarjetas, bloques y contenedores principales.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    blurRadius: Dp = 16.dp,
    backgroundAlpha: Float = 0.55f,
    borderWidth: Dp = 1.2.dp,
    tintColor: Color? = null,
    ambientGlow: Color? = null,
    shape: Shape = RoundedCornerShape(cornerRadius),
    hasSpecularHighlight: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val theme = LocalDistritoTheme.current

    val baseTint = tintColor ?: theme.surfaceColor
    val glow = ambientGlow ?: theme.glowColor.copy(alpha = 0.35f)

    val rimLightBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.32f),
            Color.White.copy(alpha = 0.10f),
            Color.White.copy(alpha = 0.04f)
        )
    )

    val glassFillBrush = Brush.verticalGradient(
        colors = listOf(
            baseTint.copy(alpha = (backgroundAlpha + 0.12f).coerceAtMost(0.95f)),
            baseTint.copy(alpha = backgroundAlpha)
        )
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = shape,
                ambientColor = glow,
                spotColor = glow
            )
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(color = theme.primaryAccent.copy(alpha = 0.3f)),
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
    ) {
        // 1. Capa de Desenfoque y Difuminado (Blur Backdrop)
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(radius = blurRadius)
                .background(baseTint.copy(alpha = (backgroundAlpha * 0.7f).coerceIn(0.15f, 0.85f)))
        )

        // 2. Capa translúcida con tinte líquido y degradado interno
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(glassFillBrush)
                .border(
                    width = borderWidth,
                    brush = rimLightBrush,
                    shape = shape
                )
        )

        // 3. Brillo especular superior
        if (hasSpecularHighlight) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.14f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = 160f
                        )
                    )
            )
        }

        // 4. Contenido
        content()
    }
}

/**
 * Marco de Ventana Flotante / Diálogo Modal Liquid Glass ("Liquid Glass Modal Frame").
 * Proporciona el marco biselado translúcido con desenfoque, brillo especular superior
 * y resplandor ambiental para ventanas emergentes, hojas y modales.
 */
@Composable
fun LiquidGlassModalFrame(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    borderWidth: Dp = 1.4.dp,
    accentGlow: Color? = null,
    backgroundAlpha: Float = 0.88f,
    content: @Composable BoxScope.() -> Unit
) {
    val theme = LocalDistritoTheme.current
    val glowColor = accentGlow ?: theme.glowColor.copy(alpha = 0.5f)
    val shape = RoundedCornerShape(cornerRadius)

    val frameBorderBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.38f),
            theme.primaryAccent.copy(alpha = 0.25f),
            Color.White.copy(alpha = 0.08f)
        )
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = 28.dp,
                shape = shape,
                ambientColor = glowColor,
                spotColor = glowColor
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1B1D28).copy(alpha = backgroundAlpha),
                        Color(0xFF10121B).copy(alpha = (backgroundAlpha + 0.08f).coerceAtMost(0.98f))
                    )
                )
            )
            .border(
                width = borderWidth,
                brush = frameBorderBrush,
                shape = shape
            )
    ) {
        // Reflejo especular superior tenue
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = 140f
                    )
                )
        )

        content()
    }
}

/**
 * Botón interactivo con apariencia Liquid Glass ("Liquid Glass Button").
 * Posee borde refractivo con degradado, brillo especular superior, resplandor ambiental
 * y efecto táctil ripple suave.
 */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = false,
    cornerRadius: Dp = 16.dp,
    tintColor: Color? = null,
    glowColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    content: @Composable RowScope.() -> Unit
) {
    val theme = LocalDistritoTheme.current
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }

    val accent = tintColor ?: theme.primaryAccent
    val glow = glowColor ?: theme.glowColor

    val borderBrush = if (isPrimary) {
        LiquidGlassSpheresPalette.activeRimBrush(accent)
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.32f),
                Color.White.copy(alpha = 0.10f)
            )
        )
    }

    val backgroundBrush = if (isPrimary) {
        Brush.verticalGradient(
            colors = listOf(
                accent.copy(alpha = 0.90f),
                accent.copy(alpha = 0.75f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.14f),
                Color.White.copy(alpha = 0.06f)
            )
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isPrimary) 12.dp else 4.dp,
                shape = shape,
                ambientColor = if (isPrimary) glow else Color.Transparent,
                spotColor = if (isPrimary) glow else Color.Transparent
            )
            .clip(shape)
            .background(backgroundBrush)
            .border(width = 1.2.dp, brush = borderBrush, shape = shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(color = Color.White.copy(alpha = 0.35f)),
                onClick = onClick
            )
    ) {
        // Brillo especular tenue en la parte superior
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isPrimary) 0.25f else 0.12f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = 50f
                    )
                )
        )

        Row(
            modifier = Modifier.padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * Botón tipo Píldora / Cápsula Liquid Glass ("Liquid Glass Pill Button").
 * Ideal para filtros de categorías, botones de reproducción ("Reproducir todo"),
 * selectores de vista o acciones rápidas.
 */
@Composable
fun LiquidGlassPillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    text: String? = null,
    icon: ImageVector? = null,
    accentColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 7.dp)
) {
    val theme = LocalDistritoTheme.current
    val accent = accentColor ?: theme.primaryAccent
    val shape = RoundedCornerShape(32.dp)
    val interactionSource = remember { MutableInteractionSource() }

    val bgBrush = if (isSelected) {
        Brush.horizontalGradient(
            listOf(
                Color.White,
                Color(0xFFE8E8EE)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFF222430).copy(alpha = 0.78f),
                Color(0xFF141620).copy(alpha = 0.78f)
            )
        )
    }

    val borderBrush = if (isSelected) {
        Brush.verticalGradient(
            listOf(
                Color.White,
                accent.copy(alpha = 0.5f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.24f),
                Color.White.copy(alpha = 0.08f)
            )
        )
    }

    val glowColor = if (isSelected) accent.copy(alpha = 0.45f) else Color.Transparent

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isSelected) 10.dp else 2.dp,
                shape = shape,
                ambientColor = glowColor,
                spotColor = glowColor
            )
            .clip(shape)
            .background(bgBrush)
            .border(width = 1.1.dp, brush = borderBrush, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = accent.copy(alpha = 0.3f)),
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = if (isSelected) Color(0xFF0F1118) else accent,
                    modifier = Modifier.size(16.dp)
                )
                if (text != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
            if (text != null) {
                Text(
                    text = text,
                    color = if (isSelected) Color(0xFF0F1118) else theme.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Botón circular con icono Liquid Glass ("Liquid Glass Icon Button").
 * Usado para botones de cabecera (cerrar, ecualizador, cast, sincronizar)
 * y controles circulares de reproducción.
 */
@Composable
fun LiquidGlassIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    tint: Color? = null,
    isHighlighted: Boolean = false,
    highlightColor: Color? = null
) {
    val theme = LocalDistritoTheme.current
    val accent = highlightColor ?: theme.primaryAccent
    val shape = CircleShape
    val interactionSource = remember { MutableInteractionSource() }

    val bgBrush = if (isHighlighted) {
        Brush.verticalGradient(
            listOf(
                accent.copy(alpha = 0.35f),
                accent.copy(alpha = 0.15f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.14f),
                Color.White.copy(alpha = 0.05f)
            )
        )
    }

    val rimBrush = if (isHighlighted) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.65f),
                accent.copy(alpha = 0.40f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.28f),
                Color.White.copy(alpha = 0.08f)
            )
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (isHighlighted) 8.dp else 3.dp,
                shape = shape,
                ambientColor = if (isHighlighted) accent.copy(alpha = 0.5f) else Color.Transparent,
                spotColor = if (isHighlighted) accent.copy(alpha = 0.5f) else Color.Transparent
            )
            .clip(shape)
            .background(bgBrush)
            .border(width = 1.1.dp, brush = rimBrush, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = (tint ?: theme.primaryAccent).copy(alpha = 0.35f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Reflejo especular superior
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = 40f
                    )
                )
        )

        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint ?: if (isHighlighted) accent else theme.textPrimary,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * División de interfaz Liquid Glass ("Liquid Glass Divider").
 * Sustituye las líneas opacas planas por una elegante costura de vidrio biselado
 * con degradado horizontal y suave refracción luminosa.
 */
@Composable
fun LiquidGlassDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    accentGlow: Color? = null
) {
    val theme = LocalDistritoTheme.current
    val accent = accentGlow ?: theme.primaryAccent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(LiquidGlassSpheresPalette.dividerBrush(accent))
    )
}

/**
 * Contenedor versátil Liquid Glass para paneles modales, hojas emergentes o secciones principales.
 */
@Composable
fun LiquidGlassContainer(
    modifier: Modifier = Modifier,
    config: LiquidGlassConfig = LiquidGlassConfig.Default,
    shape: Shape = RoundedCornerShape(config.cornerRadius),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    LiquidGlassCard(
        modifier = modifier,
        cornerRadius = config.cornerRadius,
        blurRadius = config.blurRadius,
        backgroundAlpha = config.backgroundAlpha,
        borderWidth = config.borderWidth,
        tintColor = config.tintColor,
        ambientGlow = config.ambientGlow,
        shape = shape,
        hasSpecularHighlight = config.hasSpecularHighlight,
        onClick = onClick,
        content = content
    )
}

/**
 * Modificador de extensión para aplicar el estilo visual 'Liquid Glass'
 * a cualquier Composable existente (Column, Row, Box, etc.).
 */
fun Modifier.liquidGlass(
    cornerRadius: Dp = 20.dp,
    blurRadius: Dp = 16.dp,
    backgroundAlpha: Float = 0.55f,
    borderWidth: Dp = 1.2.dp,
    tintColor: Color = Color(0xFF1E1E26),
    shape: Shape = RoundedCornerShape(cornerRadius),
    ambientGlow: Color = Color.Black.copy(alpha = 0.5f)
): Modifier = this
    .shadow(
        elevation = 10.dp,
        shape = shape,
        ambientColor = ambientGlow,
        spotColor = ambientGlow
    )
    .clip(shape)
    .blur(radius = blurRadius)
    .background(
        Brush.verticalGradient(
            colors = listOf(
                tintColor.copy(alpha = (backgroundAlpha + 0.12f).coerceAtMost(0.95f)),
                tintColor.copy(alpha = backgroundAlpha)
            )
        )
    )
    .border(
        width = borderWidth,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.32f),
                Color.White.copy(alpha = 0.08f)
            )
        ),
        shape = shape
    )
