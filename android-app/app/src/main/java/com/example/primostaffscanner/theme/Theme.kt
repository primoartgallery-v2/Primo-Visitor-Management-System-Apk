package com.example.primostaffscanner.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimoWine,
    onPrimary = Color.White,
    primaryContainer = PrimoWine.copy(alpha = 0.1f),
    onPrimaryContainer = PrimoWine,
    secondary = PrimoGold,
    onSecondary = Color.White,
    tertiary = PrimoGreen,
    background = PrimoCream,
    onBackground = PrimoInk,
    surface = PrimoCardBg,
    onSurface = PrimoInk,
    surfaceVariant = PrimoCream,
    onSurfaceVariant = PrimoMuted,
    outline = PrimoBorder
)

@Composable
fun PrimoStaffScannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
