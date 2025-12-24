package com.example.mysterybox.ui.state

import com.example.mysterybox.data.model.User

sealed class AuthState {
    data object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data object Unauthenticated : AuthState()
}
