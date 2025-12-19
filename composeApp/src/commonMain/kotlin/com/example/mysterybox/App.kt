package com.example.mysterybox

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.crossfade
import com.example.mysterybox.auth.rememberLineSdkLauncher
import com.example.mysterybox.data.model.AuthState
import com.example.mysterybox.data.repository.AuthRepository
import com.example.mysterybox.di.appModules
import com.example.mysterybox.ui.navigation.*
import com.example.mysterybox.ui.screens.*
import com.example.mysterybox.ui.theme.MysteryBoxTheme
import com.example.mysterybox.ui.viewmodel.AuthViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    // Configure Coil's singleton ImageLoader
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .crossfade(true)
            .build()
    }
    
    KoinApplication(application = {
        modules(appModules)
    }) {
        AppContent()
    }
}

@Composable
private fun AppContent() {
    MysteryBoxTheme {
        val navController = rememberNavController()
        val authViewModel: AuthViewModel = koinViewModel()
        val authState by authViewModel.authState.collectAsState()
        
        // Setup LINE SDK launcher
        val lineSdkLauncher = rememberLineSdkLauncher()
        val authRepository: AuthRepository = koinInject()
        LaunchedEffect(lineSdkLauncher) {
            authRepository.setOAuthLauncher(lineSdkLauncher)
        }

        // Navigate on successful auth
        LaunchedEffect(authState) {
            if (authState is AuthState.Authenticated) {
                navController.navigate(Home) {
                    popUpTo(Welcome) { inclusive = true }
                }
            }
        }

        NavHost(
            navController = navController,
            startDestination = Welcome,
            modifier = Modifier.fillMaxSize()
        ) {
            composable<Welcome> {
                WelcomeScreen(
                    onStartClick = {
                        navController.navigate(Login)
                    }
                )
            }

            composable<Login> {
                LoginScreen(
                    authState = authState,
                    onLoginClick = {
                        authViewModel.startLineLogin()
                    },
                    onSkipClick = {
                        navController.navigate(Home) {
                            popUpTo(Welcome) { inclusive = true }
                        }
                    },
                    onMerchantLoginClick = {
                        navController.navigate(MerchantLogin)
                    }
                )
            }

            composable<Home> {
                HomeScreen(
                    onBoxClick = { boxId ->
                        navController.navigate(BoxDetail(boxId))
                    },
                    onNavigateToReservations = {
                        navController.navigate(MyReservations)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Profile)
                    }
                )
            }

            composable<BoxDetail> { backStackEntry ->
                val boxDetail: BoxDetail = backStackEntry.toRoute()
                BoxDetailScreen(
                    boxId = boxDetail.boxId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNavigateToReservations = {
                        navController.navigate(MyReservations)
                    }
                )
            }

            composable<MyReservations> {
                ReservationsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNavigateToHome = {
                        navController.navigate(Home) {
                            popUpTo(Home) { inclusive = true }
                        }
                    },
                    onNavigateToProfile = {
                        navController.navigate(Profile)
                    }
                )
            }

            composable<Profile> {
                ProfileScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNavigateToLogin = {
                        navController.navigate(Login) {
                            popUpTo(Welcome) { inclusive = true }
                        }
                    }
                )
            }

            // Merchant Screens
            composable<MerchantLogin> {
                MerchantLoginScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onLoginSuccess = {
                        navController.navigate(MerchantDashboard) {
                            popUpTo(MerchantLogin) { inclusive = true }
                        }
                    },
                    onApplyClick = {
                        // Apply for partnership - not implemented
                    }
                )
            }

            composable<MerchantDashboard> {
                MerchantDashboardScreen(
                    onNavigateToUploadBox = {
                        navController.navigate(UploadBox)
                    },
                    onNavigateToOrders = {
                        navController.navigate(MerchantOrders)
                    },
                    onLogout = {
                        navController.navigate(Login) {
                            popUpTo(MerchantDashboard) { inclusive = true }
                        }
                    }
                )
            }

            composable<MerchantOrders> {
                MerchantOrdersScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable<UploadBox> {
                UploadBoxScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onUploadSuccess = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
