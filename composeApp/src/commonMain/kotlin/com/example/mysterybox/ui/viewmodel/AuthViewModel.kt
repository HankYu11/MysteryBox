package com.example.mysterybox.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mysterybox.auth.createBrowserLauncher
import com.example.mysterybox.auth.generateSecureState
import com.example.mysterybox.data.model.AuthState
import com.example.mysterybox.data.model.OAuthCallbackResult
import com.example.mysterybox.data.network.ApiConfig
import com.example.mysterybox.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState

    private val _isProcessingCallback = MutableStateFlow(false)
    val isProcessingCallback: StateFlow<Boolean> = _isProcessingCallback.asStateFlow()

    fun startLineLogin() {
        val state = generateSecureState()
        authRepository.storeState(state)

        val browserLauncher = createBrowserLauncher()
        browserLauncher.launchLineAuth(
            channelId = ApiConfig.LINE_CHANNEL_ID,
            redirectUri = ApiConfig.REDIRECT_URI,
            state = state,
            scope = ApiConfig.LINE_SCOPE
        )
    }

    fun handleOAuthCallback(callback: OAuthCallbackResult) {
        if (_isProcessingCallback.value) return

        viewModelScope.launch {
            _isProcessingCallback.value = true

            try {
                when {
                    callback.error != null -> {
                        // OAuth error from LINE - handled by auth state
                    }
                    callback.code != null && callback.state != null -> {
                        if (authRepository.validateState(callback.state)) {
                            authRepository.exchangeLineCode(callback.code, callback.state)
                        } else {
                            // State mismatch - possible CSRF attack, ignore
                        }
                    }
                }
            } finally {
                _isProcessingCallback.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun isAuthenticated(): Boolean = authRepository.isAuthenticated()
}
