package com.example.mysterybox.data.repository

import com.example.mysterybox.data.model.BoxStatus
import com.example.mysterybox.data.model.CreateBoxRequest
import com.example.mysterybox.data.model.Merchant
import com.example.mysterybox.data.model.MerchantLoginRequest
import com.example.mysterybox.data.model.MysteryBox
import kotlinx.coroutines.delay

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

class MerchantRepository {
    private var currentMerchant: Merchant? = null
    private val merchantBoxes = mutableListOf<MysteryBox>()
    private var boxIdCounter = 100

    suspend fun login(request: MerchantLoginRequest): Result<Merchant> {
        // Simulate network delay
        delay(1000)

        // Mock validation
        return if (request.email.isNotBlank() && request.password.length >= 6) {
            val merchant = Merchant(
                id = "merchant_001",
                email = request.email,
                storeName = "Downtown Happy Bakery",
                storeAddress = "台北大安旗艦店",
                isVerified = true
            )
            currentMerchant = merchant
            Result.Success(merchant)
        } else {
            Result.Error("帳號或密碼錯誤")
        }
    }

    fun logout() {
        currentMerchant = null
    }

    fun getCurrentMerchant(): Merchant? = currentMerchant

    fun isLoggedIn(): Boolean = currentMerchant != null

    suspend fun createBox(request: CreateBoxRequest): Result<MysteryBox> {
        val merchant = currentMerchant ?: return Result.Error("請先登入")

        // Simulate network delay
        delay(800)

        val newBox = MysteryBox(
            id = "box_${++boxIdCounter}",
            name = request.name,
            description = request.description,
            originalPrice = request.originalPrice,
            discountedPrice = request.discountedPrice,
            imageUrl = request.imageUrl ?: "custom",
            storeName = merchant.storeName,
            storeAddress = merchant.storeAddress,
            pickupTimeStart = request.saleStartTime.split(",").lastOrNull()?.trim() ?: "18:00",
            pickupTimeEnd = "21:00",
            status = BoxStatus.AVAILABLE,
            remainingCount = request.quantity,
            discountPercent = ((request.originalPrice - request.discountedPrice) * 100 / request.originalPrice)
        )

        merchantBoxes.add(newBox)
        return Result.Success(newBox)
    }

    fun getMerchantBoxes(): List<MysteryBox> = merchantBoxes.toList()

    companion object {
        private var instance: MerchantRepository? = null

        fun getInstance(): MerchantRepository {
            return instance ?: MerchantRepository().also { instance = it }
        }
    }
}
