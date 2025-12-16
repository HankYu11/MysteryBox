package com.example.mysterybox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysterybox.data.SampleData
import com.example.mysterybox.data.model.Reservation
import com.example.mysterybox.data.model.ReservationStatus
import com.example.mysterybox.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationsScreen(
    onBackClick: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("進行中", "歷史紀錄")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "我的預約",
                        fontWeight = FontWeight.Bold
                    )
                },
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
        },
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = 1,
                onBoxClick = onNavigateToHome,
                onOrdersClick = { },
                onProfileClick = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Gray50)
                .padding(paddingValues)
        ) {
            // Tab Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (selectedTab == index) Gray900 else Gray100)
                            .clickable { selectedTab = index }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (selectedTab == index) White else Gray600,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Reservations List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val filteredReservations = if (selectedTab == 0) {
                    SampleData.reservations.filter {
                        it.status != ReservationStatus.COMPLETED &&
                        it.status != ReservationStatus.CANCELLED
                    }
                } else {
                    SampleData.reservations.filter {
                        it.status == ReservationStatus.COMPLETED ||
                        it.status == ReservationStatus.CANCELLED
                    }
                }

                items(filteredReservations) { reservation ->
                    ReservationCard(reservation = reservation)
                }

                if (filteredReservations.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (selectedTab == 0) "目前沒有進行中的預約" else "目前沒有歷史紀錄",
                                color = Gray500,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationCard(
    reservation: Reservation
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Status Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        when (reservation.status) {
                            ReservationStatus.READY_FOR_PICKUP -> Green50
                            ReservationStatus.RESERVED -> Gray100
                            ReservationStatus.PICKUP_MISSED -> Red50
                            else -> Gray100
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (reservation.status) {
                            ReservationStatus.READY_FOR_PICKUP -> Icons.Default.Home
                            ReservationStatus.PICKUP_MISSED -> Icons.Default.Warning
                            else -> Icons.Outlined.DateRange
                        },
                        contentDescription = null,
                        tint = when (reservation.status) {
                            ReservationStatus.READY_FOR_PICKUP -> Green600
                            ReservationStatus.PICKUP_MISSED -> Red500
                            else -> Gray600
                        },
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (reservation.status) {
                            ReservationStatus.READY_FOR_PICKUP -> "待取貨"
                            ReservationStatus.RESERVED -> "已預約"
                            ReservationStatus.PICKUP_MISSED -> "取貨逾時"
                            ReservationStatus.COMPLETED -> "已完成"
                            ReservationStatus.CANCELLED -> "已取消"
                        },
                        color = when (reservation.status) {
                            ReservationStatus.READY_FOR_PICKUP -> Green600
                            ReservationStatus.PICKUP_MISSED -> Red500
                            else -> Gray600
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "訂單 ${reservation.orderId}",
                    color = Gray500,
                    fontSize = 13.sp
                )
            }

            // Content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Image placeholder
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFD4A574), Color(0xFFE8C9A0))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = reservation.box.name.take(1),
                        color = White.copy(alpha = 0.5f),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = reservation.box.name,
                        color = Gray900,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Store location
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Gray400,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = reservation.box.storeAddress,
                            color = Gray500,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Pickup time
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = null,
                            tint = Gray400,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${reservation.pickupDate} ${reservation.pickupTimeStart} - ${reservation.pickupTimeEnd}",
                            color = Gray500,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Price
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Gray100)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "現場付款",
                                color = Gray600,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NT\$${reservation.price}",
                            color = Gray900,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Action Button based on status
            when (reservation.status) {
                ReservationStatus.READY_FOR_PICKUP -> {
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green500),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "出示取貨碼",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                ReservationStatus.RESERVED -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Gray600
                            ),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                brush = Brush.horizontalGradient(listOf(Gray300, Gray300))
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "取消",
                                fontSize = 14.sp
                            )
                        }
                        Button(
                            onClick = { },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Gray200
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "未開放取貨",
                                color = Gray600,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                ReservationStatus.PICKUP_MISSED -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Red50)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "請聯繫客服處理，如果不取貨將影響您的信用評分。",
                                color = Red500,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Gray600
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "聯繫客服",
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                else -> { }
            }
        }
    }
}
