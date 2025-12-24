package com.example.mysterybox.data.auth

import com.example.mysterybox.data.storage.TokenStorage
import com.example.mysterybox.ui.state.AuthState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AuthManager(tokenStorage: TokenStorage) {

    // TODO: This is a temporary implementation for Commit 2
    // Will be properly enhanced in Commit 5 to combine accessTokenFlow + userDataFlow
    // and deserialize User from stored data
    val authState: StateFlow<AuthState> =
        tokenStorage.accessTokenFlow.map { accessToken ->
            if (accessToken != null) {
                // Temporary: Return Unauthenticated until Commit 5 enhances this
                AuthState.Unauthenticated
            } else {
                AuthState.Unauthenticated
            }
        }.stateIn(
            scope = CoroutineScope(Dispatchers.Main),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthState.Loading
        )
}
