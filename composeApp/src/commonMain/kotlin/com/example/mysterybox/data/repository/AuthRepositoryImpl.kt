package com.example.mysterybox.data.repository

import com.example.mysterybox.data.dto.toDomain
import com.example.mysterybox.data.model.ApiError
import com.example.mysterybox.data.model.AuthSession
import com.example.mysterybox.data.model.AuthState
import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.model.User
import com.example.mysterybox.data.network.ApiConfig
import com.example.mysterybox.data.network.MysteryBoxApiService
import com.example.mysterybox.data.network.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepositoryImpl(
    private val apiService: MysteryBoxApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var currentUser: User? = null
    private var storedState: String? = null

    override suspend fun exchangeLineCode(code: String, state: String): Result<AuthSession> {
        _authState.value = AuthState.Loading

        return when (val result = apiService.loginWithLine(code, state, ApiConfig.REDIRECT_URI)) {
            is Result.Success -> {
                val response = result.data
                if (response.success && response.session != null) {
                    val session = response.session.toDomain()
                    tokenManager.saveUserTokens(
                        response.session.accessToken,
                        response.session.refreshToken
                    )
                    currentUser = session.user
                    _authState.value = AuthState.Authenticated(
                        user = session.user,
                        accessToken = session.accessToken
                    )
                    Result.Success(session)
                } else {
                    val error = response.error ?: "Authentication failed"
                    _authState.value = AuthState.Error(error)
                    Result.Error(ApiError.AuthenticationError(error))
                }
            }
            is Result.Error -> {
                _authState.value = AuthState.Error(result.error.toMessage())
                result
            }
        }
    }

    override fun getCurrentUser(): User? = currentUser

    override fun isAuthenticated(): Boolean = tokenManager.isUserAuthenticated()

    override suspend fun logout() {
        try {
            apiService.logout()
        } catch (e: Exception) {
            // Logout locally even if server call fails
        } finally {
            tokenManager.clearUserTokens()
            currentUser = null
            storedState = null
            _authState.value = AuthState.Idle
        }
    }

    override fun validateState(receivedState: String): Boolean {
        return storedState == receivedState
    }

    override fun storeState(state: String) {
        storedState = state
    }
}
