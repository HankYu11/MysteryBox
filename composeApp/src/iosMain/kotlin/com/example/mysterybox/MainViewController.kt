package com.example.mysterybox

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * Creates the main UIViewController for iOS app.
 * This function is called from Swift (ContentView.swift).
 *
 * This bridges the Compose Multiplatform UI to the native iOS UIKit layer.
 */
fun MainViewController(): UIViewController {
    return ComposeUIViewController {
        App()
    }
}
