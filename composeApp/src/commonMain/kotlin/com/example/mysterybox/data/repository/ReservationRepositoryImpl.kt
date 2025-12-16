package com.example.mysterybox.data.repository

import com.example.mysterybox.data.SampleData
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Reservation
import com.example.mysterybox.data.model.ReservationStatus
import com.example.mysterybox.data.model.Result
import kotlinx.coroutines.delay

class ReservationRepositoryImpl : ReservationRepository {

    // In-memory storage for mock - will be replaced with API calls
    private val mockReservations = SampleData.reservations.toMutableList()
    private var reservationCounter = 100

    override suspend fun getReservations(): Result<List<Reservation>> {
        // Simulate network delay - will be replaced with API call
        delay(100)

        // TODO: Replace with actual API call
        return Result.Success(mockReservations.toList())
    }

    override suspend fun getReservationById(id: String): Result<Reservation> {
        // Simulate network delay - will be replaced with API call
        delay(100)

        // TODO: Replace with actual API call
        val reservation = mockReservations.find { it.id == id }
        return if (reservation != null) {
            Result.Success(reservation)
        } else {
            Result.Error("Reservation not found")
        }
    }

    override suspend fun createReservation(box: MysteryBox): Result<Reservation> {
        // Simulate network delay - will be replaced with API call
        delay(500)

        // TODO: Replace with actual API call
        val newReservation = Reservation(
            id = "r${++reservationCounter}",
            orderId = "#${8800 + reservationCounter}",
            box = box,
            status = ReservationStatus.RESERVED,
            pickupDate = "今日",
            pickupTimeStart = box.pickupTimeStart,
            pickupTimeEnd = box.pickupTimeEnd,
            price = box.discountedPrice
        )

        mockReservations.add(newReservation)
        return Result.Success(newReservation)
    }

    override suspend fun cancelReservation(id: String): Result<Unit> {
        // Simulate network delay - will be replaced with API call
        delay(300)

        // TODO: Replace with actual API call
        val index = mockReservations.indexOfFirst { it.id == id }
        return if (index >= 0) {
            mockReservations.removeAt(index)
            Result.Success(Unit)
        } else {
            Result.Error("Reservation not found")
        }
    }
}
