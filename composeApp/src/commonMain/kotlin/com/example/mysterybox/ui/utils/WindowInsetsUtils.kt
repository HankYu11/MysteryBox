package com.example.mysterybox.ui.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * Applies safe drawing insets as padding. This ensures content is not drawn under system UI.
 * Use this for the main content areas of your app.
 */
@Composable
fun Modifier.safeDrawingPadding() = windowInsetsPadding(WindowInsets.safeDrawing)

/**
 * Applies status bar insets as padding. Use this for top-level content that should 
 * respect the status bar area.
 */
@Composable
fun Modifier.statusBarsPadding() = windowInsetsPadding(WindowInsets.statusBars)

/**
 * Applies navigation bar insets as padding. Use this for bottom content that should
 * respect the navigation bar area.
 */
@Composable
fun Modifier.navigationBarsPadding() = windowInsetsPadding(WindowInsets.navigationBars)

/**
 * Applies system bars (status + navigation) insets as padding.
 * Useful for content that should respect both status and navigation bars.
 */
@Composable
fun Modifier.systemBarsPadding() = windowInsetsPadding(WindowInsets.systemBars)

/**
 * Applies IME (keyboard) insets as padding. Use this for content that should
 * move up when the keyboard appears.
 */
@Composable
fun Modifier.imePadding() = windowInsetsPadding(WindowInsets.ime)

/**
 * Applies only top padding from safe drawing insets.
 * Useful for screens with custom bottom navigation.
 */
@Composable
fun Modifier.safeTopPadding() = composed {
    val topPadding = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
    windowInsetsPadding(WindowInsets(top = topPadding))
}

/**
 * Applies only bottom padding from safe drawing insets.
 * Useful for floating action buttons or bottom sheets.
 */
@Composable
fun Modifier.safeBottomPadding() = composed {
    val bottomPadding = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
    windowInsetsPadding(WindowInsets(bottom = bottomPadding))
}

/**
 * Combines safe drawing padding with IME padding for forms and text input screens.
 * This ensures content respects both system UI and keyboard.
 */
@Composable
fun Modifier.safeDrawingWithImePadding() = composed {
    safeDrawingPadding().imePadding()
}