package com.example.mysterybox.data.network

import com.example.mysterybox.data.auth.AuthManager
import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.repository.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

actual fun createHttpClient(
    authRepository: AuthRepository,
    authManager: AuthManager
): HttpClient = HttpClient(Darwin) {

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false
        })
    }

    install(Logging) {
        level = LogLevel.ALL
    }

    install(Auth) {
        bearer {
            loadTokens {
                // Load the access and refresh tokens from your repository
                val session = authRepository.getCurrentSession().first()
                if (session is Result.Success && session.data != null) {
                    BearerTokens(session.data.accessToken, session.data.refreshToken ?: "")
                } else {
                    null
                }
            }

            refreshTokens {
                // Perform the token refresh
                val result = authRepository.refreshToken()
                if (result is Result.Success && result.data != null) {
                    // Save the new session
                    authRepository.saveSession(result.data)
                    // Update the AuthManager state
                    val userResult = authRepository.getCurrentUser()
                    if (userResult is Result.Success) {
                        authManager.setAuthenticated(userResult.data, result.data.accessToken)
                    }
                    // Return the new tokens to the Auth plugin
                    BearerTokens(result.data.accessToken, result.data.refreshToken ?: "")
                } else {
                    // If refresh fails, clear the session and log the user out
                    authManager.clearSession()
                    null
                }
            }

            sendWithoutRequest {
                // Only send the token for requests that are not the login/refresh endpoints
                !it.url.encodedPath.contains("auth")
            }
        }
    }
}
