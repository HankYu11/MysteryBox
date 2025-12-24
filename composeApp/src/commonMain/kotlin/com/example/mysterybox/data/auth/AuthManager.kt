package com.example.mysterybox.data.auth

import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.model.User
import com.example.mysterybox.data.repository.AuthRepository
import com.example.mysterybox.data.storage.TokenStorage
import com.example.mysterybox.ui.state.AuthState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json

/**
 * AuthManager serves as the single source of truth for authentication state.
 * It reactively combines token and user data from TokenStorage to provide
 * app-wide authentication state that can be observed by ViewModels and UI.
 */
class AuthManager(
    private val tokenStorage: TokenStorage,
    private val authRepository: AuthRepository,
    private val json: Json
) {

    /**
     * Reactive authentication state that combines accessToken and userData flows.
     * Automatically updates when tokens or user data changes in storage.
     */
    val authState: StateFlow<AuthState> =
        combine(
            tokenStorage.accessTokenFlow,
            tokenStorage.userDataFlow
        ) { accessToken, userDataJson ->
            if (accessToken != null && userDataJson != null) {
                try {
                    // Deserialize user data from JSON
                    val user = json.decodeFromString<User>(userDataJson)
                    AuthState.Authenticated(user)
                } catch (e: Exception) {
                    // Invalid user data, treat as unauthenticated
                    AuthState.Unauthenticated
                }
            } else {
                AuthState.Unauthenticated
            }
        }.stateIn(
            scope = CoroutineScope(Dispatchers.Main),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthState.Loading
        )

    /**
     * Initialize authentication state on app startup.
     * Checks if stored tokens are valid and refreshes if needed.
     * Updates user data from backend if tokens exist.
     */
    suspend fun initialize() {
        val accessToken = tokenStorage.getAccessToken()

        if (accessToken != null) {
            // We have a token, try to get/refresh user data
            when (val result = authRepository.getCurrentUser()) {
                is Result.Success -> {
                    // User data fetched successfully, TokenStorage already updated by repository
                    // AuthState will update reactively via userDataFlow
                }
                is Result.Error -> {
                    // Token might be invalid, try to refresh
                    when (val refreshResult = authRepository.refreshToken()) {
                        is Result.Success -> {
                            // Token refreshed, try getting user again
                            authRepository.getCurrentUser()
                        }
                        is Result.Error -> {
                            // Refresh failed, clear invalid tokens
                            tokenStorage.clearTokens()
                        }
                    }
                }
            }
        }
    }

    /**
     * Logout the current user.
     * Calls backend logout endpoint and clears local tokens.
     * @return Result.Success if logout completed (even if backend call failed),
     *         or Result.Error only if there's a critical error
     */
    suspend fun logout(): Result<Unit> {
        return authRepository.logout()
    }

    /**
     * Get the current authenticated user, if any.
     * @return User object if authenticated, null otherwise
     */
    fun getCurrentUser(): User? {
        return when (val state = authState.value) {
            is AuthState.Authenticated -> state.user
            else -> null
        }
    }
}
