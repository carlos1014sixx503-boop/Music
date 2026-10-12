package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// DEEP PURPLE PALETTE TOKENS
// =========================================================================
val DeepPurple950 = Color(0xFF070210) // Obsidian Velvet Purple (App Background)
val DeepPurple900 = Color(0xFF0F0622) // Deep Purple Night
val DeepPurple850 = Color(0xFF160930) // Elevated Dark Purple
val DeepPurple800 = Color(0xFF1E0E40) // Sub-surface Purple
val DeepPurple700 = Color(0xFF3B1578) // Mid-Dark Purple
val DeepPurple600 = Color(0xFF6B21A8) // Vivid Violet
val DeepPurple500 = Color(0xFF8B5CF6) // Electric Purple
val DeepPurple400 = Color(0xFFA855F7) // Vibrant Neon Purple (Primary Accent)
val DeepPurple300 = Color(0xFFC084FC) // Soft Purple Glow
val DeepPurple200 = Color(0xFFE9D5FF) // Light Lavender Tint
val DeepPurple100 = Color(0xFFF5EEFF) // Off-white Purple Highlight

// =========================================================================
// SLATE PALETTE TOKENS
// =========================================================================
val Slate950 = Color(0xFF020617) // Ultra-dark Slate Base
val Slate900 = Color(0xFF0F172A) // Deep Night Slate
val Slate850 = Color(0xFF172033) // Midnight Slate
val Slate800 = Color(0xFF1E293B) // Dark Frosted Glass Slate
val Slate700 = Color(0xFF334155) // Elevated Frosted Slate
val Slate600 = Color(0xFF475569) // Border / Divider Slate
val Slate500 = Color(0xFF64748B) // Slate Muted Text / Inactive
val Slate400 = Color(0xFF94A3B8) // Slate Secondary Text
val Slate300 = Color(0xFFCBD5E1) // Slate Medium Light Text
val Slate200 = Color(0xFFE2E8F0) // Slate High Contrast Text
val Slate100 = Color(0xFFF1F5F9) // Slate Bright Text
val Slate50  = Color(0xFFF8FAFC) // Slate Pure Crisp White

// =========================================================================
// LIQUID GLASS SEMI-TRANSPARENT & BLUR TOKENS
// =========================================================================
// Semi-transparent glass backdrops (heavy translucency designed to blur underlying content)
val GlassSlateSubtle       = Color(0x1F1E293B) // 12% Slate 800 - ultra sheer glass
val GlassSlateBase         = Color(0x381E293B) // 22% Slate 800 - standard liquid glass
val GlassSlateElevated     = Color(0x54334155) // 33% Slate 700 - elevated cards/modals
val GlassSlateHeavyFrost   = Color(0x730F172A) // 45% Slate 900 - blur-heavy backdrop
val GlassPurpleBase        = Color(0x331B0C33) // 20% Deep Purple 800
val GlassPurpleElevated    = Color(0x52281248) // 32% Deep Purple 700
val GlassDockBackground    = Color(0xD9090316) // 85% Deep Obsidian Slate-Purple

// Specular Highlights & Glass Rims
val GlassSpecularWhite     = Color(0x66FFFFFF) // Specular top rim shine
val GlassBorderSubtle      = Color(0x1FFFFFFF) // Subtle 12% frosted edge
val GlassBorderPurple      = Color(0x38A855F7) // Iridescent purple edge
val GlassBorderSlate       = Color(0x3394A3B8) // Slate glass edge
val GlassGlowPurple        = Color(0x40A855F7) // Atmospheric neon purple glow
val GlassGlowCyan          = Color(0x3D38BDF8) // Atmospheric ice cyan glow

// Brushes for Liquid Glass Rims & Highlights
val GlassSpecularRimBrush = Brush.verticalGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.38f),
        Color.White.copy(alpha = 0.10f),
        DeepPurple400.copy(alpha = 0.22f)
    )
)

val GlassSlateRimBrush = Brush.verticalGradient(
    colors = listOf(
        Slate300.copy(alpha = 0.32f),
        Slate500.copy(alpha = 0.10f),
        DeepPurple500.copy(alpha = 0.15f)
    )
)

val GlassBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF090314),
        Color(0xFF0D061A),
        Color(0xFF070B16),
        Color(0xFF04020A)
    )
)

// =========================================================================
// BACKWARD COMPATIBILITY TOKENS
// =========================================================================
val NeonPurple = DeepPurple400
val NeonPurpleDark = DeepPurple600
val NeonPurpleLight = DeepPurple300
val NeonPurpleGlow = GlassGlowPurple

val AccentPink = Color(0xFFFF2E93)
val AccentCyan = Color(0xFF00F0FF)

// Liquid 3D Orb colors
val NeonOrbPink = Color(0xFFFF26C2)
val NeonOrbPurple = DeepPurple400
val NeonOrbViolet = DeepPurple600
val NeonOrbCyan = Color(0xFF00F0FF)

// Deep Velvet Night Backgrounds
val VelvetBlack = DeepPurple950
val DeepVioletBackground = Color(0xFF0D0618)
val DeepVioletCard = Color(0xFF140A26)
val DeepVioletElevated = Color(0xFF1D0E38)
val DeepVioletBorder = Color(0x33C084FC)

// Surface & Glassmorphism
val GlassSurface = GlassPurpleBase
val GlassSurfaceHigh = GlassPurpleElevated
val GlassSurfaceUltraDark = Color(0xE60D051C)
val BottomDockBackground = GlassDockBackground

// Text Colors
val TextWhite = Slate50
val TextMuted = Slate400
val TextSubtle = Slate500

// Waveform
val WaveformWhite = Color(0xFFFFFFFF)
val WaveformInactiveWhite = Color(0x33FFFFFF)
