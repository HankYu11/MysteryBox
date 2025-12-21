package com.example.mysterybox.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mysterybox.data.auth.AuthManager
import com.example.mysterybox.ui.state.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state specific to the Profile screen.
 * Manages logout loading state independently from global auth state.
 */
sealed class ProfileUiState {
    data object Idle : ProfileUiState()
    data object LoggingOut : ProfileUiState()
    data object LogoutSuccess : ProfileUiState()
    data class LogoutError(val message: String) : ProfileUiState()
}

/**
 * ViewModel for the Profile screen.
 * Exposes global authentication state for displaying user data,
 * and manages screen-specific logout loading state.
 */
class ProfileViewModel(
    private val authManager: AuthManager
) : ViewModel() {

    /**
     * Global authentication state from AuthManager.
     * Used to display user profile information.
     */
    val authState: StateFlow<AuthState> = authManager.authState

    private val _profileState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    /**
     * Logout the current user.
     * Sets loading state, calls AuthManager to logout, then emits result.
     */
    fun logout() {
        viewModelScope.launch {
            _profileState.value = ProfileUiState.LoggingOut
            when (val result = authManager.logout()) {
                is com.example.mysterybox.data.model.Result.Success -> {
                    _profileState.value = ProfileUiState.LogoutSuccess
                }
                is com.example.mysterybox.data.model.Result.Error -> {
                    _profileState.value = ProfileUiState.LogoutError(result.error.toMessage())
                }
            }
        }
    }

    /**
     * Reset profile state to idle.
     * Call this after handling logout success/error.
     */
    fun resetState() {
        _profileState.value = ProfileUiState.Idle
    }
}
