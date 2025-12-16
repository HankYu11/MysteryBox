package com.example.mysterybox.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mysterybox.data.SampleData
import com.example.mysterybox.data.model.BoxStatus
import com.example.mysterybox.data.model.MysteryBox
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BoxViewModel : ViewModel() {

    private val _boxes = MutableStateFlow<List<MysteryBox>>(emptyList())
    val boxes: StateFlow<List<MysteryBox>> = _boxes.asStateFlow()

    private val _selectedBox = MutableStateFlow<MysteryBox?>(null)
    val selectedBox: StateFlow<MysteryBox?> = _selectedBox.asStateFlow()

    private val _selectedFilter = MutableStateFlow(BoxFilter.ALL)
    val selectedFilter: StateFlow<BoxFilter> = _selectedFilter.asStateFlow()

    init {
        loadBoxes()
    }

    fun loadBoxes() {
        _boxes.value = SampleData.mysteryBoxes
    }

    fun setFilter(filter: BoxFilter) {
        _selectedFilter.value = filter
    }

    fun getFilteredBoxes(): List<MysteryBox> {
        return when (_selectedFilter.value) {
            BoxFilter.ALL -> _boxes.value
            BoxFilter.AVAILABLE -> _boxes.value.filter { it.status == BoxStatus.AVAILABLE }
            BoxFilter.ALMOST_SOLD_OUT -> _boxes.value.filter { it.status == BoxStatus.ALMOST_SOLD_OUT }
            BoxFilter.SOLD_OUT -> _boxes.value.filter { it.status == BoxStatus.SOLD_OUT }
        }
    }

    fun loadBoxDetail(boxId: String) {
        _selectedBox.value = _boxes.value.find { it.id == boxId }
    }

    fun clearSelectedBox() {
        _selectedBox.value = null
    }
}

enum class BoxFilter {
    ALL,
    AVAILABLE,
    ALMOST_SOLD_OUT,
    SOLD_OUT
}
