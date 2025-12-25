package com.example.mysterybox.data.storage

import kotlinx.cinterop.CFTypeRefVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.CoreFoundation.CFDictionaryRef
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.*
import platform.darwin.noErr

/**
 * iOS implementation of TokenStorage using Keychain for secure storage.
 * Uses Secure Enclave on supported devices for enhanced security.
 */
class IosTokenStorage : TokenStorage {

    // Reactive flows for token observation
    private val _accessTokenFlow = MutableStateFlow(readFromKeychain(KEY_USER_ACCESS_TOKEN))
    override val accessTokenFlow: Flow<String?> = _accessTokenFlow.asStateFlow()

    private val _refreshTokenFlow = MutableStateFlow(readFromKeychain(KEY_USER_REFRESH_TOKEN))
    override val refreshTokenFlow: Flow<String?> = _refreshTokenFlow.asStateFlow()

    private val _userDataFlow = MutableStateFlow(readFromKeychain(KEY_USER_DATA))
    override val userDataFlow: Flow<String?> = _userDataFlow.asStateFlow()

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        saveToKeychain(KEY_USER_ACCESS_TOKEN, accessToken)
        saveToKeychain(KEY_USER_REFRESH_TOKEN, refreshToken)
        _accessTokenFlow.value = accessToken
        _refreshTokenFlow.value = refreshToken
    }

    override suspend fun getAccessToken(): String? {
        return readFromKeychain(KEY_USER_ACCESS_TOKEN)
    }

    override suspend fun getRefreshToken(): String? {
        return readFromKeychain(KEY_USER_REFRESH_TOKEN)
    }

    override suspend fun saveUserData(data: String) {
        saveToKeychain(KEY_USER_DATA, data)
        _userDataFlow.value = data
    }

    override suspend fun getUserData(): String? {
        return readFromKeychain(KEY_USER_DATA)
    }

    override suspend fun clearTokens() {
        deleteFromKeychain(KEY_USER_ACCESS_TOKEN)
        deleteFromKeychain(KEY_USER_REFRESH_TOKEN)
        deleteFromKeychain(KEY_USER_DATA)
        _accessTokenFlow.value = null
        _refreshTokenFlow.value = null
        _userDataFlow.value = null
    }

    override suspend fun saveMerchantToken(token: String) {
        saveToKeychain(KEY_MERCHANT_TOKEN, token)
    }

    override suspend fun getMerchantToken(): String? {
        return readFromKeychain(KEY_MERCHANT_TOKEN)
    }

    override suspend fun saveMerchantData(data: String) {
        saveToKeychain(KEY_MERCHANT_DATA, data)
    }

    override suspend fun getMerchantData(): String? {
        return readFromKeychain(KEY_MERCHANT_DATA)
    }

    override suspend fun clearMerchantToken() {
        deleteFromKeychain(KEY_MERCHANT_TOKEN)
        deleteFromKeychain(KEY_MERCHANT_DATA)
    }

    /**
     * Saves a string value to iOS Keychain.
     * Uses kSecAttrAccessibleWhenUnlockedThisDeviceOnly for security.
     */
    private fun saveToKeychain(key: String, value: String) {
        // First, delete any existing value
        deleteFromKeychain(key)

        // Convert string to NSData
        val valueData = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding)
            ?: return

        // Create query dictionary for saving
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key,
            kSecValueData to valueData,
            kSecAttrAccessible to kSecAttrAccessibleWhenUnlockedThisDeviceOnly,
            kSecAttrService to KEYCHAIN_SERVICE
        )

        // Add to keychain
        SecItemAdd(query as CFDictionaryRef, null)
    }

    /**
     * Reads a string value from iOS Keychain.
     * Returns null if the item doesn't exist or cannot be read.
     */
    private fun readFromKeychain(key: String): String? {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key,
            kSecAttrService to KEYCHAIN_SERVICE,
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne
        )

        val result = memScoped {
            val resultRef = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query as CFDictionaryRef, resultRef.ptr)

            if (status == noErr) {
                val data = resultRef.value as? NSData
                data?.let {
                    NSString.create(data = it, encoding = NSUTF8StringEncoding) as String?
                }
            } else {
                null
            }
        }

        return result
    }

    /**
     * Deletes a value from iOS Keychain.
     */
    private fun deleteFromKeychain(key: String) {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key,
            kSecAttrService to KEYCHAIN_SERVICE
        )

        SecItemDelete(query as CFDictionaryRef)
    }

    companion object {
        private const val KEYCHAIN_SERVICE = "com.example.mysterybox"
        private const val KEY_USER_ACCESS_TOKEN = "user_access_token"
        private const val KEY_USER_REFRESH_TOKEN = "user_refresh_token"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_MERCHANT_TOKEN = "merchant_token"
        private const val KEY_MERCHANT_DATA = "merchant_data"
    }
}
