package com.example.mysterybox

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.crossfade
import com.example.mysterybox.ui.state.AuthState
import com.example.mysterybox.di.appModules
import com.example.mysterybox.di.initializeKoin
import com.example.mysterybox.di.isKoinStarted
import com.example.mysterybox.ui.navigation.*
import com.example.mysterybox.ui.screens.*
import com.example.mysterybox.ui.theme.MysteryBoxTheme
import com.example.mysterybox.ui.utils.safeDrawingPadding
import com.example.mysterybox.ui.viewmodel.AuthViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    // Configure Coil's singleton ImageLoader
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .crossfade(true)
            .build()
    }
    
    // Initialize Koin based on platform
    if (isKoinStarted()) {
        // Koin already started (Android with Application class)
        AppContent()
    } else {
        // Start Koin for other platforms (iOS)
        KoinApplication(application = {
            modules(appModules)
        }) {
            AppContent()
        }
    }
}

@Composable
private fun AppContent() {
    MysteryBoxTheme {
        val navController = rememberNavController()
        val authViewModel: AuthViewModel = koinViewModel()
        val authState by authViewModel.authState.collectAsState()

        // Navigate on successful auth
        LaunchedEffect(authState) {
            if (authState is AuthState.Authenticated) {
                navController.navigate(Home) {
                    popUpTo(Welcome) { inclusive = true }
                }
            }
        }

        // Track current destination for bottom navigation
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        
        // Determine if current screen should show bottom navigation
        val showBottomBar = when {
            currentRoute?.contains("Home") == true -> true
            currentRoute?.contains("MyReservations") == true -> true  
            currentRoute?.contains("Profile") == true -> true
            else -> false
        }

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    BottomNavigationBar(
                        selectedIndex = when {
                            currentRoute?.contains("Home") == true -> 0
                            currentRoute?.contains("MyReservations") == true -> 1
                            currentRoute?.contains("Profile") == true -> 2
                            else -> 0
                        },
                        onBoxClick = {
                            if (currentRoute?.contains("Home") != true) {
                                navController.navigate(Home) {
                                    popUpTo(Home) { inclusive = true }
                                }
                            }
                        },
                        onOrdersClick = {
                            if (currentRoute?.contains("MyReservations") != true) {
                                navController.navigate(MyReservations)
                            }
                        },
                        onProfileClick = {
                            if (currentRoute?.contains("Profile") != true) {
                                navController.navigate(Profile)
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Welcome,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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
                ReservationsScreen()
            }

            composable<Profile> {
                ProfileScreen(
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
}
