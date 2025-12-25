package com.example.mysterybox.data.network

import com.example.mysterybox.data.repository.AuthRepository
import com.example.mysterybox.data.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal expect val engine: HttpClientEngine

fun createHttpClient(
    authRepository: AuthRepository,
    tokenStorage: TokenStorage
): HttpClient {
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
                    val result = authRepository.refreshToken()
                    if (result is com.example.mysterybox.data.model.Result.Success) {
                        BearerTokens(result.data.accessToken, result.data.refreshToken)
                    } else {
                        tokenStorage.clearTokens()
                        null
                    }
                }

                sendWithoutRequest { request ->
                    !request.url.pathSegments.any { it.contains("auth") }
                }
            }
        }
    }
}