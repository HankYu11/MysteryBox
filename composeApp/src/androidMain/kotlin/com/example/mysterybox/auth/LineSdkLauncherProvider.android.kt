package com.example.mysterybox.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.mysterybox.data.network.ApiConfig

/**
 * Android implementation that provides LINE SDK launcher
 */
@Composable
actual fun rememberLineSdkLauncher(): (callback: (accessToken: String?, error: String?) -> Unit) -> Unit {
    var pendingCallback by remember { mutableStateOf<((accessToken: String?, error: String?) -> Unit)?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = LineLoginContract(ApiConfig.LINE_CHANNEL_ID)
    ) { result ->
        val callback = pendingCallback
        if (callback != null) {
            LineSdkLoginHelper.handleLoginResult(
                result,
                onSuccess = { accessToken ->
                    callback(accessToken, null)
                },
                onFailure = { error ->
                    callback(null, error)
                }
            )
            pendingCallback = null
        }
    }

    return { callback ->
        pendingCallback = callback
        launcher.launch(Unit)
    }
}
