package com.example.mysterybox.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MerchantDashboardDto(
    val todayRevenue: Int,
    @SerialName("revenue_change_percent")
    val revenueChangePercent: Int,
    val todayOrders: Int,
    val activeBoxes: Int,
    val storeViews: Int,
    val recentOrders: List<MerchantOrderSummaryDto>
)

@Serializable
data class MerchantOrderSummaryDto(
    val id: String,
    val orderId: String,
    val customerName: String,
    val customerInitial: String,
    val itemDescription: String,
    val status: String,
    val timeAgo: String
)

@Serializable
data class MerchantOrderDto(
    val id: String,
    val orderId: String,
    val orderTime: String,
    val status: String,
    val isOverdue: Boolean = false,
    val overdueTime: String? = null,
    val box: MerchantOrderBoxDto,
    val customer: MerchantOrderCustomerDto,
    val totalPrice: Int
)

@Serializable
data class MerchantOrderBoxDto(
    val id: String,
    val name: String,
    val specs: String? = null,
    val quantity: Int,
    val imageUrl: String? = null
)

@Serializable
data class MerchantOrderCustomerDto(
    val name: String,
    val phone: String
)

@Serializable
data class VerifyOrderResponseDto(
    val success: Boolean,
    val message: String? = null
)
