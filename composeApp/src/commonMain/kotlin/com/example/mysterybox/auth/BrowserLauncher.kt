package com.example.mysterybox.auth

interface BrowserLauncher {
    fun launchLineAuth(
        channelId: String,
        redirectUri: String,
        state: String,
        scope: String
    )
}

expect fun createBrowserLauncher(): BrowserLauncher

expect fun generateSecureState(): String
