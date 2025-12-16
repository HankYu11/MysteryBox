package com.example.mysterybox.data.repository

import com.example.mysterybox.data.SampleData
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BoxRepositoryImpl : BoxRepository {

    private val _boxes = MutableStateFlow<List<MysteryBox>>(emptyList())

    init {
        // Initialize with sample data - will be replaced with API call
        _boxes.value = SampleData.mysteryBoxes
    }

    override fun getBoxes(): Flow<List<MysteryBox>> = _boxes.asStateFlow()

    override suspend fun getBoxById(id: String): Result<MysteryBox> {
        // Simulate network delay - will be replaced with API call
        delay(100)

        val box = _boxes.value.find { it.id == id }
        return if (box != null) {
            Result.Success(box)
        } else {
            Result.Error("Box not found")
        }
    }

    override suspend fun refreshBoxes() {
        // Simulate network delay - will be replaced with API call
        delay(500)

        // For now, just reload sample data
        // TODO: Replace with actual API call
        _boxes.value = SampleData.mysteryBoxes
    }
}
