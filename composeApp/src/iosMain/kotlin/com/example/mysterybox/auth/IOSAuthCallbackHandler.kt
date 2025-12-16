package com.example.mysterybox.auth

import com.example.mysterybox.data.model.OAuthCallbackResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton object to handle OAuth callbacks on iOS.
 *
 * Usage from Swift:
 * ```swift
 * // In SceneDelegate or AppDelegate:
 * func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
 *     guard let url = URLContexts.first?.url else { return }
 *     IOSAuthCallbackHandlerKt.handleOAuthUrl(url: url.absoluteString)
 * }
 * ```
 */
object IOSAuthCallbackHandler {
    private val _pendingCallback = MutableStateFlow<OAuthCallbackResult?>(null)
    val pendingCallback: StateFlow<OAuthCallbackResult?> = _pendingCallback.asStateFlow()

    fun handleCallback(code: String?, state: String?, error: String?) {
        _pendingCallback.value = OAuthCallbackResult(
            code = code,
            state = state,
            error = error
        )
    }

    fun clearCallback() {
        _pendingCallback.value = null
    }
}

/**
 * Helper function for Swift to call with URL string.
 * Parses the OAuth callback URL and extracts parameters.
 */
fun handleOAuthUrl(url: String) {
    if (!url.startsWith("mysterybox://auth")) return

    val queryString = url.substringAfter("?", "")
    if (queryString.isEmpty()) return

    val params = queryString.split("&").associate { param ->
        val parts = param.split("=", limit = 2)
        parts[0] to (parts.getOrNull(1) ?: "")
    }

    IOSAuthCallbackHandler.handleCallback(
        code = params["code"],
        state = params["state"],
        error = params["error"]
    )
}
