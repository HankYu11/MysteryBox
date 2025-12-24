package com.example.mysterybox.data.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Mock implementation of TokenStorage for testing.
 * Uses in-memory storage with reactive flows.
 */
class MockTokenStorage : TokenStorage {
    private val _accessToken = MutableStateFlow<String?>(null)
    private val _refreshToken = MutableStateFlow<String?>(null)
    private val _userData = MutableStateFlow<String?>(null)
    private var merchantToken: String? = null
    private var merchantData: String? = null

    // User Authentication - Flows
    override val accessTokenFlow: Flow<String?> = _accessToken.asStateFlow()
    override val refreshTokenFlow: Flow<String?> = _refreshToken.asStateFlow()
    override val userDataFlow: Flow<String?> = _userData.asStateFlow()

    // User Authentication - Methods
    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        _accessToken.value = accessToken
        _refreshToken.value = refreshToken
    }

    override suspend fun getAccessToken(): String? = _accessToken.value

    override suspend fun getRefreshToken(): String? = _refreshToken.value

    override suspend fun saveUserData(data: String) {
        _userData.value = data
    }

    override suspend fun getUserData(): String? = _userData.value

    override suspend fun clearTokens() {
        _accessToken.value = null
        _refreshToken.value = null
        _userData.value = null
    }

    // Merchant Authentication - Methods
    override suspend fun saveMerchantToken(token: String) {
        merchantToken = token
    }

    override suspend fun getMerchantToken(): String? = merchantToken

    override suspend fun saveMerchantData(data: String) {
        merchantData = data
    }

    override suspend fun getMerchantData(): String? = merchantData

    override suspend fun clearMerchantToken() {
        merchantToken = null
        merchantData = null
    }
}