package com.example.mysterybox

import android.app.Application
import com.example.mysterybox.di.KoinApp
import com.example.mysterybox.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MysteryBoxApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize KoinApp helper for accessing context
        KoinApp.init(this)

        startKoin {
            androidContext(this@MysteryBoxApplication)
            modules(appModules)
        }
    }
}