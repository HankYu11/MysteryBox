package com.example.mysterybox.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.model.User
import com.example.mysterybox.data.network.TokenManager
import com.example.mysterybox.data.repository.AuthRepository
import com.example.mysterybox.ui.state.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthenticationStatus()
    }
    
    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            try {
                if (tokenManager.isUserAuthenticated()) {
                    val user = tokenManager.getCurrentUser()
                    val accessToken = tokenManager.getAccessToken()
                    
                    if (user != null && !accessToken.isNullOrEmpty()) {
                        _authState.value = AuthState.Authenticated(
                            user = user,
                            accessToken = accessToken
                        )
                    } else {
                        // Invalid stored data, clear tokens
                        tokenManager.clearUserTokens()
                        _authState.value = AuthState.Idle
                    }
                } else {
                    _authState.value = AuthState.Idle
                }
            } catch (e: Exception) {
                // Error accessing stored data, clear and start fresh
                try {
                    tokenManager.clearUserTokens()
                } catch (clearException: Exception) {
                    // Ignore clear errors
                }
                _authState.value = AuthState.Idle
            }
        }
    }
    
    /**
     * Start LINE login process - sets loading state
     */
    fun startLineLogin() {
        _authState.value = AuthState.Loading
    }

    /**
     * Handle LINE login result from UI layer
     */
    fun handleLineLoginResult(accessToken: String?, error: String?) {
        viewModelScope.launch {
            if (error != null) {
                _authState.value = AuthState.Error("Authentication error: $error")
            } else if (accessToken != null) {
                // We have the access token from LINE SDK
                // Now send it to backend to verify and create session
                loginWithLineToken(accessToken)
            } else {
                _authState.value = AuthState.Error("Failed to get LINE authentication data")
            }
        }
    }

    private suspend fun loginWithLineToken(accessToken: String) {
        when (val result = authRepository.loginWithLineToken(accessToken)) {
            is Result.Success -> {
                _authState.value = AuthState.Authenticated(
                    user = result.data.user,
                    accessToken = result.data.accessToken
                )
            }
            is Result.Error -> {
                _authState.value = AuthState.Error(result.error.toMessage())
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Idle
        }
    }
}
