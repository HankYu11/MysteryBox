package com.example.mysterybox.di

import org.koin.core.context.GlobalContext

actual fun initializeKoin() {
    // Koin is already initialized in MysteryBoxApplication
    // Do nothing
}

actual fun isKoinStarted(): Boolean {
    return GlobalContext.getOrNull() != null
}