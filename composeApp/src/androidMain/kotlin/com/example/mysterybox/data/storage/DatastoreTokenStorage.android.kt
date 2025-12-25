package com.example.mysterybox.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * DataStore instance with migration from legacy SharedPreferences.
 * File-level encryption is provided by Android's File-Based Encryption (FBE)
 * which is enabled by default on Android 7.0+ devices.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "token_prefs"
)

internal actual fun createDataStore(): DataStore<Preferences> {
    return com.example.mysterybox.di.KoinApp.get().dataStore
}

/**
 * Migrates data from legacy SharedPreferences to DataStore and cleans up old files.
 * Call this once during app initialization.
 */
suspend fun cleanupLegacySharedPreferences(context: Context, dataStore: DataStore<Preferences>) {
    try {
        // Check if we need to migrate
        val currentData = dataStore.data.first()
        if (currentData.asMap().isNotEmpty()) {
            // DataStore already has data, just clean up old files
            cleanupOldPreferences(context)
            return
        }

        // Attempt migration from legacy SharedPreferences
        val legacyPrefsNames = listOf(
            "mystery_box_secure_tokens",
            "mystery_box_tokens" // Fallback unencrypted variant
        )

        var migrated = false
        for (prefsName in legacyPrefsNames) {
            try {
                val sharedPrefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                val allEntries = sharedPrefs.all

                if (allEntries.isNotEmpty()) {
                    // Migrate to DataStore
                    dataStore.edit { prefs ->
                        allEntries.forEach { (key, value) ->
                            if (value is String) {
                                prefs[stringPreferencesKey(key)] = value
                            }
                        }
                    }
                    migrated = true
                    println("Successfully migrated ${allEntries.size} tokens from $prefsName to DataStore")
                    break
                }
            } catch (e: Exception) {
                println("Warning: Failed to migrate from $prefsName - ${e.message}")
            }
        }

        // Clean up old files after migration
        if (migrated) {
            cleanupOldPreferences(context)
        }
    } catch (e: Exception) {
        println("Warning: Migration/cleanup failed - ${e.message}")
    }
}

private fun cleanupOldPreferences(context: Context) {
    val legacyPrefsNames = listOf(
        "mystery_box_secure_tokens",
        "mystery_box_tokens"
    )

    legacyPrefsNames.forEach { prefsName ->
        try {
            context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
            context.deleteSharedPreferences(prefsName)
            println("Cleaned up legacy SharedPreferences: $prefsName")
        } catch (e: Exception) {
            // Ignore cleanup errors - not critical
            println("Warning: Failed to cleanup $prefsName - ${e.message}")
        }
    }
}
