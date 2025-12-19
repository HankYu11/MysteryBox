package com.example.mysterybox.di

import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

private var koinStarted = false

actual fun initializeKoin() {
    if (!isKoinStarted()) {
        startKoin {
            modules(appModules)
        }
        koinStarted = true
    }
}

actual fun isKoinStarted(): Boolean {
    return koinStarted
}