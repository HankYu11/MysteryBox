package com.example.mysterybox.di

import com.example.mysterybox.data.auth.AuthManager
import com.example.mysterybox.data.network.MysteryBoxApiService
import com.example.mysterybox.data.network.createHttpClient
import com.example.mysterybox.data.repository.AuthRepository
import com.example.mysterybox.data.repository.AuthRepositoryImpl
import com.example.mysterybox.data.repository.BoxRepository
import com.example.mysterybox.data.repository.BoxRepositoryImpl
import com.example.mysterybox.data.repository.MerchantRepository
import com.example.mysterybox.data.repository.MerchantRepositoryImpl
import com.example.mysterybox.data.repository.ReservationRepository
import com.example.mysterybox.data.repository.ReservationRepositoryImpl
import com.example.mysterybox.ui.viewmodel.BoxViewModel
import com.example.mysterybox.ui.viewmodel.LoginViewModel
import com.example.mysterybox.ui.viewmodel.MerchantViewModel
import com.example.mysterybox.ui.viewmodel.ProfileViewModel
import com.example.mysterybox.ui.viewmodel.ReservationViewModel
import com.example.mysterybox.ui.viewmodel.WelcomeViewModel
import org.koin.dsl.module

val networkModule = module {
    single { createHttpClient(get(), get()) }
    single { AuthManager(get()) }
    single { MysteryBoxApiService(get()) }
}

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<BoxRepository> { BoxRepositoryImpl(get()) }
    single<MerchantRepository> { MerchantRepositoryImpl(get(), get()) }
    single<ReservationRepository> { ReservationRepositoryImpl(get()) }
}

val viewModelModule = module {
    viewModel { LoginViewModel(get(), get()) }
    viewModel { WelcomeViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { BoxViewModel(get(), get(), get()) }
    viewModel { MerchantViewModel(get(), get()) }
    viewModel { ReservationViewModel(get(), get()) }
}

val appModules = listOf(
    platformModule,
    networkModule,
    repositoryModule,
    viewModelModule
)
