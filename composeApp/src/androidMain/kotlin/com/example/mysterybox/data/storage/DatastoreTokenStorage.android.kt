package com.example.mysterybox.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * DataStore instance for secure token storage on Android.
 *
 * Security: File-level encryption is provided by Android's File-Based Encryption (FBE)
 * which is enabled by default on Android 7.0+ devices. All data is encrypted at rest.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "token_prefs"
)

internal actual fun createDataStore(): DataStore<Preferences> {
    return com.example.mysterybox.di.KoinApp.get().dataStore
}
