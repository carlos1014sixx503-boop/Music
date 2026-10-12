package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/**
 * Design Tokens for the semi-transparent, blur-heavy Liquid Glass aesthetic
 * with Deep Purple and Slate color palettes.
 */
@Immutable
data class LiquidGlassThemeTokens(
    val backgroundBrush: Brush = GlassBackgroundGradient,
    val backgroundSolid: Color = DeepPurple950,
    val surfaceGlassSubtle: Color = GlassSlateSubtle,
    val surfaceGlass: Color = GlassSlateBase,
    val surfaceGlassElevated: Color = GlassSlateElevated,
    val surfaceGlassHeavyFrost: Color = GlassSlateHeavyFrost,
    val surfaceGlassPurple: Color = GlassPurpleBase,
    val surfaceGlassPurpleElevated: Color = GlassPurpleElevated,
    val glassSpecularRim: Brush = GlassSpecularRimBrush,
    val glassSlateRim: Brush = GlassSlateRimBrush,
    val glassBorderSubtle: Color = GlassBorderSubtle,
    val glassBorderPurple: Color = GlassBorderPurple,
    val glassBorderSlate: Color = GlassBorderSlate,
    val glassGlowPurple: Color = GlassGlowPurple,
    val glassGlowCyan: Color = GlassGlowCyan,
    val primaryAccent: Color = DeepPurple400,
    val secondaryAccent: Color = Slate400,
    val tertiaryAccent: Color = AccentCyan,
    val textSlatePrimary: Color = Slate50,
    val textSlateSecondary: Color = Slate400,
    val textSlateMuted: Color = Slate500,
    val blurRadiusHeavy: Dp = 28.dp,
    val blurRadiusStandard: Dp = 16.dp,
    val blurRadiusSubtle: Dp = 8.dp,
    val glassOpacity: Float = 0.65f
)

val LocalLiquidGlassTokens = staticCompositionLocalOf { LiquidGlassThemeTokens() }

/**
 * Accessor for LiquidGlassTheme semantic tokens in Compose hierarchy.
 */
object LiquidGlassTheme {
    val tokens: LiquidGlassThemeTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalLiquidGlassTokens.current
}

/**
 * Reusable Modifier extension to style any composable container with the
 * signature semi-transparent, blur-heavy Liquid Glass aesthetic.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    isElevated: Boolean = false,
    useHeavyFrost: Boolean = false,
    customBorderBrush: Brush? = null,
    glowColor: Color? = null,
    tonalElevation: Dp = 12.dp
): Modifier {
    val surfaceColor = when {
        useHeavyFrost -> GlassSlateHeavyFrost
        isElevated -> GlassSlateElevated
        else -> GlassSlateBase
    }
    val borderBrush = customBorderBrush ?: GlassSpecularRimBrush
    val ambientGlow = glowColor ?: GlassGlowPurple

    return this
        .shadow(
            elevation = tonalElevation,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.6f),
            spotColor = ambientGlow
        )
        .clip(shape)
        .background(surfaceColor)
        .border(1.dp, borderBrush, shape)
}

/**
 * Material 3 Dark ColorScheme systematically mapped to Deep Purple & Slate tokens.
 */
private val DeepPurpleSlateColorScheme: ColorScheme = darkColorScheme(
    primary = DeepPurple400,
    onPrimary = Slate50,
    primaryContainer = DeepPurple800,
    onPrimaryContainer = DeepPurple200,
    secondary = Slate400,
    onSecondary = Slate950,
    secondaryContainer = Slate800,
    onSecondaryContainer = Slate100,
    tertiary = AccentCyan,
    onTertiary = Slate950,
    tertiaryContainer = Slate900,
    onTertiaryContainer = AccentCyan,
    background = DeepPurple950,
    onBackground = Slate50,
    surface = GlassSlateBase,
    onSurface = Slate50,
    surfaceVariant = GlassSlateElevated,
    onSurfaceVariant = Slate400,
    outline = GlassBorderSlate,
    outlineVariant = GlassBorderSubtle
)

@Composable
fun SoundWaveTheme(
    darkTheme: Boolean = true,
    tokens: LiquidGlassThemeTokens = LiquidGlassThemeTokens(),
    content: @Composable () -> Unit
) {
    val colorScheme = DeepPurpleSlateColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = DeepPurple950.toArgb()
            window.navigationBarColor = DeepPurple950.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    // Bridge with DistritoThemeConfig for existing components relying on LocalDistritoTheme
    val bridgedDistritoTheme = DistritoThemePresets.LiquidGlass.copy(
        backgroundSolid = tokens.backgroundSolid,
        backgroundBrush = tokens.backgroundBrush,
        surfaceColor = tokens.surfaceGlass,
        surfaceVariantColor = tokens.surfaceGlassElevated,
        surfaceBorderColor = tokens.glassBorderPurple,
        primaryAccent = tokens.primaryAccent,
        secondaryAccent = tokens.secondaryAccent,
        tertiaryAccent = tokens.tertiaryAccent,
        textPrimary = tokens.textSlatePrimary,
        textSecondary = tokens.textSlateSecondary,
        textMuted = tokens.textSlateMuted,
        glowColor = tokens.glassGlowPurple,
        materialColorScheme = colorScheme
    )

    CompositionLocalProvider(
        LocalLiquidGlassTokens provides tokens,
        LocalDistritoTheme provides bridgedDistritoTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
