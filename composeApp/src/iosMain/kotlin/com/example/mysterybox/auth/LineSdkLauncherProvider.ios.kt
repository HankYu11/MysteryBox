package com.example.mysterybox.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * iOS implementation (stub for now)
 */
@Composable
actual fun rememberLineSdkLauncher(): (callback: (accessToken: String?, userId: String?, displayName: String?, error: String?) -> Unit) -> Unit {
    return remember {
        { callback ->
            callback(null, null, null, "LINE SDK not implemented for iOS yet")
        }
    }
}
