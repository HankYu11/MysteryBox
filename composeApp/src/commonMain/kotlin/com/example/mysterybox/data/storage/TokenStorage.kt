package com.example.mysterybox.data.storage

import kotlinx.coroutines.flow.Flow

/**
 * TokenStorage provides secure storage for authentication tokens and user data.
 * Supports both user authentication (LINE login) and merchant authentication.
 */
interface TokenStorage {
    // User Authentication
    val accessTokenFlow: Flow<String?>
    val refreshTokenFlow: Flow<String?>
    val userDataFlow: Flow<String?>

    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?

    suspend fun saveUserData(data: String)
    suspend fun getUserData(): String?

    suspend fun clearTokens()

    // Merchant Authentication
    suspend fun saveMerchantToken(token: String)
    suspend fun getMerchantToken(): String?

    suspend fun saveMerchantData(data: String)
    suspend fun getMerchantData(): String?

    suspend fun clearMerchantToken()
}
