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
