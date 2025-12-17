package com.example.mysterybox.data.dto

import com.example.mysterybox.data.model.BoxStatus
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Reservation
import com.example.mysterybox.data.model.ReservationStatus
import kotlinx.serialization.Serializable

@Serializable
data class ReservationDto(
    val id: String,
    val orderId: String,
    val box: ReservationBoxInfoDto,
    val status: String,
    val pickupDate: String,
    val pickupTimeStart: String,
    val pickupTimeEnd: String,
    val price: Int
)

@Serializable
data class ReservationBoxInfoDto(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val storeName: String,
    val storeAddress: String
)

@Serializable
data class CreateReservationRequestDto(
    val boxId: String
)

@Serializable
data class ReservationCreatedDto(
    val id: String,
    val orderId: String,
    val status: String,
    val pickupDate: String,
    val price: Int
)

fun ReservationDto.toDomain(): Reservation = Reservation(
    id = id,
    orderId = orderId,
    box = MysteryBox(
        id = box.id,
        name = box.name,
        description = "",
        originalPrice = price,
        discountedPrice = price,
        imageUrl = box.imageUrl ?: "",
        storeName = box.storeName,
        storeAddress = box.storeAddress,
        pickupTimeStart = pickupTimeStart,
        pickupTimeEnd = pickupTimeEnd,
        status = BoxStatus.RESERVED,
        remainingCount = 0,
        discountPercent = 0
    ),
    status = parseReservationStatus(status),
    pickupDate = pickupDate,
    pickupTimeStart = pickupTimeStart,
    pickupTimeEnd = pickupTimeEnd,
    price = price
)

private fun parseReservationStatus(status: String): ReservationStatus {
    return try {
        ReservationStatus.valueOf(status)
    } catch (e: IllegalArgumentException) {
        ReservationStatus.RESERVED
    }
}
