package com.example.mysterybox.di

import com.example.mysterybox.data.storage.AndroidTokenStorage
import com.example.mysterybox.data.storage.TokenStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single<TokenStorage> { AndroidTokenStorage(androidContext()) }
}