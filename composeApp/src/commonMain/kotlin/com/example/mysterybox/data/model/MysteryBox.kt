package com.example.mysterybox.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MysteryBox(
    val id: String,
    val name: String,
    val description: String,
    val originalPrice: Int,
    val discountedPrice: Int,
    val imageUrl: String,
    val storeName: String,
    val storeAddress: String,
    val pickupTimeStart: String,
    val pickupTimeEnd: String,
    val status: BoxStatus,
    val remainingCount: Int = 0,
    val discountPercent: Int = 0
)

@Serializable
enum class BoxStatus {
    AVAILABLE,
    ALMOST_SOLD_OUT,
    SOLD_OUT,
    RESERVED
}

@Serializable
data class Reservation(
    val id: String,
    val orderId: String,
    val box: MysteryBox,
    val status: ReservationStatus,
    val pickupDate: String,
    val pickupTimeStart: String,
    val pickupTimeEnd: String,
    val price: Int
)

@Serializable
enum class ReservationStatus {
    READY_FOR_PICKUP,
    RESERVED,
    PICKUP_MISSED,
    COMPLETED,
    CANCELLED
}
