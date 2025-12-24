package com.example.mysterybox.data.repository

import com.example.mysterybox.data.dto.toDomain
import com.example.mysterybox.data.model.ApiError
import com.example.mysterybox.data.model.AuthSession
import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.model.User
import com.example.mysterybox.data.network.ApiConfig
import com.example.mysterybox.data.network.MysteryBoxApiService
import com.example.mysterybox.data.network.TokenManager

class AuthRepositoryImpl(
    private val apiService: MysteryBoxApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun loginWithLineToken(accessToken: String): Result<AuthSession> {
        // Send LINE access token to backend for verification
        return when (val result = apiService.verifyLineAccessToken(accessToken)) {
            is Result.Success -> {
                val response = result.data
                if (response.success && response.session != null) {
                    // Backend verified the token and created session
                    val session = response.session.toDomain()
                    
                    // Save backend's tokens and user data (not LINE's token)
                    tokenManager.saveUserSession(
                        response.session.accessToken,
                        response.session.refreshToken,
                        session.user
                    )
                    
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
            apiService.logout()
            tokenManager.clearUserTokens()
            Result.Success(Unit)
        } catch (e: Exception) {
            // Logout locally even if server call fails
            tokenManager.clearUserTokens()
            Result.Success(Unit)
        }
    }

    override suspend fun refreshToken(): Result<AuthSession> {
        val refreshToken = tokenManager.getRefreshToken()
            ?: return Result.Error(ApiError.AuthenticationError("No refresh token available"))

        return when (val result = apiService.refreshToken(refreshToken)) {
            is Result.Success -> {
                val response = result.data
                if (response.success && response.accessToken != null && response.refreshToken != null) {
                    val currentUser = tokenManager.getCurrentUser()
                        ?: return Result.Error(ApiError.AuthenticationError("No user data available"))

                    // Save new tokens
                    tokenManager.saveUserSession(
                        response.accessToken,
                        response.refreshToken,
                        currentUser
                    )

                    Result.Success(
                        AuthSession(
                            accessToken = response.accessToken,
                            refreshToken = response.refreshToken,
                            expiresIn = response.expiresIn ?: 0,
                            user = currentUser
                        )
                    )
                } else {
                    val error = response.error ?: "Token refresh failed"
                    Result.Error(ApiError.AuthenticationError(error))
                }
            }
            is Result.Error -> result
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        if (!tokenManager.isUserAuthenticated()) {
            return Result.Error(ApiError.AuthenticationError("Not authenticated"))
        }

        return when (val result = apiService.getCurrentUser()) {
            is Result.Success -> {
                val response = result.data
                if (response.success && response.user != null) {
                    val user = response.user.toDomain()
                    // Update stored user data
                    val accessToken = tokenManager.getAccessToken()
                    val refreshToken = tokenManager.getRefreshToken()
                    if (accessToken != null && refreshToken != null) {
                        tokenManager.saveUserSession(accessToken, refreshToken, user)
                    }
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
        return try {
            if (tokenManager.isUserAuthenticated()) {
                val user = tokenManager.getCurrentUser()
                val accessToken = tokenManager.getAccessToken()

                if (user != null && !accessToken.isNullOrEmpty()) {
                    Result.Success(
                        AuthSession(
                            accessToken = accessToken,
                            refreshToken = tokenManager.getRefreshToken() ?: "",
                            expiresIn = 0, // Not tracked in current implementation
                            user = user
                        )
                    )
                } else {
                    // Invalid stored data, clear tokens
                    tokenManager.clearUserTokens()
                    Result.Success(null)
                }
            } else {
                Result.Success(null)
            }
        } catch (e: Exception) {
            // Error accessing stored data, clear and return null
            try {
                tokenManager.clearUserTokens()
            } catch (clearException: Exception) {
                // Ignore clear errors
            }
            Result.Success(null)
        }
    }
}
