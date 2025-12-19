package com.example.mysterybox.auth

import androidx.compose.runtime.Composable

/**
 * Provides a platform-specific LINE SDK launcher
 * Returns a function that starts LINE login and provides result via callback
 */
@Composable
expect fun rememberLineSdkLauncher(): (callback: (accessToken: String?, userId: String?, displayName: String?, error: String?) -> Unit) -> Unit
