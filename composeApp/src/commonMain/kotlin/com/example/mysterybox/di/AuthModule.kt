package com.example.mysterybox.di

import com.example.mysterybox.data.auth.AuthManager
import com.example.mysterybox.data.repository.AuthRepository
import com.example.mysterybox.data.repository.AuthRepositoryImpl
import com.example.mysterybox.data.storage.DatastoreTokenStorage
import com.example.mysterybox.data.storage.TokenStorage
import com.example.mysterybox.data.storage.createDataStore
import com.example.mysterybox.ui.viewmodel.LoginViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single<TokenStorage> { DatastoreTokenStorage(createDataStore()) }
    single { AuthManager(get(), get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    viewModel { LoginViewModel(get()) }
}
