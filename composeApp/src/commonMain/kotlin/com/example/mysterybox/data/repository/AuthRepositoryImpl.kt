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

    override suspend fun loginWithLineToken(accessToken: String, userId: String, displayName: String): Result<AuthSession> {
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
                        response.session.refreshToken ?: "",
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

    override suspend fun loginWithLine(code: String, state: String?): Result<AuthSession> {
        return when (val result = apiService.loginWithLine(code, state, ApiConfig.REDIRECT_URI)) {
            is Result.Success -> {
                val response = result.data
                if (response.success && response.session != null) {
                    val session = response.session.toDomain()
                    tokenManager.saveUserSession(
                        response.session.accessToken,
                        response.session.refreshToken ?: "",
                        session.user
                    )
                    Result.Success(session)
                } else {
                    val error = response.error ?: "Authentication failed"
                    Result.Error(ApiError.AuthenticationError(error))
                }
            }
            is Result.Error -> result
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
        
        // This would need to be implemented in the API service
        return Result.Error(ApiError.NotImplemented("Token refresh not implemented"))
    }

    override suspend fun getCurrentUser(): Result<User> {
        return if (tokenManager.isUserAuthenticated()) {
            // In a real implementation, you'd decode the JWT or call an API
            Result.Error(ApiError.NotImplemented("getCurrentUser not implemented - needs JWT decode or API call"))
        } else {
            Result.Error(ApiError.AuthenticationError("Not authenticated"))
        }
    }
}
