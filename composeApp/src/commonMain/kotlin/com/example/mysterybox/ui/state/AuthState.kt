package com.example.mysterybox.ui.state

import com.example.mysterybox.data.model.User

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Authenticated(val user: User, val accessToken: String) : AuthState()
    data class Error(val message: String, val code: String? = null) : AuthState()
}