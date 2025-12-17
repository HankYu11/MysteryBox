package com.example.mysterybox.di

import com.example.mysterybox.data.network.MysteryBoxApiService
import com.example.mysterybox.data.network.TokenManager
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
    single { TokenManager() }
    single { MysteryBoxApiService(get(), get()) }
}

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<BoxRepository> { BoxRepositoryImpl(get()) }
    single<MerchantRepository> { MerchantRepositoryImpl(get(), get()) }
    single<ReservationRepository> { ReservationRepositoryImpl(get()) }
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
