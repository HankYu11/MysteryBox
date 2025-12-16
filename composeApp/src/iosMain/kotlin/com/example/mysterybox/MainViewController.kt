package com.example.mysterybox

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.ComposeUIViewController
import com.example.mysterybox.auth.IOSAuthCallbackHandler

fun MainViewController() = ComposeUIViewController {
    val pendingCallback by IOSAuthCallbackHandler.pendingCallback.collectAsState()

    App(
        oauthCallback = pendingCallback,
        onOAuthCallbackHandled = { IOSAuthCallbackHandler.clearCallback() }
    )
}