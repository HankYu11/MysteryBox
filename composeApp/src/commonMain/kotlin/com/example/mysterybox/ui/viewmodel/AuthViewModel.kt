package com.example.mysterybox.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mysterybox.data.model.AuthState
import com.example.mysterybox.data.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState

    fun startLineLogin() {
        authRepository.startLineLogin()
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun isAuthenticated(): Boolean = authRepository.isAuthenticated()
}
