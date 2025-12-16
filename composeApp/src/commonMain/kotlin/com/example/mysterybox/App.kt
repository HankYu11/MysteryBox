package com.example.mysterybox

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.mysterybox.ui.navigation.*
import com.example.mysterybox.ui.screens.*
import com.example.mysterybox.ui.theme.MysteryBoxTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MysteryBoxTheme {
        val navController = rememberNavController()

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
                    onLoginClick = {
                        navController.navigate(Home) {
                            popUpTo(Welcome) { inclusive = true }
                        }
                    },
                    onSkipClick = {
                        navController.navigate(Home) {
                            popUpTo(Welcome) { inclusive = true }
                        }
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
        }
    }
}
