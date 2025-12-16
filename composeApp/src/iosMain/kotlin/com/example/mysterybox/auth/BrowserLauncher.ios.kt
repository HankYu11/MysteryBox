package com.example.mysterybox.auth

import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem
import platform.UIKit.UIApplication
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned

class IOSBrowserLauncher : BrowserLauncher {
    override fun launchLineAuth(
        channelId: String,
        redirectUri: String,
        state: String,
        scope: String
    ) {
        val components = NSURLComponents("https://access.line.me/oauth2/v2.1/authorize")
        components.queryItems = listOf(
            NSURLQueryItem("response_type", "code"),
            NSURLQueryItem("client_id", channelId),
            NSURLQueryItem("redirect_uri", redirectUri),
            NSURLQueryItem("state", state),
            NSURLQueryItem("scope", scope)
        )

        components.URL?.let { url ->
            UIApplication.sharedApplication.openURL(url)
        }
    }
}

actual fun createBrowserLauncher(): BrowserLauncher = IOSBrowserLauncher()

@OptIn(ExperimentalForeignApi::class)
actual fun generateSecureState(): String {
    val bytes = ByteArray(32)
    bytes.usePinned { pinned ->
        SecRandomCopyBytes(kSecRandomDefault, bytes.size.toULong(), pinned.addressOf(0))
    }
    return bytes.joinToString("") { byte ->
        val hex = (byte.toInt() and 0xFF).toString(16)
        if (hex.length == 1) "0$hex" else hex
    }
}
