package com.example.mysterybox.data.network

import com.example.mysterybox.data.auth.AuthManager
import com.example.mysterybox.data.repository.AuthRepository
import io.ktor.client.HttpClient

expect fun createHttpClient(
    authRepository: AuthRepository,
    authManager: AuthManager
): HttpClient
