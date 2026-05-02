package com.medvision.ai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF83D6FF),
    onPrimary = Color(0xFF02131F),
    secondary = Color(0xFFB497FF),
    background = Color(0xFF040816),
    surface = Color(0xFF111A35),
    onSurface = Color(0xFFF3F6FF),
    onSurfaceVariant = Color(0xFFB9C6E3)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    secondary = Color(0xFF6A4BFF),
    background = Color(0xFFF5F7FF),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF101424),
    onSurfaceVariant = Color(0xFF536178)
)

@Composable
fun MedVisionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
