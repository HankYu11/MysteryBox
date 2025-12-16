package com.example.mysterybox.data.repository

import com.example.mysterybox.data.SampleData
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Reservation
import com.example.mysterybox.data.model.ReservationStatus
import com.example.mysterybox.data.model.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReservationRepositoryImpl : ReservationRepository {

    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    private var reservationCounter = 100

    init {
        // Initialize with sample data - will be replaced with API call
        _reservations.value = SampleData.reservations
    }

    override fun getReservations(): Flow<List<Reservation>> = _reservations.asStateFlow()

    override suspend fun getReservationById(id: String): Result<Reservation> {
        // Simulate network delay - will be replaced with API call
        delay(100)

        val reservation = _reservations.value.find { it.id == id }
        return if (reservation != null) {
            Result.Success(reservation)
        } else {
            Result.Error("Reservation not found")
        }
    }

    override suspend fun createReservation(box: MysteryBox): Result<Reservation> {
        // Simulate network delay - will be replaced with API call
        delay(500)

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

        _reservations.value = _reservations.value + newReservation
        return Result.Success(newReservation)
    }

    override suspend fun cancelReservation(id: String): Result<Unit> {
        // Simulate network delay - will be replaced with API call
        delay(300)

        val reservation = _reservations.value.find { it.id == id }
        return if (reservation != null) {
            _reservations.value = _reservations.value.filter { it.id != id }
            Result.Success(Unit)
        } else {
            Result.Error("Reservation not found")
        }
    }

    override suspend fun refreshReservations() {
        // Simulate network delay - will be replaced with API call
        delay(500)

        // For now, keep current state
        // TODO: Replace with actual API call
    }
}
