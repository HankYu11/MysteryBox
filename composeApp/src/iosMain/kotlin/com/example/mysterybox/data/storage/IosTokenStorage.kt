package com.example.mysterybox.data.storage

import platform.Foundation.NSUserDefaults

class IosTokenStorage : TokenStorage {
    
    private val userDefaults = NSUserDefaults.standardUserDefaults

    override suspend fun saveUserTokens(accessToken: String, refreshToken: String) {
        userDefaults.setObject(accessToken, forKey = KEY_USER_ACCESS_TOKEN)
        userDefaults.setObject(refreshToken, forKey = KEY_USER_REFRESH_TOKEN)
        userDefaults.synchronize()
    }

    override suspend fun saveMerchantToken(token: String) {
        userDefaults.setObject(token, forKey = KEY_MERCHANT_TOKEN)
        userDefaults.synchronize()
    }

    override suspend fun getUserAccessToken(): String? {
        return userDefaults.stringForKey(KEY_USER_ACCESS_TOKEN)
    }

    override suspend fun getUserRefreshToken(): String? {
        return userDefaults.stringForKey(KEY_USER_REFRESH_TOKEN)
    }

    override suspend fun getMerchantToken(): String? {
        return userDefaults.stringForKey(KEY_MERCHANT_TOKEN)
    }

    override suspend fun clearUserTokens() {
        userDefaults.removeObjectForKey(KEY_USER_ACCESS_TOKEN)
        userDefaults.removeObjectForKey(KEY_USER_REFRESH_TOKEN)
        userDefaults.synchronize()
    }

    override suspend fun clearMerchantToken() {
        userDefaults.removeObjectForKey(KEY_MERCHANT_TOKEN)
        userDefaults.synchronize()
    }

    override suspend fun clearAllTokens() {
        userDefaults.removeObjectForKey(KEY_USER_ACCESS_TOKEN)
        userDefaults.removeObjectForKey(KEY_USER_REFRESH_TOKEN)
        userDefaults.removeObjectForKey(KEY_MERCHANT_TOKEN)
        userDefaults.synchronize()
    }

    companion object {
        private const val KEY_USER_ACCESS_TOKEN = "user_access_token"
        private const val KEY_USER_REFRESH_TOKEN = "user_refresh_token"
        private const val KEY_MERCHANT_TOKEN = "merchant_token"
    }
}