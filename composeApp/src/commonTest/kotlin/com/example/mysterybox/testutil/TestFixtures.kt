package com.example.mysterybox.testutil

import com.example.mysterybox.data.dto.MysteryBoxDto
import com.example.mysterybox.data.model.BoxStatus
import com.example.mysterybox.data.model.Merchant
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Reservation
import com.example.mysterybox.data.model.ReservationStatus

object TestFixtures {

    fun createMerchant(
        id: String = "merchant-1",
        email: String = "test@store.com",
        storeName: String = "Test Store",
        storeAddress: String = "123 Test Street",
        isVerified: Boolean = true
    ) = Merchant(
        id = id,
        email = email,
        storeName = storeName,
        storeAddress = storeAddress,
        isVerified = isVerified
    )

    fun createMysteryBox(
        id: String = "box-1",
        name: String = "Test Mystery Box",
        description: String = "A test mystery box",
        originalPrice: Int = 500,
        discountedPrice: Int = 350,
        imageUrl: String = "https://example.com/image.jpg",
        storeName: String = "Test Store",
        storeAddress: String = "123 Test Street",
        pickupTimeStart: String = "18:00",
        pickupTimeEnd: String = "20:00",
        status: BoxStatus = BoxStatus.AVAILABLE,
        remainingCount: Int = 5,
        discountPercent: Int = 30
    ) = MysteryBox(
        id = id,
        name = name,
        description = description,
        originalPrice = originalPrice,
        discountedPrice = discountedPrice,
        imageUrl = imageUrl,
        storeName = storeName,
        storeAddress = storeAddress,
        pickupTimeStart = pickupTimeStart,
        pickupTimeEnd = pickupTimeEnd,
        status = status,
        remainingCount = remainingCount,
        discountPercent = discountPercent
    )

    fun createMysteryBoxDto(
        id: String = "box-1",
        name: String = "Test Mystery Box",
        description: String? = "A test mystery box",
        originalPrice: Int = 500,
        discountedPrice: Int = 350,
        imageUrl: String? = "https://example.com/image.jpg",
        storeName: String = "Test Store",
        storeAddress: String = "123 Test Street",
        pickupTimeStart: String = "18:00",
        pickupTimeEnd: String = "20:00",
        status: String = "AVAILABLE",
        remainingCount: Int = 5,
        discountPercent: Int = 30
    ) = MysteryBoxDto(
        id = id,
        name = name,
        description = description,
        originalPrice = originalPrice,
        discountedPrice = discountedPrice,
        imageUrl = imageUrl,
        storeName = storeName,
        storeAddress = storeAddress,
        pickupTimeStart = pickupTimeStart,
        pickupTimeEnd = pickupTimeEnd,
        status = status,
        remainingCount = remainingCount,
        discountPercent = discountPercent
    )

    fun createReservation(
        id: String = "reservation-1",
        orderId: String = "ORD-001",
        box: MysteryBox = createMysteryBox(),
        status: ReservationStatus = ReservationStatus.RESERVED,
        pickupDate: String = "2024-01-15",
        pickupTimeStart: String = "18:00",
        pickupTimeEnd: String = "20:00",
        price: Int = 350
    ) = Reservation(
        id = id,
        orderId = orderId,
        box = box,
        status = status,
        pickupDate = pickupDate,
        pickupTimeStart = pickupTimeStart,
        pickupTimeEnd = pickupTimeEnd,
        price = price
    )
}
