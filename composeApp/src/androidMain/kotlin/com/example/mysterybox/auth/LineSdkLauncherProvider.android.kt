package com.example.mysterybox.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Android implementation that provides LINE SDK launcher
 */
@Composable
actual fun rememberLineSdkLauncher(): (callback: (accessToken: String?, userId: String?, displayName: String?, error: String?) -> Unit) -> Unit {
    return remember {
        { callback ->
            LineSdkLoginManager.startLogin(callback)
        }
    }
}
