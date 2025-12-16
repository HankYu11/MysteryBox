package com.example.mysterybox.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mysterybox.data.model.BoxStatus
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.repository.BoxRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BoxViewModel(
    private val boxRepository: BoxRepository
) : ViewModel() {

    val boxes: StateFlow<List<MysteryBox>> = boxRepository.getBoxes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedBox = MutableStateFlow<MysteryBox?>(null)
    val selectedBox: StateFlow<MysteryBox?> = _selectedBox.asStateFlow()

    private val _selectedFilter = MutableStateFlow(BoxFilter.ALL)
    val selectedFilter: StateFlow<BoxFilter> = _selectedFilter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun refreshBoxes() {
        viewModelScope.launch {
            _isLoading.value = true
            boxRepository.refreshBoxes()
            _isLoading.value = false
        }
    }

    fun setFilter(filter: BoxFilter) {
        _selectedFilter.value = filter
    }

    fun getFilteredBoxes(): List<MysteryBox> {
        return when (_selectedFilter.value) {
            BoxFilter.ALL -> boxes.value
            BoxFilter.AVAILABLE -> boxes.value.filter { it.status == BoxStatus.AVAILABLE }
            BoxFilter.ALMOST_SOLD_OUT -> boxes.value.filter { it.status == BoxStatus.ALMOST_SOLD_OUT }
            BoxFilter.SOLD_OUT -> boxes.value.filter { it.status == BoxStatus.SOLD_OUT }
        }
    }

    fun loadBoxDetail(boxId: String) {
        viewModelScope.launch {
            when (val result = boxRepository.getBoxById(boxId)) {
                is Result.Success -> _selectedBox.value = result.data
                is Result.Error -> _selectedBox.value = null
            }
        }
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
