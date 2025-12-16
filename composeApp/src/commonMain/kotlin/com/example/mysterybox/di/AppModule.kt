package com.example.mysterybox.di

import com.example.mysterybox.data.network.createHttpClient
import com.example.mysterybox.data.repository.AuthRepository
import com.example.mysterybox.data.repository.AuthRepositoryImpl
import com.example.mysterybox.ui.viewmodel.AuthViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {
    single { createHttpClient() }
}

val repositoryModule = module {
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
}

val viewModelModule = module {
    viewModelOf(::AuthViewModel)
}

val appModules = listOf(
    networkModule,
    repositoryModule,
    viewModelModule
)
