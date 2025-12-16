package com.example.mysterybox.data.repository

import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Result
import kotlinx.coroutines.flow.Flow

interface BoxRepository {
    fun getBoxes(): Flow<List<MysteryBox>>
    suspend fun getBoxById(id: String): Result<MysteryBox>
    suspend fun refreshBoxes()
}
