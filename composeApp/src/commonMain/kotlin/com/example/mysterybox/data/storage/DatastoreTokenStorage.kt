package com.example.mysterybox.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal expect fun createDataStore(): DataStore<Preferences>

class DatastoreTokenStorage(private val dataStore: DataStore<Preferences>) : TokenStorage {

    // User Authentication Keys
    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")
    private val userDataKey = stringPreferencesKey("user_data")

    // Merchant Authentication Keys
    private val merchantTokenKey = stringPreferencesKey("merchant_token")
    private val merchantDataKey = stringPreferencesKey("merchant_data")

    // User Authentication - Flows
    override val accessTokenFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[accessTokenKey]
    }

    override val refreshTokenFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[refreshTokenKey]
    }

    override val userDataFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[userDataKey]
    }

    // User Authentication - Methods
    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit {
            it[accessTokenKey] = accessToken
            it[refreshTokenKey] = refreshToken
        }
    }

    override suspend fun getAccessToken(): String? {
        return dataStore.data.map { it[accessTokenKey] }.first()
    }

    override suspend fun getRefreshToken(): String? {
        return dataStore.data.map { it[refreshTokenKey] }.first()
    }

    override suspend fun saveUserData(data: String) {
        dataStore.edit {
            it[userDataKey] = data
        }
    }

    override suspend fun getUserData(): String? {
        return dataStore.data.map { it[userDataKey] }.first()
    }

    override suspend fun clearTokens() {
        dataStore.edit {
            it.remove(accessTokenKey)
            it.remove(refreshTokenKey)
            it.remove(userDataKey)
        }
    }

    // Merchant Authentication - Methods
    override suspend fun saveMerchantToken(token: String) {
        dataStore.edit {
            it[merchantTokenKey] = token
        }
    }

    override suspend fun getMerchantToken(): String? {
        return dataStore.data.map { it[merchantTokenKey] }.first()
    }

    override suspend fun saveMerchantData(data: String) {
        dataStore.edit {
            it[merchantDataKey] = data
        }
    }

    override suspend fun getMerchantData(): String? {
        return dataStore.data.map { it[merchantDataKey] }.first()
    }

    override suspend fun clearMerchantToken() {
        dataStore.edit {
            it.remove(merchantTokenKey)
            it.remove(merchantDataKey)
        }
    }
}
