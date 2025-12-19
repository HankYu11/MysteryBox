package com.example.mysterybox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysterybox.data.model.MerchantOrder
import com.example.mysterybox.data.model.MerchantOrderStatus
import com.example.mysterybox.ui.theme.*
import com.example.mysterybox.ui.utils.safeDrawingPadding
import com.example.mysterybox.ui.viewmodel.MerchantViewModel
import com.example.mysterybox.ui.viewmodel.OrderActionState
import com.example.mysterybox.ui.viewmodel.OrderTab
import com.example.mysterybox.ui.viewmodel.OrdersUiState
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantOrdersScreen(
    onBackClick: () -> Unit,
    viewModel: MerchantViewModel = koinViewModel()
) {
    val ordersState by viewModel.ordersState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val orderActionState by viewModel.orderActionState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    LaunchedEffect(orderActionState) {
        when (orderActionState) {
            is OrderActionState.Success -> {
                snackbarHostState.showSnackbar("操作成功")
                viewModel.resetOrderActionState()
            }
            is OrderActionState.Error -> {
                snackbarHostState.showSnackbar((orderActionState as OrderActionState.Error).message)
                viewModel.resetOrderActionState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "訂單管理與核銷",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Gray50)
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text(
                        text = "搜尋訂單編號、顧客姓名、手機",
                        color = Gray400,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Gray400
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.setSearchQuery("")
                            viewModel.searchOrders()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "清除",
                                tint = Gray400
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green500,
                    unfocusedBorderColor = Gray200,
                    focusedContainerColor = White,
                    unfocusedContainerColor = White
                ),
                singleLine = true
            )

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = OrderTab.entries.indexOf(selectedTab),
                containerColor = White,
                contentColor = Green500,
                edgePadding = 16.dp,
                divider = {}
            ) {
                OrderTab.entries.forEach { tab ->
                    val title = when (tab) {
                        OrderTab.PENDING -> "待核銷"
                        OrderTab.COMPLETED -> "已完成"
                        OrderTab.CANCELLED -> "已取消"
                        OrderTab.HISTORY -> "歷史紀錄"
                    }

                    Tab(
                        selected = selectedTab == tab,
                        onClick = { viewModel.setSelectedTab(tab) },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        selectedContentColor = Green500,
                        unselectedContentColor = Gray500
                    )
                }
            }

            HorizontalDivider(color = Gray200)

            // Info Banner
            if (selectedTab == OrderTab.PENDING) {
                InfoBanner()
            }

            // Orders List
            when (val state = ordersState) {
                is OrdersUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Green500)
                    }
                }

                is OrdersUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.message,
                                color = Red500
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadOrders() },
                                colors = ButtonDefaults.buttonColors(containerColor = Green500)
                            ) {
                                Text("重試")
                            }
                        }
                    }
                }

                is OrdersUiState.Success -> {
                    if (state.orders.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inbox,
                                    contentDescription = null,
                                    tint = Gray300,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "目前沒有訂單",
                                    color = Gray500,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(state.orders) { order ->
                                OrderCard(
                                    order = order,
                                    isActionLoading = orderActionState is OrderActionState.Loading,
                                    onVerifyClick = { viewModel.verifyOrder(order.id) },
                                    onCancelClick = { viewModel.cancelOrder(order.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Blue50)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Blue500,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "顧客出示取貨 QRCode 後，您可以掃描或手動核銷完成訂單",
                color = Blue500,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun OrderCard(
    order: MerchantOrder,
    isActionLoading: Boolean,
    onVerifyClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Order ID and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = order.orderId,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Gray900
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = order.orderTime,
                        fontSize = 13.sp,
                        color = Gray400
                    )
                }

                OrderStatusChip(status = order.status, isOverdue = order.isOverdue)
            }

            // Overdue Warning
            if (order.isOverdue && order.overdueTime != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Red50)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Red500,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "預約時間已過期: ${order.overdueTime}",
                        color = Red500,
                        fontSize = 12.sp
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Gray100
            )

            // Product Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Product Image Placeholder
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Gray100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = Gray400,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.box.name,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = Gray900,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (order.box.specs != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = order.box.specs,
                            fontSize = 13.sp,
                            color = Gray500
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "x${order.box.quantity}",
                            fontSize = 13.sp,
                            color = Gray500
                        )
                        Text(
                            text = "NT\$${order.totalPrice}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Green500
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Gray100
            )

            // Customer Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Gray100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Gray500,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = order.customer.name,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Gray900
                        )
                        Text(
                            text = order.customer.phone,
                            fontSize = 13.sp,
                            color = Gray500
                        )
                    }
                }

                // Call Button
                IconButton(
                    onClick = { /* Call customer */ },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Green50)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "撥打電話",
                        tint = Green500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Action Buttons
            if (order.status == MerchantOrderStatus.PENDING_PICKUP ||
                order.status == MerchantOrderStatus.PENDING_ACTION
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (order.isOverdue) {
                        // Overdue actions
                        OutlinedButton(
                            onClick = { /* Contact customer */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Gray700
                            )
                        ) {
                            Text(
                                text = "聯絡顧客",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = onCancelClick,
                            enabled = !isActionLoading,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Red500
                            )
                        ) {
                            if (isActionLoading) {
                                CircularProgressIndicator(
                                    color = White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "取消訂單",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        // Normal actions
                        OutlinedButton(
                            onClick = { /* View details */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Gray700
                            )
                        ) {
                            Text(
                                text = "查看詳情",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = onVerifyClick,
                            enabled = !isActionLoading,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Green500
                            )
                        ) {
                            if (isActionLoading) {
                                CircularProgressIndicator(
                                    color = White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "手動核銷",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderStatusChip(status: MerchantOrderStatus, isOverdue: Boolean) {
    val (backgroundColor, textColor, text) = when {
        isOverdue -> Triple(Red50, Red500, "待處理")
        status == MerchantOrderStatus.PENDING_PICKUP -> Triple(Green50, Green600, "待取貨")
        status == MerchantOrderStatus.PENDING_ACTION -> Triple(Yellow50, Yellow500, "待處理")
        status == MerchantOrderStatus.COMPLETED -> Triple(Gray100, Gray600, "已完成")
        status == MerchantOrderStatus.CANCELLED -> Triple(Red50, Red500, "已取消")
        else -> Triple(Gray100, Gray600, "未知")
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
