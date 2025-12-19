package com.example.mysterybox.data.network

import com.example.mysterybox.data.storage.TokenStorage

class TokenManager(private val tokenStorage: TokenStorage) {
    
    suspend fun saveUserTokens(accessToken: String, refreshToken: String) {
        tokenStorage.saveUserTokens(accessToken, refreshToken)
    }

    suspend fun saveMerchantToken(token: String) {
        tokenStorage.saveMerchantToken(token)
    }

    suspend fun getAccessToken(): String? = tokenStorage.getUserAccessToken()

    suspend fun getRefreshToken(): String? = tokenStorage.getUserRefreshToken()

    suspend fun getMerchantToken(): String? = tokenStorage.getMerchantToken()

    suspend fun clearUserTokens() {
        tokenStorage.clearUserTokens()
    }

    suspend fun clearMerchantToken() {
        tokenStorage.clearMerchantToken()
    }

    suspend fun clearAllTokens() {
        tokenStorage.clearAllTokens()
    }

    suspend fun isUserAuthenticated(): Boolean = tokenStorage.getUserAccessToken() != null

    suspend fun isMerchantAuthenticated(): Boolean = tokenStorage.getMerchantToken() != null
}
