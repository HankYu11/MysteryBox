package com.example.mysterybox.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mysterybox.data.model.CreateBoxRequest
import com.example.mysterybox.data.model.Merchant
import com.example.mysterybox.data.model.MerchantLoginRequest
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.repository.MerchantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MerchantUiState {
    data object Idle : MerchantUiState()
    data object Loading : MerchantUiState()
    data class LoggedIn(val merchant: Merchant) : MerchantUiState()
    data class Error(val message: String) : MerchantUiState()
}

sealed class CreateBoxUiState {
    data object Idle : CreateBoxUiState()
    data object Loading : CreateBoxUiState()
    data class Success(val box: MysteryBox) : CreateBoxUiState()
    data class Error(val message: String) : CreateBoxUiState()
}

class MerchantViewModel(
    private val merchantRepository: MerchantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MerchantUiState>(MerchantUiState.Idle)
    val uiState: StateFlow<MerchantUiState> = _uiState.asStateFlow()

    private val _createBoxState = MutableStateFlow<CreateBoxUiState>(CreateBoxUiState.Idle)
    val createBoxState: StateFlow<CreateBoxUiState> = _createBoxState.asStateFlow()

    private val _merchantBoxes = MutableStateFlow<List<MysteryBox>>(emptyList())
    val merchantBoxes: StateFlow<List<MysteryBox>> = _merchantBoxes.asStateFlow()

    init {
        // Check if already logged in
        merchantRepository.getCurrentMerchant()?.let { merchant ->
            _uiState.value = MerchantUiState.LoggedIn(merchant)
            loadMerchantBoxes()
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = MerchantUiState.Loading

            when (val result = merchantRepository.login(MerchantLoginRequest(email, password))) {
                is Result.Success -> {
                    _uiState.value = MerchantUiState.LoggedIn(result.data)
                    loadMerchantBoxes()
                }
                is Result.Error -> {
                    _uiState.value = MerchantUiState.Error(result.error.toMessage())
                }
            }
        }
    }

    fun logout() {
        merchantRepository.logout()
        _uiState.value = MerchantUiState.Idle
        _merchantBoxes.value = emptyList()
    }

    fun createBox(request: CreateBoxRequest) {
        viewModelScope.launch {
            _createBoxState.value = CreateBoxUiState.Loading

            when (val result = merchantRepository.createBox(request)) {
                is Result.Success -> {
                    _createBoxState.value = CreateBoxUiState.Success(result.data)
                    loadMerchantBoxes()
                }
                is Result.Error -> {
                    _createBoxState.value = CreateBoxUiState.Error(result.error.toMessage())
                }
            }
        }
    }

    private fun loadMerchantBoxes() {
        viewModelScope.launch {
            when (val result = merchantRepository.getMerchantBoxes()) {
                is Result.Success -> {
                    _merchantBoxes.value = result.data
                }
                is Result.Error -> {
                    // Silently handle error - boxes will remain empty
                }
            }
        }
    }

    fun resetCreateBoxState() {
        _createBoxState.value = CreateBoxUiState.Idle
    }

    fun clearError() {
        if (_uiState.value is MerchantUiState.Error) {
            _uiState.value = MerchantUiState.Idle
        }
    }

    fun isLoggedIn(): Boolean = merchantRepository.isLoggedIn()

    fun getCurrentMerchant(): Merchant? = merchantRepository.getCurrentMerchant()
}
