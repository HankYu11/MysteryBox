package com.example.mysterybox.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import java.security.SecureRandom
import java.util.Base64

class AndroidBrowserLauncher(private val context: Context) : BrowserLauncher {
    override fun launchLineAuth(
        channelId: String,
        redirectUri: String,
        state: String,
        scope: String
    ) {
        val authUrl = buildString {
            append("https://access.line.me/oauth2/v2.1/authorize")
            append("?response_type=code")
            append("&client_id=$channelId")
            append("&redirect_uri=${Uri.encode(redirectUri)}")
            append("&state=$state")
            append("&scope=${Uri.encode(scope)}")
        }

        try {
            val customTabsIntent = CustomTabsIntent.Builder().build()
            customTabsIntent.launchUrl(context, Uri.parse(authUrl))
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}

private var appContext: Context? = null

fun setAppContext(context: Context) {
    appContext = context.applicationContext
}

actual fun createBrowserLauncher(): BrowserLauncher {
    return AndroidBrowserLauncher(
        appContext ?: throw IllegalStateException("App context not set. Call setAppContext() first.")
    )
}

actual fun generateSecureState(): String {
    val random = SecureRandom()
    val bytes = ByteArray(32)
    random.nextBytes(bytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
}
