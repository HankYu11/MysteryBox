package com.example.mysterybox

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.mysterybox.data.storage.cleanupLegacySharedPreferences
import com.example.mysterybox.di.KoinApp
import com.example.mysterybox.di.appModules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MysteryBoxApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // Initialize KoinApp helper for accessing context
        KoinApp.init(this)

        startKoin {
            androidContext(this@MysteryBoxApplication)
            modules(appModules)
        }

        // Clean up legacy SharedPreferences after migration to DataStore
        applicationScope.launch {
            val dataStore: DataStore<Preferences> by inject()
            cleanupLegacySharedPreferences(this@MysteryBoxApplication, dataStore)
        }
    }
}