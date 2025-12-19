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

enum class ReservationTab(val index: Int, val displayName: String) {
    ACTIVE(0, "進行中"),
    HISTORY(1, "歷史紀錄")
}

class ReservationViewModel(
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())



    private val _selectedTab = MutableStateFlow(ReservationTab.ACTIVE)
    val selectedTab: StateFlow<ReservationTab> = _selectedTab.asStateFlow()

    private val _filteredReservations = MutableStateFlow<List<Reservation>>(emptyList())
    val filteredReservations: StateFlow<List<Reservation>> = _filteredReservations.asStateFlow()

    init {
        loadReservations()
    }

    fun loadReservations() {
        viewModelScope.launch {
            when (val result = reservationRepository.getReservations()) {
                is Result.Success -> {
                    _reservations.value = result.data
                    updateFilteredReservations()
                }
                is Result.Error -> {
                    _reservations.value = emptyList()
                    _filteredReservations.value = emptyList()
                }
            }
        }
    }

    fun getActiveReservations(): List<Reservation> {
        return _reservations.value.filter {
            it.status != ReservationStatus.COMPLETED && it.status != ReservationStatus.CANCELLED
        }
    }

    fun getPastReservations(): List<Reservation> {
        return _reservations.value.filter {
            it.status == ReservationStatus.COMPLETED || it.status == ReservationStatus.CANCELLED
        }
    }

    fun cancelReservation(id: String) {
        viewModelScope.launch {
            when (reservationRepository.cancelReservation(id)) {
                is Result.Success -> loadReservations()
                is Result.Error -> { /* Could add error handling here */ }
            }
        }
    }


    fun selectTab(tab: ReservationTab) {
        _selectedTab.value = tab
        updateFilteredReservations()
    }

    fun selectTabByIndex(index: Int) {
        val tab = ReservationTab.values().find { it.index == index } ?: ReservationTab.ACTIVE
        selectTab(tab)
    }

    private fun updateFilteredReservations() {
        _filteredReservations.value = when (_selectedTab.value) {
            ReservationTab.ACTIVE -> getActiveReservations()
            ReservationTab.HISTORY -> getPastReservations()
        }
    }

    fun getTabDisplayNames(): List<String> {
        return ReservationTab.values().map { it.displayName }
    }
}
