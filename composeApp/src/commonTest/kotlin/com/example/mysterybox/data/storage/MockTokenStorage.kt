package com.example.mysterybox.data.storage

class MockTokenStorage : TokenStorage {
    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var merchantToken: String? = null
    private var userData: String? = null
    private var merchantData: String? = null

    override suspend fun saveUserTokens(accessToken: String, refreshToken: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    override suspend fun saveMerchantToken(token: String) {
        this.merchantToken = token
    }

    override suspend fun saveUserData(userData: String) {
        this.userData = userData
    }

    override suspend fun saveMerchantData(merchantData: String) {
        this.merchantData = merchantData
    }

    override suspend fun getUserAccessToken(): String? = accessToken

    override suspend fun getUserRefreshToken(): String? = refreshToken

    override suspend fun getMerchantToken(): String? = merchantToken

    override suspend fun getUserData(): String? = userData

    override suspend fun getMerchantData(): String? = merchantData

    override suspend fun clearUserTokens() {
        accessToken = null
        refreshToken = null
        userData = null
    }

    override suspend fun clearMerchantToken() {
        merchantToken = null
        merchantData = null
    }

    override suspend fun clearAllTokens() {
        accessToken = null
        refreshToken = null
        userData = null
        merchantToken = null
        merchantData = null
    }
}