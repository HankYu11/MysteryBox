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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthRepositoryImpl(
    private val apiService: MysteryBoxApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var currentUser: User? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    
    // LINE SDK login launcher (will be set from platform-specific code)
    private var lineSdkLauncher: ((callback: (String?, String?, String?, String?) -> Unit) -> Unit)? = null
    
    override fun setOAuthLauncher(launcher: Any) {
        // Expecting a function that starts LINE SDK login
        @Suppress("UNCHECKED_CAST")
        lineSdkLauncher = launcher as? ((callback: (String?, String?, String?, String?) -> Unit) -> Unit)
    }

    override fun startLineLogin() {
        val launcher = lineSdkLauncher
        if (launcher == null) {
            _authState.value = AuthState.Error("LINE SDK not initialized")
            return
        }
        
        _authState.value = AuthState.Loading
        
        // Launch LINE SDK login
        launcher { accessToken, userId, displayName, error ->
            scope.launch {
                if (error != null) {
                    _authState.value = AuthState.Error("Authentication error: $error")
                } else if (accessToken != null && userId != null && displayName != null) {
                    // We have the access token from LINE SDK
                    // Now send it to backend to verify and create session
                    loginWithLineAccessToken(accessToken, userId, displayName)
                } else {
                    _authState.value = AuthState.Error("Failed to get LINE authentication data")
                }
            }
        }
    }
    
    /**
     * Send LINE access token to backend for verification and session creation
     */
    private suspend fun loginWithLineAccessToken(lineAccessToken: String, lineUserId: String, displayName: String) {
        // TODO: Create a new backend endpoint to accept LINE access token
        // For now, save the token and create a temporary user
        // Backend should verify the token with LINE API and create proper session
        
        tokenManager.saveUserTokens(lineAccessToken, "temp_refresh_token")
        
        currentUser = User(
            id = lineUserId,
            lineUserId = lineUserId,
            displayName = displayName,
            pictureUrl = null,
            createdAt = null
        )
        
        _authState.value = AuthState.Authenticated(
            user = currentUser!!,
            accessToken = lineAccessToken
        )
    }

    override suspend fun loginWithLine(code: String, state: String?): Result<AuthSession> {
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
            _authState.value = AuthState.Idle
        }
    }
}
