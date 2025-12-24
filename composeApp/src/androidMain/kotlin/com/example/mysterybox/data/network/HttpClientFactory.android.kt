package com.example.mysterybox.data.network

import android.util.Log
import com.example.mysterybox.data.auth.AuthInterceptor
import com.example.mysterybox.data.auth.AuthManager
import com.example.mysterybox.data.auth.TokenAuthenticator
import com.example.mysterybox.data.repository.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

actual fun createHttpClient(
    authRepository: AuthRepository,
    authManager: AuthManager
): HttpClient = HttpClient(OkHttp) {

    engine {
        // Create the authenticator and interceptor
        val tokenAuthenticator = TokenAuthenticator(authRepository, authManager)
        val authInterceptor = AuthInterceptor(authRepository)

        // Add them to the OkHttp client
        authenticator(tokenAuthenticator)
        addInterceptor(authInterceptor)

        config {
            connectTimeout(20, TimeUnit.SECONDS)
            readTimeout(20, TimeUnit.SECONDS)
            writeTimeout(20, TimeUnit.SECONDS)
        }
    }

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
                Log.d("MysteryBoxAPI", message)
            }
        }
        level = LogLevel.ALL
    }
}
