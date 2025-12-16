package com.example.mysterybox.data.repository

import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Reservation
import com.example.mysterybox.data.model.Result

interface ReservationRepository {
    suspend fun getReservations(): Result<List<Reservation>>
    suspend fun getReservationById(id: String): Result<Reservation>
    suspend fun createReservation(box: MysteryBox): Result<Reservation>
    suspend fun cancelReservation(id: String): Result<Unit>
}
