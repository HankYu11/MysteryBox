package com.example.mysterybox.di

import com.example.mysterybox.data.storage.IosTokenStorage
import com.example.mysterybox.data.storage.TokenStorage
import org.koin.dsl.module

actual val platformModule = module {
    single<TokenStorage> { IosTokenStorage() }
}