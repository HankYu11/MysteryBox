package com.example.mysterybox.data.repository

import com.example.mysterybox.data.model.AuthSession
import com.example.mysterybox.data.model.AuthState
import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>

    suspend fun exchangeLineCode(code: String, state: String): Result<AuthSession>

    fun getCurrentUser(): User?

    fun isAuthenticated(): Boolean

    suspend fun logout()

    fun validateState(receivedState: String): Boolean

    fun storeState(state: String)
}
