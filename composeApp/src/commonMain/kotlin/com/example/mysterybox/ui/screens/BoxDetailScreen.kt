package com.example.mysterybox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysterybox.data.model.BoxStatus
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.ui.theme.*
import com.example.mysterybox.ui.utils.safeDrawingPadding
import com.example.mysterybox.ui.viewmodel.BoxViewModel
import com.example.mysterybox.ui.viewmodel.ReservationState
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxDetailScreen(
    boxId: String,
    onBackClick: () -> Unit,
    onNavigateToReservations: () -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: BoxViewModel = koinViewModel()
) {
    val selectedBox by viewModel.selectedBox.collectAsState()
    val reservationState by viewModel.reservationState.collectAsState()

    LaunchedEffect(boxId) {
        viewModel.loadBoxDetail(boxId)
    }

    LaunchedEffect(reservationState) {
        when (reservationState) {
            is ReservationState.Success -> {
                viewModel.resetReservationState()
                onNavigateToReservations()
            }
            is ReservationState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (reservationState as ReservationState.Error).message
                )
                viewModel.resetReservationState()
            }
            else -> {}
        }
    }

    val box = selectedBox ?: return

    BoxDetailContent(
        box = box,
        onBackClick = onBackClick,
        onReserveClick = { viewModel.createReservation(box) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoxDetailContent(
    box: MysteryBox,
    onBackClick: () -> Unit,
    onReserveClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            TopAppBar(
                title = { Text("Mystery Box Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White
                )
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(White)
                    .verticalScroll(rememberScrollState())
            ) {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = when (box.imageUrl) {
                                "bakery", "bakery_night" -> listOf(Color(0xFFD4A574), Color(0xFFE8C9A0))
                                "snacks" -> listOf(Color(0xFF8B9DC3), Color(0xFFB8C5D6))
                                "vegetables" -> listOf(Color(0xFF7CB342), Color(0xFF9CCC65))
                                "drinks" -> listOf(Color(0xFFAB47BC), Color(0xFFCE93D8))
                                else -> listOf(Color(0xFFD4A574), Color(0xFFE8C9A0))
                            }
                        )
                    )
            ) {
                // Countdown timer badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(White)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Green500)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ends in 2h 15m",
                            color = Gray700,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Content placeholder
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = box.name.take(2),
                        color = White.copy(alpha = 0.3f),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Status badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Green50)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "AVAILABLE",
                            color = Green600,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Orange50)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${box.discountPercent}% OFF",
                            color = Orange500,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = box.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Price
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${box.discountedPrice}",
                        color = Green500,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "$${box.originalPrice}",
                        color = Gray400,
                        fontSize = 18.sp,
                        textDecoration = TextDecoration.LineThrough
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Save \$${box.originalPrice - box.discountedPrice}",
                        color = Green600,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // What's Inside Section
                Text(
                    text = "WHAT'S INSIDE",
                    color = Gray500,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = box.description,
                    color = Gray700,
                    fontSize = 15.sp,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mystery Contents badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Gray100)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Green500),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "?",
                                color = White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Mystery Contents",
                            color = Gray700,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(color = Gray200)

                Spacer(modifier = Modifier.height(24.dp))

                // Pickup Time
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = null,
                        tint = Green500,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${box.pickupTimeStart} - ${box.pickupTimeEnd} Today",
                            color = Gray900,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Pick up window",
                            color = Gray500,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Store Location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Green500,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = box.storeName,
                            color = Gray900,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = box.storeAddress,
                            color = Gray500,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Gray100)
                            .clickable { }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "View Map",
                            color = Gray700,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
            // Closes scrollable content Column
            }

            // Bottom Bar
            Surface(
                shadowElevation = 8.dp,
                color = White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$",
                            color = Green500,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${box.discountedPrice}",
                            color = Green500,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onReserveClick,
                        enabled = box.status != BoxStatus.SOLD_OUT,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green500,
                            disabledContainerColor = Gray300
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(
                            text = "Reserve Now",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "->", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun BoxDetailScreenPreview() {
    val mockBox = MysteryBox(
        id = "1",
        name = "Fresh Bakery Surprise Box",
        description = "A delightful mix of freshly baked goods including artisan bread, pastries, and sweet treats. Perfect for breakfast or snacks throughout the week.",
        originalPrice = 2500,
        discountedPrice = 850,
        imageUrl = "bakery",
        storeName = "Artisan Bakery & Cafe",
        storeAddress = "123 Main Street, Downtown",
        pickupTimeStart = "19:00",
        pickupTimeEnd = "21:00",
        status = BoxStatus.AVAILABLE,
        remainingCount = 3,
        discountPercent = 66
    )

    MysteryBoxTheme {
        BoxDetailContent(
            box = mockBox,
            onBackClick = {},
            onReserveClick = {}
        )
    }
}
