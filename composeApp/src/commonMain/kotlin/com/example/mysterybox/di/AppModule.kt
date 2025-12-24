package com.example.mysterybox.di

import com.example.mysterybox.data.network.MysteryBoxApiService
import com.example.mysterybox.data.network.createHttpClient
import com.example.mysterybox.data.repository.*
import com.example.mysterybox.ui.viewmodel.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {
    single { createHttpClient(get(), get()) }
    single { MysteryBoxApiService(get()) }
    single { Json { ignoreUnknownKeys = true } }
}

val repositoryModule = module {
    single<BoxRepository> { BoxRepositoryImpl(get()) }
    single<MerchantRepository> { MerchantRepositoryImpl(get(), get(), get()) }
    single<ReservationRepository> { ReservationRepositoryImpl(get()) }
}

val viewModelModule = module {
    viewModel { WelcomeViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { BoxViewModel(get(), get(), get()) }
    viewModel { MerchantViewModel(get(), get(), get()) }
    viewModel { ReservationViewModel(get(), get()) }
}

val appModules = listOf(
    platformModule,
    networkModule,
    authModule,
    repositoryModule,
    viewModelModule
)
