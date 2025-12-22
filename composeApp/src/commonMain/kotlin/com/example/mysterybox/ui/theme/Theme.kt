package com.example.mysterybox.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Green500,
    onPrimary = White,
    primaryContainer = Green50,
    onPrimaryContainer = Green700,
    secondary = Gray600,
    onSecondary = White,
    secondaryContainer = Gray100,
    onSecondaryContainer = Gray800,
    tertiary = Orange500,
    onTertiary = White,
    background = White,
    onBackground = Gray900,
    surface = White,
    onSurface = Gray900,
    surfaceVariant = Gray50,
    onSurfaceVariant = Gray600,
    error = Red500,
    onError = White,
    errorContainer = Red50,
    onErrorContainer = Red500,
    outline = Gray300,
    outlineVariant = Gray200
)

@Composable
expect fun ConfigureSystemBars(
    statusBarColor: Color = Color.Transparent,
    navigationBarColor: Color = Color.Transparent,
    darkIcons: Boolean = true
)

@Composable
fun MysteryBoxTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    
    // Configure system bars for edge-to-edge
    ConfigureSystemBars(
        statusBarColor = Color.Transparent,
        navigationBarColor = Color.Transparent,
        darkIcons = true // Dark icons work well with light theme
    )
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
