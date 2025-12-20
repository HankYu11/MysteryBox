package com.example.mysterybox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysterybox.data.model.BoxStatus
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.ui.theme.*
import com.example.mysterybox.ui.utils.navigationBarsPadding
import com.example.mysterybox.ui.utils.safeDrawingPadding
import com.example.mysterybox.ui.utils.statusBarsPadding
import com.example.mysterybox.ui.viewmodel.BoxFilter
import com.example.mysterybox.ui.viewmodel.BoxViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onBoxClick: (String) -> Unit,
    viewModel: BoxViewModel = koinViewModel()
) {
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val filteredBoxes by viewModel.filteredBoxes.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        viewModel.loadBoxes()
    }

    val selectedTab = when (selectedFilter) {
        BoxFilter.ALL -> 0
        BoxFilter.AVAILABLE -> 1
        BoxFilter.ALMOST_SOLD_OUT -> 2
        BoxFilter.SOLD_OUT -> 3
    }
    val tabs = listOf("全部", "可預約", "即將售罄", "已售完")

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.loadBoxes() },
        state = pullToRefreshState,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header
            Text(
                text = "今日驚喜箱",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Gray900,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            )

            // Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    FilterChip(
                        selected = selectedTab == index,
                        onClick = {
                            val filter = when (index) {
                                0 -> BoxFilter.ALL
                                1 -> BoxFilter.AVAILABLE
                                2 -> BoxFilter.ALMOST_SOLD_OUT
                                3 -> BoxFilter.SOLD_OUT
                                else -> BoxFilter.ALL
                            }
                            viewModel.setFilter(filter)
                        },
                        label = { Text(tab, fontSize = 14.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Green500,
                            selectedLabelColor = White,
                            containerColor = Gray100,
                            labelColor = Gray600
                        ),
                        border = null,
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            // Notice Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Green50)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "!",
                        color = Green600,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Green100)
                            .wrapContentSize(Alignment.Center)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "搶救食物，減少浪費！所有商品皆為即期商品，請確認後再預約。",
                        color = Green700,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // Box List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredBoxes) { box ->
                    MysteryBoxCard(
                        box = box,
                        onClick = { onBoxClick(box.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun MysteryBoxCard(
    box: MysteryBox,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = when (box.imageUrl) {
                                "bakery" -> listOf(Color(0xFFD4A574), Color(0xFFE8C9A0))
                                "snacks" -> listOf(Color(0xFF8B9DC3), Color(0xFFB8C5D6))
                                "vegetables" -> listOf(Color(0xFF7CB342), Color(0xFF9CCC65))
                                "drinks" -> listOf(Color(0xFFAB47BC), Color(0xFFCE93D8))
                                else -> listOf(Color(0xFFD4A574), Color(0xFFE8C9A0))
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Pickup time badge
                if (box.status == BoxStatus.AVAILABLE || box.status == BoxStatus.ALMOST_SOLD_OUT) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(White)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.DateRange,
                                contentDescription = null,
                                tint = Green600,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${box.pickupTimeStart} 開賣",
                                color = Gray700,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Remaining count badge
                if (box.remainingCount > 0 && box.status != BoxStatus.SOLD_OUT) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Green500)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "剩餘 ${box.remainingCount} 個",
                            color = White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Sold out overlay
                if (box.status == BoxStatus.SOLD_OUT) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Gray700)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "已售完",
                                color = White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Content placeholder text
                Text(
                    text = box.name.take(2),
                    color = White.copy(alpha = 0.3f),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Content
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = box.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gray900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = box.description,
                    fontSize = 13.sp,
                    color = Gray500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$${box.discountedPrice}",
                            color = Green500,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$${box.originalPrice}",
                            color = Gray400,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }

                    Button(
                        onClick = { },
                        enabled = box.status != BoxStatus.SOLD_OUT,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green500,
                            disabledContainerColor = Gray300
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = when (box.status) {
                                BoxStatus.SOLD_OUT -> "已售完"
                                BoxStatus.ALMOST_SOLD_OUT -> "即將開始"
                                else -> "立即預約"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedIndex: Int,
    onBoxClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = selectedIndex == 0,
            onClick = onBoxClick,
            icon = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (selectedIndex == 0) Green500 else Gray400),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "?",
                        color = White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            label = { Text("驚喜箱", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Green500,
                selectedTextColor = Green500,
                indicatorColor = Green50
            )
        )
        NavigationBarItem(
            selected = selectedIndex == 1,
            onClick = onOrdersClick,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DateRange,
                    contentDescription = "訂單"
                )
            },
            label = { Text("訂單", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Green500,
                selectedTextColor = Green500,
                indicatorColor = Green50
            )
        )
        NavigationBarItem(
            selected = selectedIndex == 2,
            onClick = onProfileClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "會員"
                )
            },
            label = { Text("會員", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Green500,
                selectedTextColor = Green500,
                indicatorColor = Green50
            )
        )
    }
}
