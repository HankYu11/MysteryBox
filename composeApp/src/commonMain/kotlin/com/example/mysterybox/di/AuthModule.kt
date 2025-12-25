package com.example.mysterybox.di

import com.example.mysterybox.data.auth.AuthManager
import com.example.mysterybox.data.repository.AuthRepository
import com.example.mysterybox.data.repository.AuthRepositoryImpl
import com.example.mysterybox.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    // Application-scoped CoroutineScope for long-lived operations
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    // TokenStorage is provided by platformModule (Android: AndroidTokenStorage, iOS: DatastoreTokenStorage)
    single { AuthManager(get(), get(), get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    viewModel { LoginViewModel(get()) }
}
