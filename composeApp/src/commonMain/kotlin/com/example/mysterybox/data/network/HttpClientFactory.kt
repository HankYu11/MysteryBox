package com.example.mysterybox.data.network

import com.example.mysterybox.data.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal expect val engine: HttpClientEngine

@Serializable
private data class RefreshTokenRequest(val refreshToken: String)

@Serializable
private data class RefreshTokenResponse(
    val success: Boolean,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val error: String? = null
)

fun createHttpClient(
    tokenStorage: TokenStorage
): HttpClient {
    // Create a persistent internal client for token refresh to avoid resource leak
    // This client is reused across refresh attempts and doesn't have Auth plugin
    val refreshClient = HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    return HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = false
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println("MysteryBoxAPI: $message")
                }
            }
            level = LogLevel.ALL
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val accessToken = tokenStorage.getAccessToken()
                    val refreshToken = tokenStorage.getRefreshToken()
                    if (accessToken != null && refreshToken != null) {
                        BearerTokens(accessToken, refreshToken)
                    } else {
                        null
                    }
                }

                refreshTokens {
                    val refreshToken = tokenStorage.getRefreshToken()
                    if (refreshToken != null) {
                        try {
                            // Use persistent refresh client to avoid creating new client each time
                            val response: RefreshTokenResponse = refreshClient.post("${ApiConfig.BASE_URL}/api/auth/refresh") {
                                contentType(ContentType.Application.Json)
                                setBody(RefreshTokenRequest(refreshToken))
                            }.body()

                            if (response.success && response.accessToken != null && response.refreshToken != null) {
                                tokenStorage.saveTokens(response.accessToken, response.refreshToken)
                                BearerTokens(response.accessToken, response.refreshToken)
                            } else {
                                tokenStorage.clearTokens()
                                null
                            }
                        } catch (e: Exception) {
                            tokenStorage.clearTokens()
                            null
                        }
                    } else {
                        null
                    }
                }

                sendWithoutRequest { request ->
                    // Don't send token for public auth endpoints (login/verify/refresh)
                    // But DO send for protected endpoints like /api/auth/me, /api/auth/logout
                    val path = request.url.toString()
                    !path.contains("/auth/line") &&
                    !path.contains("/auth/refresh") &&
                    !path.contains("/auth/merchant/login")
                }
            }
        }
    }
}