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
import com.example.mysterybox.data.network.TokenManager
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

data class UploadFormState(
    val boxName: String = "",
    val description: String = "",
    val contentReference: String = "",
    val originalPrice: String = "500",
    val discountedPrice: String = "199",
    val quantity: Int = 5,
    val saleTime: String = "今天, 18:00",
    val imageUrl: String? = null
)

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false
)

class MerchantViewModel(
    private val merchantRepository: MerchantRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<MerchantUiState>(MerchantUiState.Idle)
    val uiState: StateFlow<MerchantUiState> = _uiState.asStateFlow()

    private val _currentMerchant = MutableStateFlow<Merchant?>(null)
    val currentMerchant: StateFlow<Merchant?> = _currentMerchant.asStateFlow()

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

    // Form states for UI screens
    private val _uploadFormState = MutableStateFlow(UploadFormState())
    val uploadFormState: StateFlow<UploadFormState> = _uploadFormState.asStateFlow()

    private val _loginFormState = MutableStateFlow(LoginFormState())
    val loginFormState: StateFlow<LoginFormState> = _loginFormState.asStateFlow()

    init {
        checkMerchantAuth()
    }
    
    private fun checkMerchantAuth() {
        viewModelScope.launch {
            try {
                if (tokenManager.isMerchantAuthenticated()) {
                    val merchant = tokenManager.getCurrentMerchant()
                    val token = tokenManager.getMerchantToken()
                    
                    if (merchant != null && !token.isNullOrEmpty()) {
                        _currentMerchant.value = merchant
                        _uiState.value = MerchantUiState.LoggedIn(merchant)
                        loadMerchantBoxes()
                    } else {
                        // Invalid stored data, clear tokens
                        tokenManager.clearMerchantToken()
                        _uiState.value = MerchantUiState.Idle
                    }
                } else {
                    _uiState.value = MerchantUiState.Idle
                }
            } catch (e: Exception) {
                // Error accessing stored data, clear and start fresh
                try {
                    tokenManager.clearMerchantToken()
                } catch (clearException: Exception) {
                    // Ignore clear errors
                }
                _uiState.value = MerchantUiState.Idle
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = MerchantUiState.Loading

            when (val result = merchantRepository.login(MerchantLoginRequest(email, password))) {
                is Result.Success -> {
                    _currentMerchant.value = result.data
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
        viewModelScope.launch {
            merchantRepository.logout()
            _currentMerchant.value = null
            _uiState.value = MerchantUiState.Idle
            _merchantBoxes.value = emptyList()
        }
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

    fun isLoggedIn(): Boolean = _currentMerchant.value != null

    fun getCurrentMerchant(): Merchant? = _currentMerchant.value

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

    // Upload Form State Management
    fun updateBoxName(name: String) {
        _uploadFormState.value = _uploadFormState.value.copy(boxName = name)
    }

    fun updateDescription(description: String) {
        _uploadFormState.value = _uploadFormState.value.copy(description = description)
    }

    fun updateContentReference(reference: String) {
        _uploadFormState.value = _uploadFormState.value.copy(contentReference = reference)
    }

    fun updateOriginalPrice(price: String) {
        val filteredPrice = price.filter { it.isDigit() }
        _uploadFormState.value = _uploadFormState.value.copy(originalPrice = filteredPrice)
    }

    fun updateDiscountedPrice(price: String) {
        val filteredPrice = price.filter { it.isDigit() }
        _uploadFormState.value = _uploadFormState.value.copy(discountedPrice = filteredPrice)
    }

    fun updateQuantity(quantity: Int) {
        if (quantity >= 1) {
            _uploadFormState.value = _uploadFormState.value.copy(quantity = quantity)
        }
    }

    fun incrementQuantity() {
        _uploadFormState.value = _uploadFormState.value.copy(
            quantity = _uploadFormState.value.quantity + 1
        )
    }

    fun decrementQuantity() {
        val currentQuantity = _uploadFormState.value.quantity
        if (currentQuantity > 1) {
            _uploadFormState.value = _uploadFormState.value.copy(quantity = currentQuantity - 1)
        }
    }

    fun updateSaleTime(time: String) {
        _uploadFormState.value = _uploadFormState.value.copy(saleTime = time)
    }

    fun updateImageUrl(url: String?) {
        _uploadFormState.value = _uploadFormState.value.copy(imageUrl = url)
    }

    fun isUploadFormValid(): Boolean {
        val form = _uploadFormState.value
        return form.boxName.isNotBlank() && 
               form.description.isNotBlank() && 
               form.originalPrice.isNotBlank() && 
               form.discountedPrice.isNotBlank()
    }

    fun resetUploadForm() {
        _uploadFormState.value = UploadFormState()
    }

    fun createBoxFromForm() {
        val form = _uploadFormState.value
        if (isUploadFormValid()) {
            createBox(
                CreateBoxRequest(
                    name = form.boxName,
                    description = form.description,
                    contentReference = form.contentReference,
                    originalPrice = form.originalPrice.toIntOrNull() ?: 0,
                    discountedPrice = form.discountedPrice.toIntOrNull() ?: 0,
                    quantity = form.quantity,
                    saleStartTime = form.saleTime,
                    imageUrl = form.imageUrl
                )
            )
        }
    }

    // Login Form State Management  
    fun updateEmail(email: String) {
        _loginFormState.value = _loginFormState.value.copy(email = email)
    }

    fun updatePassword(password: String) {
        _loginFormState.value = _loginFormState.value.copy(password = password)
    }

    fun togglePasswordVisibility() {
        _loginFormState.value = _loginFormState.value.copy(
            passwordVisible = !_loginFormState.value.passwordVisible
        )
    }

    fun isLoginFormValid(): Boolean {
        val form = _loginFormState.value
        return form.email.isNotBlank() && form.password.isNotBlank()
    }

    fun resetLoginForm() {
        _loginFormState.value = LoginFormState()
    }

    fun loginWithForm() {
        val form = _loginFormState.value
        if (isLoginFormValid()) {
            login(form.email, form.password)
        }
    }
}
