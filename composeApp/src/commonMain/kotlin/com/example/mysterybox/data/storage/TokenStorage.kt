package com.example.mysterybox.data.storage

interface TokenStorage {
    suspend fun saveUserTokens(accessToken: String, refreshToken: String)
    suspend fun saveMerchantToken(token: String)
    suspend fun saveUserData(userData: String) // JSON serialized User data
    suspend fun saveMerchantData(merchantData: String) // JSON serialized Merchant data
    suspend fun getUserAccessToken(): String?
    suspend fun getUserRefreshToken(): String?
    suspend fun getMerchantToken(): String?
    suspend fun getUserData(): String? // JSON serialized User data
    suspend fun getMerchantData(): String? // JSON serialized Merchant data
    suspend fun clearUserTokens()
    suspend fun clearMerchantToken()
    suspend fun clearAllTokens()
}