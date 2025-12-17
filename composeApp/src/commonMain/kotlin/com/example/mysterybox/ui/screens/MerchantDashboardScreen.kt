package com.example.mysterybox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysterybox.data.model.MerchantOrderStatus
import com.example.mysterybox.data.model.MerchantOrderSummary
import com.example.mysterybox.ui.theme.*
import com.example.mysterybox.data.model.MerchantDashboard
import com.example.mysterybox.ui.viewmodel.DashboardUiState
import com.example.mysterybox.ui.viewmodel.MerchantViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantDashboardScreen(
    onNavigateToUploadBox: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onLogout: () -> Unit,
    viewModel: MerchantViewModel = koinViewModel()
) {
    val dashboardState by viewModel.dashboardState.collectAsState()
    val merchant = viewModel.getCurrentMerchant()

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Green500),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = merchant?.storeName?.firstOrNull()?.toString() ?: "M",
                                color = White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = getGreeting(),
                                color = Gray500,
                                fontSize = 12.sp
                            )
                            Text(
                                text = merchant?.storeName ?: "店家",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Gray900
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* Notification */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "通知",
                            tint = Gray600
                        )
                    }
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "登出",
                            tint = Gray600
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White
                )
            )
        }
    ) { paddingValues ->
        when (val state = dashboardState) {
            is DashboardUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green500)
                }
            }

            is DashboardUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            color = Red500
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadDashboard() },
                            colors = ButtonDefaults.buttonColors(containerColor = Green500)
                        ) {
                            Text("重試")
                        }
                    }
                }
            }

            is DashboardUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Gray50)
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Today's Overview Section
                    item {
                        Text(
                            text = "今日概覽",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Gray900
                        )
                    }

                    // Stats Grid
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                modifier = Modifier.weight(1f),
                                title = "今日營收",
                                value = "NT\$${state.dashboard.todayRevenue}",
                                changePercent = state.dashboard.revenueChangePercent,
                                icon = Icons.Default.AttachMoney,
                                iconBackgroundColor = Green50,
                                iconColor = Green500
                            )
                            StatCard(
                                modifier = Modifier.weight(1f),
                                title = "今日訂單",
                                value = state.dashboard.todayOrders.toString(),
                                icon = Icons.Default.Receipt,
                                iconBackgroundColor = Blue100,
                                iconColor = Blue500
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                modifier = Modifier.weight(1f),
                                title = "上架中神秘箱",
                                value = state.dashboard.activeBoxes.toString(),
                                icon = Icons.Default.Inventory2,
                                iconBackgroundColor = Orange100,
                                iconColor = Orange500
                            )
                            StatCard(
                                modifier = Modifier.weight(1f),
                                title = "店鋪瀏覽",
                                value = state.dashboard.storeViews.toString(),
                                icon = Icons.Default.Visibility,
                                iconBackgroundColor = Purple100,
                                iconColor = Purple500
                            )
                        }
                    }

                    // Publish New Box Button
                    item {
                        Button(
                            onClick = onNavigateToUploadBox,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Green500
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "發布新神秘箱",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Order Management Card
                    item {
                        OrderManagementCard(onClick = onNavigateToOrders)
                    }

                    // Recent Orders Section
                    if (state.dashboard.recentOrders.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "最近訂單",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Gray900
                                )
                                TextButton(onClick = onNavigateToOrders) {
                                    Text(
                                        text = "查看全部",
                                        color = Green500,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        items(state.dashboard.recentOrders) { order ->
                            RecentOrderItem(order = order)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    changePercent: Int? = null,
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (changePercent != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (changePercent >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (changePercent >= 0) Green500 else Red500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${if (changePercent >= 0) "+" else ""}$changePercent%",
                            color = if (changePercent >= 0) Green500 else Red500,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Gray900
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                color = Gray500,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun OrderManagementCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Blue50),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Blue500,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "訂單管理與核銷",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Gray900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "查看訂單、核銷取貨",
                        color = Gray500,
                        fontSize = 13.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Gray400
            )
        }
    }
}

@Composable
private fun RecentOrderItem(order: MerchantOrderSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Customer Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Gray100),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = order.customerInitial,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Gray600
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = order.customerName,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Gray900
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = order.orderId,
                        fontSize = 12.sp,
                        color = Gray400
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = order.itemDescription,
                    fontSize = 13.sp,
                    color = Gray500
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                OrderStatusBadge(status = order.status)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = order.timeAgo,
                    fontSize = 12.sp,
                    color = Gray400
                )
            }
        }
    }
}

