package com.example.mysterybox.data.dto

import com.example.mysterybox.data.model.BoxStatus
import com.example.mysterybox.data.model.MysteryBox
import kotlinx.serialization.Serializable

@Serializable
data class MysteryBoxDto(
    val id: String,
    val name: String,
    val description: String?,
    val originalPrice: Int,
    val discountedPrice: Int,
    val imageUrl: String?,
    val storeName: String,
    val storeAddress: String,
    val pickupTimeStart: String,
    val pickupTimeEnd: String,
    val status: String,
    val remainingCount: Int,
    val discountPercent: Int
)

fun MysteryBoxDto.toDomain(): MysteryBox = MysteryBox(
    id = id,
    name = name,
    description = description ?: "",
    originalPrice = originalPrice,
    discountedPrice = discountedPrice,
    imageUrl = imageUrl ?: "",
    storeName = storeName,
    storeAddress = storeAddress,
    pickupTimeStart = pickupTimeStart,
    pickupTimeEnd = pickupTimeEnd,
    status = parseBoxStatus(status),
    remainingCount = remainingCount,
    discountPercent = discountPercent
)

private fun parseBoxStatus(status: String): BoxStatus {
    return try {
        BoxStatus.valueOf(status)
    } catch (e: IllegalArgumentException) {
        BoxStatus.AVAILABLE
    }
}
