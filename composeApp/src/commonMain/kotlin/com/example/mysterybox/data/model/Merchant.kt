package com.example.mysterybox.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Merchant(
    val id: String,
    val email: String,
    val storeName: String,
    val storeAddress: String,
    val isVerified: Boolean = false
)

@Serializable
data class MerchantLoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class CreateBoxRequest(
    val name: String,
    val description: String,
    val contentReference: String,
    val originalPrice: Int,
    val discountedPrice: Int,
    val quantity: Int,
    val saleStartTime: String,
    val imageUrl: String? = null
)

data class MerchantDashboard(
    val todayRevenue: Int,
    val revenueChangePercent: Int,
    val todayOrders: Int,
    val activeBoxes: Int,
    val storeViews: Int,
    val recentOrders: List<MerchantOrderSummary>
)

data class MerchantOrderSummary(
    val id: String,
    val orderId: String,
    val customerName: String,
    val customerInitial: String,
    val itemDescription: String,
    val status: MerchantOrderStatus,
    val timeAgo: String
)

data class MerchantOrder(
    val id: String,
    val orderId: String,
    val orderTime: String,
    val status: MerchantOrderStatus,
    val isOverdue: Boolean,
    val overdueTime: String?,
    val box: MerchantOrderBox,
    val customer: MerchantOrderCustomer,
    val totalPrice: Int
)

data class MerchantOrderBox(
    val id: String,
    val name: String,
    val specs: String?,
    val quantity: Int,
    val imageUrl: String?
)

data class MerchantOrderCustomer(
    val name: String,
    val phone: String
)

enum class MerchantOrderStatus {
    PENDING_PICKUP,
    PENDING_ACTION,
    COMPLETED,
    CANCELLED;

    companion object {
        fun fromString(value: String): MerchantOrderStatus {
            return entries.find { it.name == value } ?: PENDING_PICKUP
        }
    }
}
