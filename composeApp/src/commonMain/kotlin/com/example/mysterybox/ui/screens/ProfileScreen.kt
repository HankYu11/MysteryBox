package com.example.mysterybox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.mysterybox.ui.state.AuthState
import com.example.mysterybox.ui.theme.*
import com.example.mysterybox.ui.utils.safeDrawingPadding
import com.example.mysterybox.ui.viewmodel.ProfileUiState
import com.example.mysterybox.ui.viewmodel.ProfileViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val profileState by viewModel.profileState.collectAsState()
    var logoutRequested by remember { mutableStateOf(false) }

    // Navigate to login after successful logout
    LaunchedEffect(authState) {
        if (logoutRequested && authState is AuthState.Idle) {
            logoutRequested = false
            onNavigateToLogin()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        Text(
            text = "個人資料",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Gray900,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            when (val state = authState) {
                is AuthState.Authenticated -> {
                    // User Profile Header
                    UserProfileHeader(
                        displayName = state.user.displayName,
                        pictureUrl = state.user.pictureUrl
                    )

                    // User Info Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "帳號資訊",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gray900
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            InfoRow(
                                icon = Icons.Default.Person,
                                label = "顯示名稱",
                                value = state.user.displayName
                            )

                            if (state.user.lineUserId != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                InfoRow(
                                    icon = Icons.Default.AccountCircle,
                                    label = "LINE ID",
                                    value = state.user.lineUserId
                                )
                            }

                            state.user.createdAt?.let { createdAt ->
                                Spacer(modifier = Modifier.height(12.dp))
                                InfoRow(
                                    icon = Icons.Default.DateRange,
                                    label = "加入日期",
                                    value = formatDate(createdAt)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Settings Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "設定",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gray900
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Logout Button
                            OutlinedButton(
                                onClick = {
                                    logoutRequested = true
                                    viewModel.logout()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                shape = RoundedCornerShape(8.dp),
                                enabled = profileState !is ProfileUiState.LoggingOut
                            ) {
                                if (profileState is ProfileUiState.LoggingOut) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = "登出",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("登出")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                is AuthState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                else -> {
                    // Not authenticated
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = Gray400
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "請先登入",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Gray700
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "登入後即可查看個人資料",
                                fontSize = 14.sp,
                                color = Gray500
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = onNavigateToLogin,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Green500
                                )
                            ) {
                                Text("前往登入")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserProfileHeader(
    displayName: String,
    pictureUrl: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Green500,
                        Green600
                    )
                )
            )
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(White),
                contentAlignment = Alignment.Center
            ) {
                if (!pictureUrl.isNullOrBlank()) {
                    // Load actual image from URL using Coil
                    AsyncImage(
                        model = pictureUrl,
                        contentDescription = "頭像",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Show placeholder icon when no URL
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "頭像",
                        modifier = Modifier.size(60.dp),
                        tint = Green500
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display Name
            Text(
                text = displayName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Gray500
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Gray500
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Gray900
            )
        }
    }
}

private fun formatDate(dateString: String): String {
    // Simple date formatting - you can enhance this
    return try {
        dateString.substringBefore('T')
    } catch (e: Exception) {
        dateString
    }
}
