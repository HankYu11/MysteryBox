package com.example.mysterybox.data.repository

import com.example.mysterybox.data.dto.toDomain
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.network.MysteryBoxApiService

class BoxRepositoryImpl(
    private val apiService: MysteryBoxApiService
) : BoxRepository {

    override suspend fun getBoxes(): Result<List<MysteryBox>> {
        return apiService.getBoxes().map { dtos ->
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun getBoxById(id: String): Result<MysteryBox> {
        return apiService.getBoxById(id).map { it.toDomain() }
    }
}
