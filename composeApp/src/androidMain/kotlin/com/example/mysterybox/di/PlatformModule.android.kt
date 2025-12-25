package com.example.mysterybox.di

import com.example.mysterybox.data.storage.DatastoreTokenStorage
import com.example.mysterybox.data.storage.TokenStorage
import com.example.mysterybox.data.storage.createDataStore
import org.koin.dsl.module

actual val platformModule = module {
    single { createDataStore() }
    single<TokenStorage> { DatastoreTokenStorage(get()) }
}