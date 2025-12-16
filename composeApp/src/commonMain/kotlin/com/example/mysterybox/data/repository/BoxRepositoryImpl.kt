package com.example.mysterybox.data.repository

import com.example.mysterybox.data.SampleData
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Result
import kotlinx.coroutines.delay

class BoxRepositoryImpl : BoxRepository {

    override suspend fun getBoxes(): Result<List<MysteryBox>> {
        // Simulate network delay - will be replaced with API call
        delay(100)

        // TODO: Replace with actual API call
        return Result.Success(SampleData.mysteryBoxes)
    }

    override suspend fun getBoxById(id: String): Result<MysteryBox> {
        // Simulate network delay - will be replaced with API call
        delay(100)

        // TODO: Replace with actual API call
        val box = SampleData.mysteryBoxes.find { it.id == id }
        return if (box != null) {
            Result.Success(box)
        } else {
            Result.Error("Box not found")
        }
    }
}
