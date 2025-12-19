package com.example.mysterybox.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
actual fun ConfigureSystemBars(
    statusBarColor: Color,
    navigationBarColor: Color,
    darkIcons: Boolean
) {
    // iOS handles system bars differently through Info.plist and native code
    // This is a no-op implementation for KMP compatibility
    // iOS safe areas are handled automatically by the system
}