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
     * Initialize authentication state by verifying tokens with backend.
     * Should be called once on app startup.
     * This will:
     * 1. Check if tokens exist locally
     * 2. Verify token with backend (GET /api/auth/me)
     * 3. Auto-refresh if token expired
     * 4. Set auth state based on result
     */
    suspend fun initialize() {
        // First check if we have tokens locally
        val localSession = authRepository.getCurrentSession()
        if (localSession is Result.Success && localSession.data != null) {
            // We have tokens, now verify them with backend
            when (val result = authRepository.getCurrentUser()) {
                is Result.Success -> {
                    // Token is valid (or was refreshed automatically)
                    val session = authRepository.getCurrentSession()
                    if (session is Result.Success && session.data != null) {
                        _authState.value = AuthState.Authenticated(
                            user = result.data,
                            accessToken = session.data.accessToken
                        )
                    } else {
                        _authState.value = AuthState.Idle
                    }
                }
                is Result.Error -> {
                    // Token verification failed (refresh also failed)
                    _authState.value = AuthState.Idle
                }
            }
        } else {
            // No tokens found locally
            _authState.value = AuthState.Idle
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

    /**
     * Clear local session without calling backend logout.
     * Use this when session has expired or when token refresh fails.
     * This will prompt the user to login again.
     */
    suspend fun clearSession() {
        authRepository.logout() // This clears local tokens
        _authState.value = AuthState.Idle
    }

    /**
     * Check if tokens still exist, and update auth state if they don't.
     * Call this when you detect an authentication error to ensure UI state is updated.
     */
    suspend fun checkAndUpdateAuthState() {
        val session = authRepository.getCurrentSession()
        when (session) {
            is Result.Success -> {
                if (session.data == null) {
                    // No session exists, clear auth state
                    _authState.value = AuthState.Idle
                }
            }
            is Result.Error -> {
                // Error getting session, clear auth state
                _authState.value = AuthState.Idle
            }
        }
    }
}
