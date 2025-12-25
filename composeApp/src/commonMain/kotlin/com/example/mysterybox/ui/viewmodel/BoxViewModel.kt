package com.example.mysterybox.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mysterybox.data.auth.AuthManager
import com.example.mysterybox.data.model.ApiError
import com.example.mysterybox.data.model.BoxStatus
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Reservation
import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.repository.BoxRepository
import com.example.mysterybox.data.repository.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ReservationState {
    data object Idle : ReservationState()
    data object Loading : ReservationState()
    data object Success : ReservationState()
    data class Error(val message: String) : ReservationState()
}

class BoxViewModel(
    private val boxRepository: BoxRepository,
    private val reservationRepository: ReservationRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _boxes = MutableStateFlow<List<MysteryBox>>(emptyList())

    private val _selectedBox = MutableStateFlow<MysteryBox?>(null)
    val selectedBox: StateFlow<MysteryBox?> = _selectedBox.asStateFlow()

    private val _selectedFilter = MutableStateFlow(BoxFilter.ALL)
    val selectedFilter: StateFlow<BoxFilter> = _selectedFilter.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()


    private val _reservationState = MutableStateFlow<ReservationState>(ReservationState.Idle)
    val reservationState: StateFlow<ReservationState> = _reservationState.asStateFlow()

    val filteredBoxes: StateFlow<List<MysteryBox>> = combine(_boxes, _selectedFilter) { boxes, filter ->
        when (filter) {
            BoxFilter.ALL -> boxes
            BoxFilter.AVAILABLE -> boxes.filter { it.status == BoxStatus.AVAILABLE }
            BoxFilter.ALMOST_SOLD_OUT -> boxes.filter { it.status == BoxStatus.ALMOST_SOLD_OUT }
            BoxFilter.SOLD_OUT -> boxes.filter { it.status == BoxStatus.SOLD_OUT }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadBoxes()
    }

    fun loadBoxes() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                when (val result = boxRepository.getBoxes()) {
                    is Result.Success -> _boxes.value = result.data
                    is Result.Error -> _boxes.value = emptyList()
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun setFilter(filter: BoxFilter) {
        _selectedFilter.value = filter
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

    fun createReservation(box: MysteryBox) {
        viewModelScope.launch {
            _reservationState.value = ReservationState.Loading
            when (val result = reservationRepository.createReservation(box)) {
                is Result.Success -> _reservationState.value = ReservationState.Success
                is Result.Error -> {
                    // Auth state updates automatically via TokenStorage flows
                    _reservationState.value = ReservationState.Error(result.error.toMessage())
                }
            }
        }
    }

    fun resetReservationState() {
        _reservationState.value = ReservationState.Idle
    }
}

enum class BoxFilter {
    ALL,
    AVAILABLE,
    ALMOST_SOLD_OUT,
    SOLD_OUT
}
