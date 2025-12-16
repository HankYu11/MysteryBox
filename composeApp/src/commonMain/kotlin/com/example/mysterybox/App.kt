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
import com.example.mysterybox.data.model.AuthState
import com.example.mysterybox.data.model.OAuthCallbackResult
import com.example.mysterybox.di.appModules
import com.example.mysterybox.ui.navigation.*
import com.example.mysterybox.ui.screens.*
import com.example.mysterybox.ui.theme.MysteryBoxTheme
import com.example.mysterybox.ui.viewmodel.AuthViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App(
    oauthCallback: OAuthCallbackResult? = null,
    onOAuthCallbackHandled: () -> Unit = {}
) {
    KoinApplication(application = {
        modules(appModules)
    }) {
        MysteryBoxTheme {
            val navController = rememberNavController()
            val authViewModel: AuthViewModel = koinViewModel()
            val authState by authViewModel.authState.collectAsState()

        // Handle OAuth callback
        LaunchedEffect(oauthCallback) {
            oauthCallback?.let { callback ->
                authViewModel.handleOAuthCallback(callback)
                onOAuthCallbackHandled()
            }
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
                        // Profile not implemented yet
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
                    onReserveClick = { box ->
                        // Handle reservation
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
                        // Profile not implemented yet
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
                        navController.navigate(UploadBox) {
                            popUpTo(MerchantLogin) { inclusive = true }
                        }
                    },
                    onApplyClick = {
                        // Apply for partnership - not implemented
                    }
                )
            }

            composable<UploadBox> {
                UploadBoxScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onUploadSuccess = {
                        // Go back or show success message
                        navController.popBackStack()
                    }
                )
            }
        }
        }
    }
}
