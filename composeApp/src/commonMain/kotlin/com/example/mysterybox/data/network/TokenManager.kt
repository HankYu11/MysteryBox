package com.example.mysterybox.data.network

import com.example.mysterybox.data.model.Merchant
import com.example.mysterybox.data.model.User
import com.example.mysterybox.data.storage.TokenStorage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TokenManager(private val tokenStorage: TokenStorage) {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    suspend fun saveUserSession(accessToken: String, refreshToken: String, user: User) {
        tokenStorage.saveUserTokens(accessToken, refreshToken)
        tokenStorage.saveUserData(json.encodeToString(user))
    }

    suspend fun saveMerchantSession(token: String, merchant: Merchant) {
        tokenStorage.saveMerchantToken(token)
        tokenStorage.saveMerchantData(json.encodeToString(merchant))
    }

    suspend fun getAccessToken(): String? = tokenStorage.getUserAccessToken()

    suspend fun getRefreshToken(): String? = tokenStorage.getUserRefreshToken()

    suspend fun getMerchantToken(): String? = tokenStorage.getMerchantToken()
    
    suspend fun getCurrentUser(): User? {
        return try {
            tokenStorage.getUserData()?.let { userData ->
                json.decodeFromString<User>(userData)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getCurrentMerchant(): Merchant? {
        return try {
            tokenStorage.getMerchantData()?.let { merchantData ->
                json.decodeFromString<Merchant>(merchantData)
            }
        } catch (e: Exception) {
            null
        }
    }

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
