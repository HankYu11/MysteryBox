package com.example.mysterybox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysterybox.auth.rememberLineSdkLauncher
import com.example.mysterybox.ui.theme.*
import com.example.mysterybox.ui.utils.navigationBarsPadding
import com.example.mysterybox.ui.utils.statusBarsPadding
import com.example.mysterybox.ui.viewmodel.LoginUiState
import com.example.mysterybox.ui.viewmodel.LoginViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSkipClick: () -> Unit,
    onMerchantLoginClick: () -> Unit = {},
    viewModel: LoginViewModel = koinViewModel()
) {
    val loginState by viewModel.loginState.collectAsState()
    val lineSdkLauncher = rememberLineSdkLauncher()

    // Navigate on successful login
    LaunchedEffect(loginState) {
        if (loginState is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    fun handleLoginClick() {
        viewModel.startLineLogin()
        lineSdkLauncher { accessToken, error ->
            viewModel.handleLineLoginResult(accessToken, error)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "登入/註冊",
            color = Gray900,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Logo/Brand Box
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Green700,
                            Green600
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Small logo placeholder
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        color = White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "SURPRISE",
                    color = White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "BOX",
                    color = White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // App Name
        Text(
            text = "Surprise Box",
            color = Gray900,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = "搶救即期美食，減少食物浪費。\n每一天都有不同的驚喜等著你！",
            color = Gray500,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        // Error Message
        when (loginState) {
            is LoginUiState.Error -> {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = (loginState as LoginUiState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> { /* No error to display */ }
        }

        Spacer(modifier = Modifier.weight(1f))

        // LINE Login Button
        Button(
            onClick = { handleLoginClick() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Green500,
                disabledContainerColor = Green500.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = loginState !is LoginUiState.Loading
        ) {
            when (loginState) {
                is LoginUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "登入中...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                else -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LINE icon placeholder
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "L",
                                color = Green500,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "透過 LINE 帳號登入",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Skip Button
        Text(
            text = "先逛逛再說",
            color = Gray500,
            fontSize = 14.sp,
            modifier = Modifier.clickable { onSkipClick() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Merchant Login Link
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "我是店家？",
                color = Gray400,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "店家登入",
                color = Green600,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onMerchantLoginClick() }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Footer
        Text(
            text = "登入即代表同意我們的服務條款、隱私權政策 與 社群條款。",
            color = Gray400,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
