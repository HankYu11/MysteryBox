package com.example.mysterybox.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal expect fun createDataStore(): DataStore<Preferences>

class DatastoreTokenStorage(private val dataStore: DataStore<Preferences>) : TokenStorage {

    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")

    override val accessTokenFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[accessTokenKey]
    }

    override val refreshTokenFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[refreshTokenKey]
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit {
            it[accessTokenKey] = accessToken
            it[refreshTokenKey] = refreshToken
        }
    }

    override suspend fun getAccessToken(): String? {
        var accessToken: String? = null
        dataStore.edit {
            accessToken = it[accessTokenKey]
        }
        return accessToken
    }

    override suspend fun getRefreshToken(): String? {
        var refreshToken: String? = null
        dataStore.edit {
            refreshToken = it[refreshTokenKey]
        }
        return refreshToken
    }

    override suspend fun clearTokens() {
        dataStore.edit {
            it.remove(accessTokenKey)
            it.remove(refreshTokenKey)
        }
    }
}
