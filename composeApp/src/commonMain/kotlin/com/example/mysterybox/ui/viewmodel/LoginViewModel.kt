package com.example.mysterybox.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state specific to the Login screen.
 * Isolated from global authentication state managed by AuthManager.
 */
sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

/**
 * ViewModel for the Login screen.
 * Manages LINE login flow with screen-specific loading and error states.
 * The authentication state is updated reactively through the AuthRepository.
 */
class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    /**
     * Start LINE login process - sets loading state.
     * Call this before launching LINE SDK.
     */
    fun startLineLogin() {
        _loginState.value = LoginUiState.Loading
    }

    /**
     * Handle LINE login result from LINE SDK.
     * @param accessToken The access token from LINE, or null if login failed
     * @param error Error message from LINE SDK, or null if no error
     */
    fun handleLineLoginResult(accessToken: String?, error: String?) {
        viewModelScope.launch {
            if (error != null) {
                _loginState.value = LoginUiState.Error("Authentication error: $error")
            } else if (accessToken != null) {
                loginWithLineToken(accessToken)
            } else {
                _loginState.value = LoginUiState.Error("Failed to get LINE authentication data")
            }
        }
    }

    /**
     * Send LINE access token to backend for verification and session creation.
     */
    private suspend fun loginWithLineToken(accessToken: String) {
        when (val result = authRepository.loginWithLineToken(accessToken)) {
            is Result.Success -> {
                // The repository handles saving tokens and AuthManager will react.
                // Just update the UI state for this screen.
                _loginState.value = LoginUiState.Success
            }
            is Result.Error -> {
                _loginState.value = LoginUiState.Error(result.error.toMessage())
            }
        }
    }

    /**
     * Reset the login state back to Idle.
     * Call this when navigating away from the login screen.
     */
    fun resetState() {
        _loginState.value = LoginUiState.Idle
    }
}
