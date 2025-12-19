package com.example.mysterybox.di

import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

actual fun initializeKoin() {
    if (!isKoinStarted()) {
        startKoin {
            modules(appModules)
        }
    }
}

actual fun isKoinStarted(): Boolean {
    return GlobalContext.getOrNull() != null
}