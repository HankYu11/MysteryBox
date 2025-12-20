package com.example.mysterybox.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AndroidTokenStorage(private val context: Context) : TokenStorage {

    private val sharedPreferences: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "mystery_box_secure_tokens",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to regular SharedPreferences if encryption fails
            context.getSharedPreferences("mystery_box_tokens", Context.MODE_PRIVATE)
        }
    }

    override suspend fun saveUserTokens(accessToken: String, refreshToken: String) {
        sharedPreferences.edit()
            .putString(KEY_USER_ACCESS_TOKEN, accessToken)
            .putString(KEY_USER_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    override suspend fun saveMerchantToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_MERCHANT_TOKEN, token)
            .apply()
    }

    override suspend fun saveUserData(userData: String) {
        sharedPreferences.edit()
            .putString(KEY_USER_DATA, userData)
            .apply()
    }

    override suspend fun saveMerchantData(merchantData: String) {
        sharedPreferences.edit()
            .putString(KEY_MERCHANT_DATA, merchantData)
            .apply()
    }

    override suspend fun getUserAccessToken(): String? {
        return sharedPreferences.getString(KEY_USER_ACCESS_TOKEN, null)
    }

    override suspend fun getUserRefreshToken(): String? {
        return sharedPreferences.getString(KEY_USER_REFRESH_TOKEN, null)
    }

    override suspend fun getMerchantToken(): String? {
        return sharedPreferences.getString(KEY_MERCHANT_TOKEN, null)
    }

    override suspend fun getUserData(): String? {
        return sharedPreferences.getString(KEY_USER_DATA, null)
    }

    override suspend fun getMerchantData(): String? {
        return sharedPreferences.getString(KEY_MERCHANT_DATA, null)
    }

    override suspend fun clearUserTokens() {
        sharedPreferences.edit()
            .remove(KEY_USER_ACCESS_TOKEN)
            .remove(KEY_USER_REFRESH_TOKEN)
            .remove(KEY_USER_DATA)
            .apply()
    }

    override suspend fun clearMerchantToken() {
        sharedPreferences.edit()
            .remove(KEY_MERCHANT_TOKEN)
            .remove(KEY_MERCHANT_DATA)
            .apply()
    }

    override suspend fun clearAllTokens() {
        sharedPreferences.edit()
            .remove(KEY_USER_ACCESS_TOKEN)
            .remove(KEY_USER_REFRESH_TOKEN)
            .remove(KEY_USER_DATA)
            .remove(KEY_MERCHANT_TOKEN)
            .remove(KEY_MERCHANT_DATA)
            .apply()
    }

    companion object {
        private const val KEY_USER_ACCESS_TOKEN = "user_access_token"
        private const val KEY_USER_REFRESH_TOKEN = "user_refresh_token"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_MERCHANT_TOKEN = "merchant_token"
        private const val KEY_MERCHANT_DATA = "merchant_data"
    }
}