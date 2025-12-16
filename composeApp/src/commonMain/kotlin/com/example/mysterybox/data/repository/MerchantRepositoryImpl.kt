package com.example.mysterybox.data.repository

import com.example.mysterybox.data.model.BoxStatus
import com.example.mysterybox.data.model.CreateBoxRequest
import com.example.mysterybox.data.model.Merchant
import com.example.mysterybox.data.model.MerchantLoginRequest
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Result
import kotlinx.coroutines.delay

class MerchantRepositoryImpl : MerchantRepository {
    private var currentMerchant: Merchant? = null
    private val merchantBoxes = mutableListOf<MysteryBox>()
    private var boxIdCounter = 100

    override suspend fun login(request: MerchantLoginRequest): Result<Merchant> {
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

    override fun logout() {
        currentMerchant = null
    }

    override fun getCurrentMerchant(): Merchant? = currentMerchant

    override fun isLoggedIn(): Boolean = currentMerchant != null

    override suspend fun createBox(request: CreateBoxRequest): Result<MysteryBox> {
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

    override fun getMerchantBoxes(): List<MysteryBox> = merchantBoxes.toList()
}
