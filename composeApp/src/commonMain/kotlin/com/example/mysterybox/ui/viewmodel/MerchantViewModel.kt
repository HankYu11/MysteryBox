package com.example.mysterybox.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mysterybox.data.model.CreateBoxRequest
import com.example.mysterybox.data.model.Merchant
import com.example.mysterybox.data.model.MerchantDashboard
import com.example.mysterybox.data.model.MerchantLoginRequest
import com.example.mysterybox.data.model.MerchantOrder
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

sealed class DashboardUiState {
    data object Loading : DashboardUiState()
    data class Success(val dashboard: MerchantDashboard) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

sealed class OrdersUiState {
    data object Loading : OrdersUiState()
    data class Success(val orders: List<MerchantOrder>) : OrdersUiState()
    data class Error(val message: String) : OrdersUiState()
}

sealed class OrderActionState {
    data object Idle : OrderActionState()
    data object Loading : OrderActionState()
    data object Success : OrderActionState()
    data class Error(val message: String) : OrderActionState()
}

enum class OrderTab {
    PENDING, COMPLETED, CANCELLED, HISTORY
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

    private val _dashboardState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val dashboardState: StateFlow<DashboardUiState> = _dashboardState.asStateFlow()

    private val _ordersState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val ordersState: StateFlow<OrdersUiState> = _ordersState.asStateFlow()

    private val _orderActionState = MutableStateFlow<OrderActionState>(OrderActionState.Idle)
    val orderActionState: StateFlow<OrderActionState> = _orderActionState.asStateFlow()

    private val _selectedTab = MutableStateFlow(OrderTab.PENDING)
    val selectedTab: StateFlow<OrderTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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

    // Dashboard methods
    fun loadDashboard() {
        viewModelScope.launch {
            _dashboardState.value = DashboardUiState.Loading
            when (val result = merchantRepository.getDashboard()) {
                is Result.Success -> {
                    _dashboardState.value = DashboardUiState.Success(result.data)
                }
                is Result.Error -> {
                    _dashboardState.value = DashboardUiState.Error(result.error.toMessage())
                }
            }
        }
    }

    // Orders methods
    fun loadOrders() {
        viewModelScope.launch {
            _ordersState.value = OrdersUiState.Loading
            val status = when (_selectedTab.value) {
                OrderTab.PENDING -> "PENDING_PICKUP"
                OrderTab.COMPLETED -> "COMPLETED"
                OrderTab.CANCELLED -> "CANCELLED"
                OrderTab.HISTORY -> null
            }
            val search = _searchQuery.value.takeIf { it.isNotBlank() }

            when (val result = merchantRepository.getOrders(status, search)) {
                is Result.Success -> {
                    _ordersState.value = OrdersUiState.Success(result.data)
                }
                is Result.Error -> {
                    _ordersState.value = OrdersUiState.Error(result.error.toMessage())
                }
            }
        }
    }

    fun setSelectedTab(tab: OrderTab) {
        _selectedTab.value = tab
        loadOrders()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchOrders() {
        loadOrders()
    }

    fun verifyOrder(orderId: String) {
        viewModelScope.launch {
            _orderActionState.value = OrderActionState.Loading
            when (val result = merchantRepository.verifyOrder(orderId)) {
                is Result.Success -> {
                    _orderActionState.value = OrderActionState.Success
                    loadOrders()
                }
                is Result.Error -> {
                    _orderActionState.value = OrderActionState.Error(result.error.toMessage())
                }
            }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            _orderActionState.value = OrderActionState.Loading
            when (val result = merchantRepository.cancelOrder(orderId)) {
                is Result.Success -> {
                    _orderActionState.value = OrderActionState.Success
                    loadOrders()
                }
                is Result.Error -> {
                    _orderActionState.value = OrderActionState.Error(result.error.toMessage())
                }
            }
        }
    }

    fun resetOrderActionState() {
        _orderActionState.value = OrderActionState.Idle
    }
}
