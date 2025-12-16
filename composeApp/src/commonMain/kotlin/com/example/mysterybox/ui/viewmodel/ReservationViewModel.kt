package com.example.mysterybox.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Reservation
import com.example.mysterybox.data.model.ReservationStatus
import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.repository.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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

    val reservations: StateFlow<List<Reservation>> = reservationRepository.getReservations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _createReservationState = MutableStateFlow<ReservationUiState>(ReservationUiState.Idle)
    val createReservationState: StateFlow<ReservationUiState> = _createReservationState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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
            reservationRepository.cancelReservation(id)
            _isLoading.value = false
        }
    }

    fun refreshReservations() {
        viewModelScope.launch {
            _isLoading.value = true
            reservationRepository.refreshReservations()
            _isLoading.value = false
        }
    }

    fun resetCreateReservationState() {
        _createReservationState.value = ReservationUiState.Idle
    }
}
