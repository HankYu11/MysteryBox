package com.example.mysterybox.data.repository

import com.example.mysterybox.data.dto.toDomain
import com.example.mysterybox.data.model.*
import com.example.mysterybox.data.network.MysteryBoxApiService
import com.example.mysterybox.data.storage.TokenStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AuthRepositoryImpl(
    private val apiService: MysteryBoxApiService,
    private val tokenStorage: TokenStorage,
    private val json: Json
) : AuthRepository {

    override suspend fun loginWithLineToken(accessToken: String): Result<AuthSession> {
        return when (val result = apiService.verifyLineAccessToken(accessToken)) {
            is Result.Success -> {
                val response = result.data
                if (response.success && response.session != null) {
                    val session = response.session.toDomain()
                    tokenStorage.saveTokens(session.accessToken, session.refreshToken ?: session.accessToken)
                    tokenStorage.saveUserData(json.encodeToString(session.user))
                    Result.Success(session)
                } else {
                    val error = response.error ?: "Authentication failed"
                    Result.Error(ApiError.AuthenticationError(error))
                }
            }
            is Result.Error -> {
                Result.Error(ApiError.NetworkError("Failed to verify with server: ${result.error.toMessage()}"))
            }
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            val result = apiService.logout()
            tokenStorage.clearTokens()

            when (result) {
                is Result.Success -> Result.Success(Unit)
                is Result.Error -> {
                    // Local tokens cleared, but backend call failed
                    // Inform caller of network error for logging/analytics
                    result
                }
            }
        } catch (e: Exception) {
            // Always clear local tokens even on exception
            tokenStorage.clearTokens()
            // Return error to inform caller, but local logout succeeded
            Result.Error(ApiError.NetworkError("Logout request failed: ${e.message}", e))
        }
    }

    override suspend fun refreshToken(): Result<AuthSession> {
        val refreshToken = tokenStorage.getRefreshToken()
            ?: return Result.Error(ApiError.AuthenticationError("No refresh token available"))

        return when (val result = apiService.refreshToken(refreshToken)) {
            is Result.Success -> {
                val response = result.data
                if (response.success && response.accessToken != null && response.refreshToken != null) {
                    val userData = tokenStorage.getUserData() ?: return Result.Error(ApiError.AuthenticationError("No user data available"))
                    val user = json.decodeFromString<User>(userData)

                    tokenStorage.saveTokens(response.accessToken, response.refreshToken)

                    Result.Success(
                        AuthSession(
                            accessToken = response.accessToken,
                            refreshToken = response.refreshToken,
                            expiresIn = response.expiresIn ?: 0,
                            user = user
                        )
                    )
                } else {
                    tokenStorage.clearTokens() // Clear tokens on refresh failure
                    val error = response.error ?: "Token refresh failed"
                    Result.Error(ApiError.AuthenticationError(error))
                }
            }
            is Result.Error -> {
                tokenStorage.clearTokens() // Clear tokens on refresh failure
                result
            }
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        val accessToken = tokenStorage.getAccessToken()
        if (accessToken.isNullOrEmpty()) {
            return Result.Error(ApiError.AuthenticationError("Not authenticated"))
        }

        return when (val result = apiService.getCurrentUser()) {
            is Result.Success -> {
                val response = result.data
                if (response.success && response.user != null) {
                    val user = response.user.toDomain()
                    tokenStorage.saveUserData(json.encodeToString(user))
                    Result.Success(user)
                } else {
                    val error = response.error ?: "Failed to get user info"
                    Result.Error(ApiError.AuthenticationError(error))
                }
            }
            is Result.Error -> result
        }
    }

    override suspend fun getCurrentSession(): Result<AuthSession?> {
        val accessToken = tokenStorage.getAccessToken()
        val refreshToken = tokenStorage.getRefreshToken()
        val userData = tokenStorage.getUserData()

        return if (accessToken != null && refreshToken != null && userData != null) {
            val user = json.decodeFromString<User>(userData)
            Result.Success(
                AuthSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresIn = 0, // Not tracked
                    user = user
                )
            )
        } else {
            Result.Success(null)
        }
    }
}
