package com.example.mysterybox.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Reservation
import com.example.mysterybox.data.model.ReservationStatus
import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.repository.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ReservationUiState {
    data object Idle : ReservationUiState()
    data object Loading : ReservationUiState()
    data class Success(val reservation: Reservation) : ReservationUiState()
    data class Error(val message: String) : ReservationUiState()
}

class ReservationViewModel(
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    val reservations: StateFlow<List<Reservation>> = _reservations.asStateFlow()

    private val _createReservationState = MutableStateFlow<ReservationUiState>(ReservationUiState.Idle)
    val createReservationState: StateFlow<ReservationUiState> = _createReservationState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadReservations()
    }

    fun loadReservations() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = reservationRepository.getReservations()) {
                is Result.Success -> _reservations.value = result.data
                is Result.Error -> _reservations.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    fun getActiveReservations(): List<Reservation> {
        return reservations.value.filter {
            it.status != ReservationStatus.COMPLETED && it.status != ReservationStatus.CANCELLED
        }
    }

    fun getPastReservations(): List<Reservation> {
        return reservations.value.filter {
            it.status == ReservationStatus.COMPLETED || it.status == ReservationStatus.CANCELLED
        }
    }

    fun createReservation(box: MysteryBox) {
        viewModelScope.launch {
            _createReservationState.value = ReservationUiState.Loading

            when (val result = reservationRepository.createReservation(box)) {
                is Result.Success -> {
                    _createReservationState.value = ReservationUiState.Success(result.data)
                    // Refresh reservations list to include the new one
                    loadReservations()
                }
                is Result.Error -> {
                    _createReservationState.value = ReservationUiState.Error(result.message)
                }
            }
        }
    }

    fun cancelReservation(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (reservationRepository.cancelReservation(id)) {
                is Result.Success -> loadReservations()
                is Result.Error -> { /* Could add error handling here */ }
            }
            _isLoading.value = false
        }
    }

    fun resetCreateReservationState() {
        _createReservationState.value = ReservationUiState.Idle
    }
}