@Composable
private fun OrderStatusBadge(status: MerchantOrderStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        MerchantOrderStatus.PENDING_PICKUP -> Triple(Green50, Green600, "待取貨")
        MerchantOrderStatus.PENDING_ACTION -> Triple(Yellow50, Yellow500, "待處理")
        MerchantOrderStatus.COMPLETED -> Triple(Gray100, Gray600, "已完成")
        MerchantOrderStatus.CANCELLED -> Triple(Red50, Red500, "已取消")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun getGreeting(): String {
    // Use a simple greeting - time-based greeting would require platform-specific implementation
    return "您好"
}

// region Preview

@Preview
@Composable
private fun MerchantDashboardContentPreview() {
    val mockDashboard = MerchantDashboard(
        todayRevenue = 12580,
        revenueChangePercent = 12,
        todayOrders = 8,
        activeBoxes = 3,
        storeViews = 156,
        recentOrders = listOf(
            MerchantOrderSummary(
                id = "1",
                orderId = "#8823",
                customerName = "王小明",
                customerInitial = "王",
                itemDescription = "綜合麵包驚喜箱 x 1",
                status = MerchantOrderStatus.PENDING_PICKUP,
                timeAgo = "10 分鐘前"
            ),
            MerchantOrderSummary(
                id = "2",
                orderId = "#8822",
                customerName = "李美麗",
                customerInitial = "李",
                itemDescription = "甜點驚喜箱 x 2",
                status = MerchantOrderStatus.COMPLETED,
                timeAgo = "30 分鐘前"
            ),
            MerchantOrderSummary(
                id = "3",
                orderId = "#8821",
                customerName = "陳大華",
                customerInitial = "陳",
                itemDescription = "日式便當箱 x 1",
                status = MerchantOrderStatus.CANCELLED,
                timeAgo = "1 小時前"
            )
        )
    )

    MerchantDashboardContent(
        storeName = "好味道烘焙坊",
        dashboard = mockDashboard,
        onNavigateToUploadBox = {},
        onNavigateToOrders = {},
        onLogout = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MerchantDashboardContent(
    storeName: String,
    dashboard: MerchantDashboard,
    onNavigateToUploadBox: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Green500),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = storeName.firstOrNull()?.toString() ?: "M",
                                color = White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = getGreeting(),
                                color = Gray500,
                                fontSize = 12.sp
                            )
                            Text(
                                text = storeName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Gray900
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "通知",
                            tint = Gray600
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "登出",
                            tint = Gray600
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Gray50)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Text(
                    text = "今日概覽",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Gray900
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "今日營收",
                        value = "NT\$${dashboard.todayRevenue}",
                        changePercent = dashboard.revenueChangePercent,
                        icon = Icons.Default.AttachMoney,
                        iconBackgroundColor = Green50,
                        iconColor = Green500
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "今日訂單",
                        value = dashboard.todayOrders.toString(),
                        icon = Icons.Default.Receipt,
                        iconBackgroundColor = Blue100,
                        iconColor = Blue500
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "上架中神秘箱",
                        value = dashboard.activeBoxes.toString(),
                        icon = Icons.Default.Inventory2,
                        iconBackgroundColor = Orange100,
                        iconColor = Orange500
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "店鋪瀏覽",
                        value = dashboard.storeViews.toString(),
                        icon = Icons.Default.Visibility,
                        iconBackgroundColor = Purple100,
                        iconColor = Purple500
                    )
                }
            }

            item {
                Button(
                    onClick = onNavigateToUploadBox,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green500),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "發布新神秘箱",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            item {
                OrderManagementCard(onClick = onNavigateToOrders)
            }

            if (dashboard.recentOrders.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "最近訂單",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Gray900
                        )
                        TextButton(onClick = onNavigateToOrders) {
                            Text(
                                text = "查看全部",
                                color = Green500,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                items(dashboard.recentOrders) { order ->
                    RecentOrderItem(order = order)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// endregion
