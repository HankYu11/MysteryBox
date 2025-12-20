package com.example.mysterybox.data.repository

import com.example.mysterybox.data.model.AuthSession
import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.model.User

interface AuthRepository {
    suspend fun loginWithLineToken(accessToken: String): Result<AuthSession>
    suspend fun logout(): Result<Unit>
    suspend fun refreshToken(): Result<AuthSession>
    suspend fun getCurrentUser(): Result<User>
}
