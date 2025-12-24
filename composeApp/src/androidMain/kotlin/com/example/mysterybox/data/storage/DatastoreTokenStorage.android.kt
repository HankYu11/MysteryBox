package com.example.mysterybox.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token_prefs")

internal actual fun createDataStore(): DataStore<Preferences> {
    return com.example.mysterybox.di.KoinApp.get().dataStore
}
