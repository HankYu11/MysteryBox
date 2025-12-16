package com.example.mysterybox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysterybox.data.model.CreateBoxRequest
import com.example.mysterybox.ui.theme.*
import com.example.mysterybox.ui.viewmodel.CreateBoxUiState
import com.example.mysterybox.ui.viewmodel.MerchantViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadBoxScreen(
    onBackClick: () -> Unit,
    onUploadSuccess: () -> Unit
) {
    var boxName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var contentReference by remember { mutableStateOf("") }
    var originalPrice by remember { mutableStateOf("500") }
    var discountedPrice by remember { mutableStateOf("199") }
    var quantity by remember { mutableStateOf(5) }
    var saleTime by remember { mutableStateOf("今天, 18:00") }

    val viewModel: MerchantViewModel = koinViewModel()
    val createBoxState by viewModel.createBoxState.collectAsState()

    val isLoading = createBoxState is CreateBoxUiState.Loading
    val errorMessage = (createBoxState as? CreateBoxUiState.Error)?.message

    LaunchedEffect(createBoxState) {
        if (createBoxState is CreateBoxUiState.Success) {
            viewModel.resetCreateBoxState()
            onUploadSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "上架神秘箱子",
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
                actions = {
                    TextButton(onClick = { /* Preview */ }) {
                        Text(
                            text = "預覽",
                            color = Green500,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Image Upload Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 2.dp,
                        color = Green200,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(Green50.copy(alpha = 0.3f))
                    .clickable { /* Handle image picker */ },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Green100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Green500,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "點擊上傳封面照片",
                        color = Gray700,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "建議尺寸 800x600",
                        color = Gray400,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Basic Info Section
            Text(
                text = "基本資訊",
                color = Gray900,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Box Name
            Text(
                text = "箱子名稱",
                color = Gray700,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = boxName,
                onValueChange = { boxName = it },
                placeholder = {
                    Text(
                        text = "例如：週五驚喜麵包包",
                        color = Gray400
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green500,
                    unfocusedBorderColor = Gray200,
                    focusedContainerColor = White,
                    unfocusedContainerColor = Gray50
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Description
            Text(
                text = "商品描述",
                color = Gray700,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = {
                    Text(
                        text = "簡單介紹這個箱子的特色，吸引顧客購買...",
                        color = Gray400
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green500,
                    unfocusedBorderColor = Gray200,
                    focusedContainerColor = White,
                    unfocusedContainerColor = Gray50
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Content Reference
            Text(
                text = "內容物參考",
                color = Gray700,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = contentReference,
                onValueChange = { contentReference = it },
                placeholder = {
                    Text(
                        text = "例如：隨機麵包 3 個、飲料 1 瓶",
                        color = Gray400
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = Gray400
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green500,
                    unfocusedBorderColor = Gray200,
                    focusedContainerColor = White,
                    unfocusedContainerColor = Gray50
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Upload Button
            Button(
                onClick = {
                    viewModel.createBox(
                        CreateBoxRequest(
                            name = boxName,
                            description = description,
                            contentReference = contentReference,
                            originalPrice = originalPrice.toIntOrNull() ?: 0,
                            discountedPrice = discountedPrice.toIntOrNull() ?: 0,
                            quantity = quantity,
                            saleStartTime = saleTime
                        )
                    )
                },
                enabled = boxName.isNotBlank() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green500,
                    disabledContainerColor = Gray300
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "確認上架",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Error Message
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    color = Red500,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Price Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Original Price
                OutlinedTextField(
                    value = originalPrice,
                    onValueChange = { originalPrice = it.filter { c -> c.isDigit() } },
                    label = { Text("原價") },
                    leadingIcon = {
                        Text(
                            text = "$",
                            color = Gray500,
                            fontSize = 16.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Gray200
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                // Discounted Price
                OutlinedTextField(
                    value = discountedPrice,
                    onValueChange = { discountedPrice = it.filter { c -> c.isDigit() } },
                    label = { Text("優惠價") },
                    leadingIcon = {
                        Text(
                            text = "$",
                            color = Green500,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Green200,
                        unfocusedContainerColor = Green50
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Discount hint
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Gray200),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "i",
                        color = Gray500,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "建議折數為原價的 3-5 折，吸引力較高。",
                    color = Gray500,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Sales Settings Section
            Text(
                text = "銷售設定",
                color = Gray900,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quantity
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Gray50),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "預約數量",
                        color = Gray700,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.dp, Gray300, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = Gray600
                            )
                        }

                        Text(
                            text = "$quantity",
                            color = Gray900,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )

                        IconButton(
                            onClick = { quantity++ },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Green500)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sale Start Time
            Text(
                text = "開賣時間",
                color = Gray700,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Show time picker */ },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Gray50),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = null,
                            tint = Gray500
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = saleTime,
                            color = Gray700,
                            fontSize = 15.sp
                        )
                    }

                    Text(
                        text = "v",
                        color = Gray400,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
