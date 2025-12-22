package com.example.mysterybox.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * iOS implementation using LINE SDK via Swift bridge.
 * Delegates to IOSLineSdkManager which bridges to Swift LINE SDK.
 */
@Composable
actual fun rememberLineSdkLauncher(): (callback: (accessToken: String?, error: String?) -> Unit) -> Unit {
    return remember {
        { callback ->
            IOSLineSdkManager.startLogin(callback)
        }
    }
}
