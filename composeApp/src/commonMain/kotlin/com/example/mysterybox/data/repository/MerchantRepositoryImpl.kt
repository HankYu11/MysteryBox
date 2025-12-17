package com.example.mysterybox.data.repository

import com.example.mysterybox.data.dto.CreateBoxRequestDto
import com.example.mysterybox.data.dto.toDomain
import com.example.mysterybox.data.model.ApiError
import com.example.mysterybox.data.model.CreateBoxRequest
import com.example.mysterybox.data.model.Merchant
import com.example.mysterybox.data.model.MerchantLoginRequest
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.network.MysteryBoxApiService
import com.example.mysterybox.data.network.TokenManager

class MerchantRepositoryImpl(
    private val apiService: MysteryBoxApiService,
    private val tokenManager: TokenManager
) : MerchantRepository {
    private var currentMerchant: Merchant? = null

    override suspend fun login(request: MerchantLoginRequest): Result<Merchant> {
        return when (val result = apiService.merchantLogin(request.email, request.password)) {
            is Result.Success -> {
                val merchantResponse = result.data
                val merchant = merchantResponse.toDomain()
                currentMerchant = merchant
                merchantResponse.token?.let { tokenManager.saveMerchantToken(it) }
                Result.Success(merchant)
            }
            is Result.Error -> result
        }
    }

    override fun logout() {
        currentMerchant = null
        tokenManager.clearMerchantToken()
    }

    override fun getCurrentMerchant(): Merchant? = currentMerchant

    override fun isLoggedIn(): Boolean = tokenManager.isMerchantAuthenticated()

    override suspend fun createBox(request: CreateBoxRequest): Result<MysteryBox> {
        if (!isLoggedIn()) {
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
