package com.example.mysterybox.data.repository

import com.example.mysterybox.data.dto.toDomain
import com.example.mysterybox.data.model.ApiError
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Reservation
import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.network.MysteryBoxApiService

class ReservationRepositoryImpl(
    private val apiService: MysteryBoxApiService
) : ReservationRepository {

    override suspend fun getReservations(): Result<List<Reservation>> {
        return apiService.getReservations().map { dtos ->
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun getReservationById(id: String): Result<Reservation> {
        return when (val result = apiService.getReservations()) {
            is Result.Success -> {
                val reservation = result.data.find { it.id == id }
                if (reservation != null) {
                    Result.Success(reservation.toDomain())
                } else {
                    Result.Error(ApiError.NotFoundError("Reservation not found"))
                }
            }
            is Result.Error -> result
        }
    }

    override suspend fun createReservation(box: MysteryBox): Result<Reservation> {
        return apiService.createReservation(box.id).map { it.toDomain() }
    }

    override suspend fun cancelReservation(id: String): Result<Unit> {
        return apiService.cancelReservation(id)
    }
}
