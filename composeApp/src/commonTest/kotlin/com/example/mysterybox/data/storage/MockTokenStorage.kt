package com.example.mysterybox.data.storage

class MockTokenStorage : TokenStorage {
    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var merchantToken: String? = null

    override suspend fun saveUserTokens(accessToken: String, refreshToken: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    override suspend fun saveMerchantToken(token: String) {
        this.merchantToken = token
    }

    override suspend fun getUserAccessToken(): String? = accessToken

    override suspend fun getUserRefreshToken(): String? = refreshToken

    override suspend fun getMerchantToken(): String? = merchantToken

    override suspend fun clearUserTokens() {
        accessToken = null
        refreshToken = null
    }

    override suspend fun clearMerchantToken() {
        merchantToken = null
    }

    override suspend fun clearAllTokens() {
        accessToken = null
        refreshToken = null
        merchantToken = null
    }
}