package com.example.mysterybox.data.storage

interface TokenStorage {
    suspend fun saveUserTokens(accessToken: String, refreshToken: String)
    suspend fun saveMerchantToken(token: String)
    suspend fun getUserAccessToken(): String?
    suspend fun getUserRefreshToken(): String?
    suspend fun getMerchantToken(): String?
    suspend fun clearUserTokens()
    suspend fun clearMerchantToken()
    suspend fun clearAllTokens()
}