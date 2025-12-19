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
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    // Platform-specific launcher management
    private var lineSdkLauncher: ((callback: (String?, String?, String?, String?) -> Unit) -> Unit)? = null
    
    init {
        checkAuthenticationStatus()
    }
    
    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            try {
                if (tokenManager.isUserAuthenticated()) {
                    // Since getCurrentUser is not implemented, skip for now
                    // TODO: Implement getCurrentUser in AuthRepository
                    val accessToken = tokenManager.getAccessToken()
                    if (!accessToken.isNullOrEmpty()) {
                        // For now, just mark as idle until we implement proper user fetching
                        _authState.value = AuthState.Idle
                    }
                }
            } catch (e: Exception) {
                // Fail silently and remain in idle state
                _authState.value = AuthState.Idle
            }
        }
    }
    
    fun setOAuthLauncher(launcher: Any) {
        @Suppress("UNCHECKED_CAST")
        lineSdkLauncher = launcher as? ((callback: (String?, String?, String?, String?) -> Unit) -> Unit)
    }
    
    fun startLineLogin() {
        val launcher = lineSdkLauncher
        if (launcher == null) {
            _authState.value = AuthState.Error("LINE SDK not initialized")
            return
        }
        
        _authState.value = AuthState.Loading
        
        // Launch LINE SDK login
        launcher { accessToken, userId, displayName, error ->
            viewModelScope.launch {
                if (error != null) {
                    _authState.value = AuthState.Error("Authentication error: $error")
                } else if (accessToken != null && userId != null && displayName != null) {
                    // We have the access token from LINE SDK
                    // Now send it to backend to verify and create session
                    loginWithLineToken(accessToken, userId, displayName)
                } else {
                    _authState.value = AuthState.Error("Failed to get LINE authentication data")
                }
            }
        }
    }
    
    private suspend fun loginWithLineToken(accessToken: String, userId: String, displayName: String) {
        when (val result = authRepository.loginWithLineToken(accessToken, userId, displayName)) {
            is Result.Success -> {
                _currentUser.value = result.data.user
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
            _currentUser.value = null
            _authState.value = AuthState.Idle
        }
    }

    fun isAuthenticated(): Boolean = _currentUser.value != null
    
    fun getCurrentUser(): User? = _currentUser.value
}
