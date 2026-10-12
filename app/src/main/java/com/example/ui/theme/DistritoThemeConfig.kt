package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class AppThemeKey(val displayName: String) {
    LIQUID_GLASS("Liquid Glass"),
    BARCELONA("Barcelona"),
    REAL_MADRID("Real Madrid"),
    AMOLED_DARK("AMOLED Dark"),
    NEON("Neon")
}

data class DistritoThemeConfig(
    val key: AppThemeKey,
    val name: String,
    val description: String,
    val tagline: String,
    val backgroundBrush: Brush,
    val backgroundSolid: Color,
    val surfaceColor: Color,
    val surfaceVariantColor: Color,
    val surfaceBorderColor: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val tertiaryAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val glowColor: Color,
    val isGlassmorphism: Boolean,
    val glassAlpha: Float,
    val materialColorScheme: ColorScheme
)

object DistritoThemePresets {

    val LiquidGlass = DistritoThemeConfig(
        key = AppThemeKey.LIQUID_GLASS,
        name = "Liquid Glass",
        description = "Estética translúcida Liquid Glass con desenfoque profundo, púrpura neón y pizarra (Slate).",
        tagline = "Liquid Glass Púrpura & Pizarra (Por defecto)",
        backgroundBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF070210),
                Color(0xFF0F0622),
                Color(0xFF0B1120),
                Color(0xFF020617)
            )
        ),
        backgroundSolid = Color(0xFF070210),
        surfaceColor = Color(0x381E293B),
        surfaceVariantColor = Color(0x54334155),
        surfaceBorderColor = Color(0x38A855F7),
        primaryAccent = Color(0xFFA855F7),
        secondaryAccent = Color(0xFF94A3B8),
        tertiaryAccent = Color(0xFF00F0FF),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8),
        textMuted = Color(0xFF64748B),
        glowColor = Color(0x40A855F7),
        isGlassmorphism = true,
        glassAlpha = 0.65f,
        materialColorScheme = darkColorScheme(
            primary = Color(0xFFA855F7),
            onPrimary = Color(0xFF020617),
            primaryContainer = Color(0xFF1E0E40),
            onPrimaryContainer = Color(0xFFE9D5FF),
            secondary = Color(0xFF94A3B8),
            onSecondary = Color(0xFF0F172A),
            surface = Color(0xFF0F172A),
            onSurface = Color(0xFFF8FAFC),
            background = Color(0xFF070210),
            onBackground = Color(0xFFF8FAFC)
        )
    )

    val Barcelona = DistritoThemeConfig(
        key = AppThemeKey.BARCELONA,
        name = "Barcelona",
        description = "Inspiración deportiva blaugrana con azul marino profundo, granate refinado y toques dorados.",
        tagline = "Fuerza Blaugrana & Tonalidades Granates",
        backgroundBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF060D1E),
                Color(0xFF1A0A1F),
                Color(0xFF080F24)
            )
        ),
        backgroundSolid = Color(0xFF060D1E),
        surfaceColor = Color(0x440F1D38),
        surfaceVariantColor = Color(0x44260D24),
        surfaceBorderColor = Color(0x40A50044),
        primaryAccent = Color(0xFFEDBB00),
        secondaryAccent = Color(0xFF1877F2),
        tertiaryAccent = Color(0xFFA50044),
        textPrimary = Color(0xFFFDFEFE),
        textSecondary = Color(0xFFCBD5E1),
        textMuted = Color(0xFF8392A5),
        glowColor = Color(0x55EDBB00),
        isGlassmorphism = true,
        glassAlpha = 0.65f,
        materialColorScheme = darkColorScheme(
            primary = Color(0xFFEDBB00),
            onPrimary = Color(0xFF1C1400),
            primaryContainer = Color(0xFF5A4300),
            onPrimaryContainer = Color(0xFFFFE082),
            secondary = Color(0xFF004D98),
            onSecondary = Color(0xFFFFFFFF),
            tertiary = Color(0xFFA50044),
            surface = Color(0xFF0D172A),
            onSurface = Color(0xFFFFFFFF),
            background = Color(0xFF060D1E),
            onBackground = Color(0xFFFFFFFF)
        )
    )

    val RealMadrid = DistritoThemeConfig(
        key = AppThemeKey.REAL_MADRID,
        name = "Real Madrid",
        description = "Elegancia imperial con base obsidiana oscura, tipografía blanco puro y detalles en oro real.",
        tagline = "Distinción Blanca & Acentos de Oro Imperial",
        backgroundBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0C0E14),
                Color(0xFF151821),
                Color(0xFF0A0C10)
            )
        ),
        backgroundSolid = Color(0xFF0C0E14),
        surfaceColor = Color(0x451A1E29),
        surfaceVariantColor = Color(0x50282C3C),
        surfaceBorderColor = Color(0x45D4AF37),
        primaryAccent = Color(0xFFF1C40F),
        secondaryAccent = Color(0xFFFFFFFF),
        tertiaryAccent = Color(0xFF8E44AD),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFD1D5DB),
        textMuted = Color(0xFF9CA3AF),
        glowColor = Color(0x55F1C40F),
        isGlassmorphism = true,
        glassAlpha = 0.65f,
        materialColorScheme = darkColorScheme(
            primary = Color(0xFFF1C40F),
            onPrimary = Color(0xFF221A00),
            primaryContainer = Color(0xFF5C4700),
            onPrimaryContainer = Color(0xFFFCEB82),
            secondary = Color(0xFFF8FAFC),
            onSecondary = Color(0xFF0F172A),
            surface = Color(0xFF12151D),
            onSurface = Color(0xFFFFFFFF),
            background = Color(0xFF0C0E14),
            onBackground = Color(0xFFFFFFFF)
        )
    )

    val AmoledDark = DistritoThemeConfig(
        key = AppThemeKey.AMOLED_DARK,
        name = "AMOLED Dark",
        description = "Negro absoluto para pantallas OLED con máximo ahorro de energía y contraste nítido.",
        tagline = "Negro Puro 100% & Máxima Eficiencia",
        backgroundBrush = Brush.linearGradient(
            colors = listOf(Color(0xFF000000), Color(0xFF000000))
        ),
        backgroundSolid = Color(0xFF000000),
        surfaceColor = Color(0xFF0A0A0A),
        surfaceVariantColor = Color(0xFF161616),
        surfaceBorderColor = Color(0xFF242424),
        primaryAccent = Color(0xFFFFFFFF),
        secondaryAccent = Color(0xFF38BDF8),
        tertiaryAccent = Color(0xFFA1A1AA),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFA1A1AA),
        textMuted = Color(0xFF52525B),
        glowColor = Color(0x22FFFFFF),
        isGlassmorphism = false,
        glassAlpha = 1.0f,
        materialColorScheme = darkColorScheme(
            primary = Color(0xFFFFFFFF),
            onPrimary = Color(0xFF000000),
            secondary = Color(0xFF38BDF8),
            onSecondary = Color(0xFF000000),
            surface = Color(0xFF0A0A0A),
            onSurface = Color(0xFFFFFFFF),
            background = Color(0xFF000000),
            onBackground = Color(0xFFFFFFFF)
        )
    )

    val Neon = DistritoThemeConfig(
        key = AppThemeKey.NEON,
        name = "Neon",
        description = "Estilo cyberpunk electrizante con vibrantes halos cian neón, magenta y violeta resplandeciente.",
        tagline = "Frecuencia Cyberpunk & Resplandor Eléctrico",
        backgroundBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF050610),
                Color(0xFF100720),
                Color(0xFF050512)
            )
        ),
        backgroundSolid = Color(0xFF050610),
        surfaceColor = Color(0x3B181232),
        surfaceVariantColor = Color(0x50281248),
        surfaceBorderColor = Color(0x4000F5D4),
        primaryAccent = Color(0xFF00F5D4),
        secondaryAccent = Color(0xFFFF007F),
        tertiaryAccent = Color(0xFF7928CA),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFFC4B5FD),
        textMuted = Color(0xFF8B5CF6),
        glowColor = Color(0x6000F5D4),
        isGlassmorphism = true,
        glassAlpha = 0.50f,
        materialColorScheme = darkColorScheme(
            primary = Color(0xFF00F5D4),
            onPrimary = Color(0xFF002923),
            secondary = Color(0xFFFF007F),
            onSecondary = Color(0xFF330018),
            surface = Color(0xFF0D0B1C),
            onSurface = Color(0xFFF8FAFC),
            background = Color(0xFF050610),
            onBackground = Color(0xFFF8FAFC)
        )
    )

    val allThemes: List<DistritoThemeConfig> = listOf(
        LiquidGlass,
        Barcelona,
        RealMadrid,
        AmoledDark,
        Neon
    )

    fun getTheme(key: AppThemeKey): DistritoThemeConfig {
        return allThemes.find { it.key == key } ?: LiquidGlass
    }
}

val LocalDistritoTheme = compositionLocalOf { DistritoThemePresets.LiquidGlass }
