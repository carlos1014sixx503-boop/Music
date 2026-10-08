package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun DistritoMusicTheme(
    themeConfig: DistritoThemeConfig = DistritoThemePresets.LiquidGlass,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalDistritoTheme provides themeConfig) {
        MaterialTheme(
            colorScheme = themeConfig.materialColorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    DistritoMusicTheme(
        themeConfig = DistritoThemePresets.LiquidGlass,
        content = content
    )
}

