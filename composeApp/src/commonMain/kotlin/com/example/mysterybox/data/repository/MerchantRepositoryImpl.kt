package com.example.mysterybox.data.repository

import com.example.mysterybox.data.dto.CreateBoxRequestDto
import com.example.mysterybox.data.dto.MerchantDashboardDto
import com.example.mysterybox.data.dto.MerchantOrderDto
import com.example.mysterybox.data.dto.MerchantOrderSummaryDto
import com.example.mysterybox.data.dto.toDomain
import com.example.mysterybox.data.model.ApiError
import com.example.mysterybox.data.model.CreateBoxRequest
import com.example.mysterybox.data.model.Merchant
import com.example.mysterybox.data.model.MerchantDashboard
import com.example.mysterybox.data.model.MerchantLoginRequest
import com.example.mysterybox.data.model.MerchantOrder
import com.example.mysterybox.data.model.MerchantOrderBox
import com.example.mysterybox.data.model.MerchantOrderCustomer
import com.example.mysterybox.data.model.MerchantOrderStatus
import com.example.mysterybox.data.model.MerchantOrderSummary
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.network.MysteryBoxApiService
import com.example.mysterybox.data.storage.TokenStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MerchantRepositoryImpl(
    private val apiService: MysteryBoxApiService,
    private val tokenStorage: TokenStorage,
    private val json: Json
) : MerchantRepository {

    override suspend fun login(request: MerchantLoginRequest): Result<Merchant> {
        return when (val result = apiService.merchantLogin(request.email, request.password)) {
            is Result.Success -> {
                val merchantResponse = result.data
                val merchant = merchantResponse.toDomain()
                merchantResponse.token?.let { token ->
                    tokenStorage.saveMerchantToken(token)
                    tokenStorage.saveMerchantData(json.encodeToString(merchant))
                }
                Result.Success(merchant)
            }
            is Result.Error -> result
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            // Call logout API if needed
            tokenStorage.clearMerchantToken()
            Result.Success(Unit)
        } catch (e: Exception) {
            tokenStorage.clearMerchantToken() // Clear token even if API call fails
            Result.Success(Unit)
        }
    }

    override suspend fun getCurrentMerchant(): Result<Merchant> {
        // This would typically fetch from server or decode JWT token
        // For now, return error if no token exists
        val token = tokenStorage.getMerchantToken()
        return if (token != null) {
            // In a real implementation, you'd decode the JWT or call an API
            Result.Error(ApiError.NotImplemented("getCurrentMerchant not implemented - needs JWT decode or API call"))
        } else {
            Result.Error(ApiError.AuthenticationError("Not authenticated"))
        }
    }

    override suspend fun createBox(request: CreateBoxRequest): Result<MysteryBox> {
        if (tokenStorage.getMerchantToken() == null) {
            return Result.Error(ApiError.AuthenticationError("請先登入"))
        }

        val dto = CreateBoxRequestDto(
            name = request.name,
            description = request.description,
            originalPrice = request.originalPrice,
            discountedPrice = request.discountedPrice,
            quantity = request.quantity,
            pickupTimeStart = extractPickupTime(request.saleStartTime),
            pickupTimeEnd = calculatePickupEndTime(request.saleStartTime),
            imageUrl = request.imageUrl
        )

        return apiService.createBox(dto).map { it.toDomain() }
    }

    override suspend fun getMerchantBoxes(): Result<List<MysteryBox>> {
        return apiService.getMerchantBoxes().map { dtos ->
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun getDashboard(): Result<MerchantDashboard> {
        return apiService.getMerchantDashboard().map { it.toDomain() }
    }

    override suspend fun getOrders(status: String?, search: String?): Result<List<MerchantOrder>> {
        return apiService.getMerchantOrders(status, search).map { dtos ->
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun verifyOrder(orderId: String): Result<Unit> {
        return apiService.verifyOrder(orderId).map { }
    }

    override suspend fun cancelOrder(orderId: String): Result<Unit> {
        return apiService.cancelMerchantOrder(orderId).map { }
    }

    private fun extractPickupTime(saleStartTime: String): String {
        return saleStartTime.split(",").lastOrNull()?.trim() ?: "18:00"
    }

    private fun calculatePickupEndTime(saleStartTime: String): String {
        val startTime = extractPickupTime(saleStartTime)
        val hour = startTime.split(":").firstOrNull()?.toIntOrNull() ?: 18
        val endHour = minOf(hour + 2, 23)
        return "${endHour.toString().padStart(2, '0')}:00"
    }
}

private fun MerchantDashboardDto.toDomain() = MerchantDashboard(
    todayRevenue = todayRevenue,
    revenueChangePercent = revenueChangePercent,
    todayOrders = todayOrders,
    activeBoxes = activeBoxes,
    storeViews = storeViews,
    recentOrders = recentOrders.map { it.toDomain() }
)

private fun MerchantOrderSummaryDto.toDomain() = MerchantOrderSummary(
    id = id,
    orderId = orderId,
    customerName = customerName,
    customerInitial = customerInitial,
    itemDescription = itemDescription,
    status = MerchantOrderStatus.fromString(status),
    timeAgo = timeAgo
)

private fun MerchantOrderDto.toDomain() = MerchantOrder(
    id = id,
    orderId = orderId,
    orderTime = orderTime,
    status = MerchantOrderStatus.fromString(status),
    isOverdue = isOverdue,
    overdueTime = overdueTime,
    box = MerchantOrderBox(
        id = box.id,
        name = box.name,
        specs = box.specs,
        quantity = box.quantity,
        imageUrl = box.imageUrl
    ),
    customer = MerchantOrderCustomer(
        name = customer.name,
        phone = customer.phone
    ),
    totalPrice = totalPrice
)
