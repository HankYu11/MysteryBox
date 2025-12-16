package com.example.mysterybox.di

import com.example.mysterybox.data.network.createHttpClient
import com.example.mysterybox.data.repository.AuthRepository
import com.example.mysterybox.data.repository.AuthRepositoryImpl
import com.example.mysterybox.data.repository.BoxRepository
import com.example.mysterybox.data.repository.BoxRepositoryImpl
import com.example.mysterybox.data.repository.MerchantRepository
import com.example.mysterybox.data.repository.MerchantRepositoryImpl
import com.example.mysterybox.data.repository.ReservationRepository
import com.example.mysterybox.data.repository.ReservationRepositoryImpl
import com.example.mysterybox.ui.viewmodel.AuthViewModel
import com.example.mysterybox.ui.viewmodel.BoxViewModel
import com.example.mysterybox.ui.viewmodel.MerchantViewModel
import com.example.mysterybox.ui.viewmodel.ReservationViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {
    single { createHttpClient() }
}

val repositoryModule = module {
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    singleOf(::BoxRepositoryImpl) bind BoxRepository::class
    singleOf(::MerchantRepositoryImpl) bind MerchantRepository::class
    singleOf(::ReservationRepositoryImpl) bind ReservationRepository::class
}

val viewModelModule = module {
    viewModelOf(::AuthViewModel)
    viewModelOf(::BoxViewModel)
    viewModelOf(::MerchantViewModel)
    viewModelOf(::ReservationViewModel)
}

val appModules = listOf(
    networkModule,
    repositoryModule,
    viewModelModule
)
