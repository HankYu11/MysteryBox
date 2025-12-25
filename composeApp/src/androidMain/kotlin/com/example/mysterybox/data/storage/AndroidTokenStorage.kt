package com.example.mysterybox.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    // Reactive flows for token changes
    private val _accessTokenFlow = MutableStateFlow(sharedPreferences.getString(KEY_USER_ACCESS_TOKEN, null))
    override val accessTokenFlow: Flow<String?> = _accessTokenFlow.asStateFlow()

    private val _refreshTokenFlow = MutableStateFlow(sharedPreferences.getString(KEY_USER_REFRESH_TOKEN, null))
    override val refreshTokenFlow: Flow<String?> = _refreshTokenFlow.asStateFlow()

    private val _userDataFlow = MutableStateFlow(sharedPreferences.getString(KEY_USER_DATA, null))
    override val userDataFlow: Flow<String?> = _userDataFlow.asStateFlow()

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPreferences.edit()
            .putString(KEY_USER_ACCESS_TOKEN, accessToken)
            .putString(KEY_USER_REFRESH_TOKEN, refreshToken)
            .commit()
        _accessTokenFlow.value = accessToken
        _refreshTokenFlow.value = refreshToken
    }

    override suspend fun saveMerchantToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_MERCHANT_TOKEN, token)
            .commit()
    }

    override suspend fun saveUserData(userData: String) {
        sharedPreferences.edit()
            .putString(KEY_USER_DATA, userData)
            .commit()
        _userDataFlow.value = userData
    }

    override suspend fun saveMerchantData(merchantData: String) {
        sharedPreferences.edit()
            .putString(KEY_MERCHANT_DATA, merchantData)
            .commit()
    }

    override suspend fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_USER_ACCESS_TOKEN, null)
    }

    override suspend fun getRefreshToken(): String? {
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

    override suspend fun clearTokens() {
        sharedPreferences.edit()
            .remove(KEY_USER_ACCESS_TOKEN)
            .remove(KEY_USER_REFRESH_TOKEN)
            .remove(KEY_USER_DATA)
            .commit()
        _accessTokenFlow.value = null
        _refreshTokenFlow.value = null
        _userDataFlow.value = null
    }

    override suspend fun clearMerchantToken() {
        sharedPreferences.edit()
            .remove(KEY_MERCHANT_TOKEN)
            .remove(KEY_MERCHANT_DATA)
            .commit()
    }

    companion object {
        private const val KEY_USER_ACCESS_TOKEN = "user_access_token"
        private const val KEY_USER_REFRESH_TOKEN = "user_refresh_token"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_MERCHANT_TOKEN = "merchant_token"
        private const val KEY_MERCHANT_DATA = "merchant_data"
    }
}
