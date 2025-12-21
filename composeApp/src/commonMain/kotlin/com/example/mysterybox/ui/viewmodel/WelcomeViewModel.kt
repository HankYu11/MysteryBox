package com.example.mysterybox.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mysterybox.data.auth.AuthManager
import com.example.mysterybox.ui.state.AuthState
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for the Welcome screen.
 * Simply observes global authentication state from AuthManager.
 * No screen-specific state needed - navigation is triggered by AuthState.Authenticated.
 */
class WelcomeViewModel(
    private val authManager: AuthManager
) : ViewModel() {

    /**
     * Global authentication state from AuthManager.
     * WelcomeScreen observes this to navigate to Home when user is authenticated.
     */
    val authState: StateFlow<AuthState> = authManager.authState
}
