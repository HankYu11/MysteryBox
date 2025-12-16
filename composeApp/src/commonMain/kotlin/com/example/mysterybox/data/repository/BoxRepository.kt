package com.example.mysterybox.data.repository

import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Result

interface BoxRepository {
    suspend fun getBoxes(): Result<List<MysteryBox>>
    suspend fun getBoxById(id: String): Result<MysteryBox>
}
