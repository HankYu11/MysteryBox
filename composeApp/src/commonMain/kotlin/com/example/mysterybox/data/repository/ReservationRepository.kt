package com.example.mysterybox.data.repository

import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Reservation
import com.example.mysterybox.data.model.Result
import kotlinx.coroutines.flow.Flow

interface ReservationRepository {
    fun getReservations(): Flow<List<Reservation>>
    suspend fun getReservationById(id: String): Result<Reservation>
    suspend fun createReservation(box: MysteryBox): Result<Reservation>
    suspend fun cancelReservation(id: String): Result<Unit>
    suspend fun refreshReservations()
}
