package com.example.mysterybox.data.network

class TokenManager {
    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var merchantToken: String? = null

    fun saveUserTokens(accessToken: String, refreshToken: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    fun saveMerchantToken(token: String) {
        this.merchantToken = token
    }

    fun getAccessToken(): String? = accessToken

    fun getRefreshToken(): String? = refreshToken

    fun getMerchantToken(): String? = merchantToken

    fun clearUserTokens() {
        accessToken = null
        refreshToken = null
    }

    fun clearMerchantToken() {
        merchantToken = null
    }

    fun clearAllTokens() {
        clearUserTokens()
        clearMerchantToken()
    }

    fun isUserAuthenticated(): Boolean = accessToken != null

    fun isMerchantAuthenticated(): Boolean = merchantToken != null
}
