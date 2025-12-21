package com.example.mysterybox.data.auth

import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.model.User
import com.example.mysterybox.data.repository.AuthRepository
import com.example.mysterybox.ui.state.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AuthManager serves as a singleton to manage app-wide authentication state.
 * It provides a single source of truth for authentication status that can be
 * observed by any ViewModel or composable in the application.
 */
class AuthManager(
    private val authRepository: AuthRepository
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /**
     * Initialize authentication state by checking for existing session.
     * Should be called once on app startup.
     */
    suspend fun initialize() {
        when (val result = authRepository.getCurrentSession()) {
            is Result.Success -> {
                val session = result.data
                if (session != null) {
                    _authState.value = AuthState.Authenticated(
                        user = session.user,
                        accessToken = session.accessToken
                    )
                } else {
                    _authState.value = AuthState.Idle
                }
            }
            is Result.Error -> {
                _authState.value = AuthState.Idle
            }
        }
    }

    /**
     * Update auth state to authenticated with user and token.
     * Called by ViewModels after successful login.
     */
    fun setAuthenticated(user: User, accessToken: String) {
        _authState.value = AuthState.Authenticated(user, accessToken)
    }

    /**
     * Clear authentication state and logout from backend.
     * Called by ViewModels when user logs out.
     * Returns Result indicating success or failure.
     */
    suspend fun logout(): Result<Unit> {
        val result = authRepository.logout()
        if (result is Result.Success) {
            _authState.value = AuthState.Idle
        }
        return result
    }

    /**
     * Get currently authenticated user, if any.
     * Returns null if not authenticated.
     */
    fun getCurrentUser(): User? {
        return (_authState.value as? AuthState.Authenticated)?.user
    }
}
