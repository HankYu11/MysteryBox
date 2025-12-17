package com.example.mysterybox.data.dto

import com.example.mysterybox.data.model.Merchant
import kotlinx.serialization.Serializable

@Serializable
data class MerchantLoginRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class MerchantResponseDto(
    val id: String,
    val email: String,
    val storeName: String,
    val storeAddress: String,
    val isVerified: Boolean,
    val token: String? = null
)

@Serializable
data class CreateBoxRequestDto(
    val name: String,
    val description: String? = null,
    val originalPrice: Int,
    val discountedPrice: Int,
    val quantity: Int,
    val pickupTimeStart: String,
    val pickupTimeEnd: String,
    val imageUrl: String? = null
)

fun MerchantResponseDto.toDomain(): Merchant = Merchant(
    id = id,
    email = email,
    storeName = storeName,
    storeAddress = storeAddress,
    isVerified = isVerified
)
