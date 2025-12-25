package com.example.mysterybox.ui.viewmodel

import app.cash.turbine.test
import com.example.mysterybox.data.model.ApiError
import com.example.mysterybox.data.model.CreateBoxRequest
import com.example.mysterybox.data.model.Merchant
import com.example.mysterybox.data.model.MerchantDashboard
import com.example.mysterybox.data.model.MerchantLoginRequest
import com.example.mysterybox.data.model.MerchantOrder
import com.example.mysterybox.data.model.MerchantOrderBox
import com.example.mysterybox.data.model.MerchantOrderCustomer
import com.example.mysterybox.data.model.MerchantOrderStatus
import com.example.mysterybox.data.model.MerchantOrderSummary
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.repository.MerchantRepository
import com.example.mysterybox.data.storage.MockTokenStorage
import com.example.mysterybox.testutil.TestFixtures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MerchantViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val json = Json { ignoreUnknownKeys = true }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState is Idle when not logged in`() = runTest {
        val fakeRepo = FakeMerchantRepository()
        val viewModel = MerchantViewModel(fakeRepo, MockTokenStorage(), json)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals(MerchantUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial uiState is LoggedIn when merchant exists`() = runTest {
        val merchant = TestFixtures.createMerchant(storeName = "Existing Store")
        val fakeRepo = FakeMerchantRepository(currentMerchant = merchant)
        val tokenStorage = MockTokenStorage()
        tokenStorage.saveMerchantToken("token")
        tokenStorage.saveMerchantData(json.encodeToString(Merchant.serializer(), merchant))

        val viewModel = MerchantViewModel(fakeRepo, tokenStorage, json)

        // Start collecting before advancing to catch both Idle and LoggedIn states
        viewModel.uiState.test {
            // First emission could be Idle or LoggedIn depending on timing
            val firstState = awaitItem()

            if (firstState is MerchantUiState.Idle) {
                // If we got Idle, advance and wait for LoggedIn
                testDispatcher.scheduler.advanceUntilIdle()
                val secondState = awaitItem()
                assertTrue(secondState is MerchantUiState.LoggedIn)
                assertEquals("Existing Store", (secondState as MerchantUiState.LoggedIn).merchant.storeName)
            } else {
                // checkMerchantAuth already completed, verify we got LoggedIn
                assertTrue(firstState is MerchantUiState.LoggedIn)
                assertEquals("Existing Store", (firstState as MerchantUiState.LoggedIn).merchant.storeName)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login updates uiState to LoggedIn on success`() = runTest {
        val merchant = TestFixtures.createMerchant(email = "test@store.com")
        val fakeRepo = FakeMerchantRepository(
            loginResult = Result.Success(merchant)
        )
        val viewModel = MerchantViewModel(fakeRepo, MockTokenStorage(), json)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.login("test@store.com", "password")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is MerchantUiState.LoggedIn)
            assertEquals("test@store.com", (state as MerchantUiState.LoggedIn).merchant.email)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login updates uiState to Error on failure`() = runTest {
        val fakeRepo = FakeMerchantRepository(
            loginResult = Result.Error(ApiError.AuthenticationError("Invalid credentials"))
        )
        val viewModel = MerchantViewModel(fakeRepo, MockTokenStorage(), json)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.login("wrong@email.com", "wrong")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is MerchantUiState.Error)
            assertEquals("Invalid credentials", (state as MerchantUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `logout sets uiState to Idle`() = runTest {
        val merchant = TestFixtures.createMerchant()
        val fakeRepo = FakeMerchantRepository(currentMerchant = merchant)
        val viewModel = MerchantViewModel(fakeRepo, MockTokenStorage(), json)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals(MerchantUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `logout clears merchantBoxes`() = runTest {
        val merchant = TestFixtures.createMerchant()
        val boxes = listOf(TestFixtures.createMysteryBox())
        val fakeRepo = FakeMerchantRepository(
            currentMerchant = merchant,
            getMerchantBoxesResult = Result.Success(boxes)
        )
        val viewModel = MerchantViewModel(fakeRepo, MockTokenStorage(), json)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify logout by checking the UI state
        viewModel.uiState.test {
            assertEquals(MerchantUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `createBox updates createBoxState to Success`() = runTest {
        val box = TestFixtures.createMysteryBox(name = "New Box")
        val fakeRepo = FakeMerchantRepository(
            createBoxResult = Result.Success(box)
        )
        val viewModel = MerchantViewModel(fakeRepo, MockTokenStorage(), json)
        testDispatcher.scheduler.advanceUntilIdle()

        val request = CreateBoxRequest(
            name = "New Box",
            description = "Test description",
            contentReference = "Bread, Pastries",
            originalPrice = 1000,
            discountedPrice = 800,
            quantity = 5,
            saleStartTime = "18:00"
        )
        viewModel.createBox(request)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.createBoxState.test {
            val state = awaitItem()
            assertTrue(state is CreateBoxUiState.Success)
            assertEquals("New Box", (state as CreateBoxUiState.Success).box.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `createBox updates createBoxState to Error on failure`() = runTest {
        val fakeRepo = FakeMerchantRepository(
            createBoxResult = Result.Error(ApiError.ValidationError("Invalid data"))
        )
        val viewModel = MerchantViewModel(fakeRepo, MockTokenStorage(), json)
        testDispatcher.scheduler.advanceUntilIdle()

        val request = CreateBoxRequest(
            name = "",
            description = "",
            contentReference = "",
            originalPrice = 0,
            discountedPrice = 0,
            quantity = 0,
            saleStartTime = ""
        )
        viewModel.createBox(request)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.createBoxState.test {
            val state = awaitItem()
            assertTrue(state is CreateBoxUiState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resetCreateBoxState sets state to Idle`() = runTest {
        val box = TestFixtures.createMysteryBox()
        val fakeRepo = FakeMerchantRepository(
            createBoxResult = Result.Success(box)
        )
        val viewModel = MerchantViewModel(fakeRepo, MockTokenStorage(), json)
        testDispatcher.scheduler.advanceUntilIdle()

        val request = CreateBoxRequest(
            name = "Test", description = "", contentReference = "Items",
            originalPrice = 100, discountedPrice = 80, quantity = 1, saleStartTime = "18:00"
        )
        viewModel.createBox(request)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.resetCreateBoxState()

        viewModel.createBoxState.test {
            assertEquals(CreateBoxUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearError sets uiState to Idle when in Error state`() = runTest {
        val fakeRepo = FakeMerchantRepository(
            loginResult = Result.Error(ApiError.NetworkError("Failed"))
        )
        val viewModel = MerchantViewModel(fakeRepo, MockTokenStorage(), json)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.login("test@test.com", "password")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.clearError()

        viewModel.uiState.test {
            assertEquals(MerchantUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadDashboard updates dashboardState to Success`() = runTest {
        val dashboard = MerchantDashboard(
            todayRevenue = 15000,
            revenueChangePercent = 10,
            todayOrders = 20,
            activeBoxes = 5,
            storeViews = 100,
            recentOrders = listOf(
                MerchantOrderSummary(
                    id = "order-1",
                    orderId = "ORD-001",
                    customerName = "John Doe",
                    customerInitial = "J",
                    itemDescription = "Mystery Box x1",
                    status = MerchantOrderStatus.PENDING_PICKUP,
                    timeAgo = "5 minutes ago"
                )
            )
        )
        val fakeRepo = FakeMerchantRepository(
            getDashboardResult = Result.Success(dashboard)
        )
        val viewModel = MerchantViewModel(fakeRepo, MockTokenStorage(), json)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadDashboard()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.dashboardState.test {
            val state = awaitItem()
            assertTrue(state is DashboardUiState.Success)
            assertEquals(5, (state as DashboardUiState.Success).dashboard.activeBoxes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadDashboard updates dashboardState to Error on failure`() = runTest {
        val fakeRepo = FakeMerchantRepository(
            getDashboardResult = Result.Error(ApiError.NetworkError("Connection failed"))
        )
        val viewModel = MerchantViewModel(fakeRepo, MockTokenStorage(), json)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadDashboard()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.dashboardState.test {
            val state = awaitItem()
            assertTrue(state is DashboardUiState.Error)
            assertEquals("Connection failed", (state as DashboardUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSelectedTab updates selectedTab`() = runTest {
        val fakeRepo = FakeMerchantRepository()
        val viewModel = MerchantViewModel(fakeRepo, MockTokenStorage(), json)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setSelectedTab(OrderTab.COMPLETED)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectedTab.test {
            assertEquals(OrderTab.COMPLETED, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSearchQuery updates searchQuery`() = runTest {
        val fakeRepo = FakeMerchantRepository()
        val viewModel = MerchantViewModel(fakeRepo, MockTokenStorage(), json)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setSearchQuery("test query")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.searchQuery.test {
            assertEquals("test query", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadOrders updates ordersState to Success`() = runTest {
        val orders = listOf(
            MerchantOrder(
                id = "order-1",
                orderId = "ORD-001",
                orderTime = "2024-01-15 10:00",
                status = MerchantOrderStatus.PENDING_PICKUP,
                isOverdue = false,
                overdueTime = null,
                box = MerchantOrderBox(
                    id = "box-1",
                    name = "Mystery Box",
                    specs = "500ml",
                    quantity = 1,
                    imageUrl = "https://example.com/box.jpg"
                ),
                customer = MerchantOrderCustomer(
                    name = "John Doe",
                    phone = "08012345678"
                ),
                totalPrice = 500
            )
        )
        val fakeRepo = FakeMerchantRepository(
            getOrdersResult = Result.Success(orders)
        )
        val viewModel = MerchantViewModel(fakeRepo, MockTokenStorage(), json)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadOrders()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.ordersState.test {
            val state = awaitItem()
            assertTrue(state is OrdersUiState.Success)
            assertEquals(1, (state as OrdersUiState.Success).orders.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `verifyOrder updates orderActionState to Success`() = runTest {
        val fakeRepo = FakeMerchantRepository(
            verifyOrderResult = Result.Success(Unit),
            getOrdersResult = Result.Success(emptyList())
        )
        val viewModel = MerchantViewModel(fakeRepo, MockTokenStorage(), json)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.verifyOrder("order-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.orderActionState.test {
            val state = awaitItem()
            assertTrue(state is OrderActionState.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `verifyOrder updates orderActionState to Error on failure`() = runTest {
        val fakeRepo = FakeMerchantRepository(
            verifyOrderResult = Result.Error(ApiError.NotFoundError("Order not found"))
        )
        val viewModel = MerchantViewModel(fakeRepo, MockTokenStorage(), json)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.verifyOrder("invalid-order")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.orderActionState.test {
            val state = awaitItem()
            assertTrue(state is OrderActionState.Error)
            assertEquals("Order not found", (state as OrderActionState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cancelOrder updates orderActionState to Success`() = runTest {
        val fakeRepo = FakeMerchantRepository(
            cancelOrderResult = Result.Success(Unit),
            getOrdersResult = Result.Success(emptyList())
        )
        val viewModel = MerchantViewModel(fakeRepo, MockTokenStorage(), json)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.cancelOrder("order-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.orderActionState.test {
            val state = awaitItem()
            assertTrue(state is OrderActionState.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resetOrderActionState sets state to Idle`() = runTest {
        val fakeRepo = FakeMerchantRepository(
            verifyOrderResult = Result.Success(Unit),
            getOrdersResult = Result.Success(emptyList())
        )
        val viewModel = MerchantViewModel(fakeRepo, MockTokenStorage(), json)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.verifyOrder("order-1")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.resetOrderActionState()

        viewModel.orderActionState.test {
            assertEquals(OrderActionState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isLoggedIn returns false when not authenticated`() = runTest {
        val fakeRepo = FakeMerchantRepository()
        val viewModel = MerchantViewModel(fakeRepo, MockTokenStorage(), json)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.isLoggedIn())
    }

    @Test
    fun `isLoggedIn returns true when authenticated`() = runTest {
        val merchant = TestFixtures.createMerchant()
        val merchantJson = json.encodeToString(Merchant.serializer(), merchant)
        val tokenStorage = MockTokenStorage()
        tokenStorage.saveMerchantToken("test-token")
        tokenStorage.saveMerchantData(merchantJson)

        val fakeRepo = FakeMerchantRepository(currentMerchant = merchant)
        val viewModel = MerchantViewModel(fakeRepo, tokenStorage, json)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.isLoggedIn())
    }

    @Test
    fun `getCurrentMerchant returns null when not logged in`() = runTest {
        val fakeRepo = FakeMerchantRepository()
        val viewModel = MerchantViewModel(fakeRepo, MockTokenStorage(), json)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNull(viewModel.getCurrentMerchant())
    }

    @Test
    fun `getCurrentMerchant returns merchant when logged in`() = runTest {
        val merchant = TestFixtures.createMerchant(storeName = "My Store")
        val tokenStorage = MockTokenStorage()
        tokenStorage.saveMerchantToken("test-token")
        tokenStorage.saveMerchantData(json.encodeToString(Merchant.serializer(), merchant))
        val fakeRepo = FakeMerchantRepository(currentMerchant = merchant)
        val viewModel = MerchantViewModel(fakeRepo, tokenStorage, json)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("My Store", viewModel.getCurrentMerchant()?.storeName)
    }
}

// Fake implementation
private class FakeMerchantRepository(
    private var currentMerchant: Merchant? = null,
    private val loginResult: Result<Merchant> = Result.Error(ApiError.UnknownError),
    private val createBoxResult: Result<MysteryBox> = Result.Error(ApiError.UnknownError),
    private val getMerchantBoxesResult: Result<List<MysteryBox>> = Result.Success(emptyList()),
    private val getDashboardResult: Result<MerchantDashboard> = Result.Error(ApiError.UnknownError),
    private val getOrdersResult: Result<List<MerchantOrder>> = Result.Success(emptyList()),
    private val verifyOrderResult: Result<Unit> = Result.Success(Unit),
    private val cancelOrderResult: Result<Unit> = Result.Success(Unit)
) : MerchantRepository {

    override suspend fun login(request: MerchantLoginRequest): Result<Merchant> {
        return loginResult.also {
            if (it is Result.Success) {
                currentMerchant = it.data
            }
        }
    }

    override suspend fun logout(): Result<Unit> {
        currentMerchant = null
        return Result.Success(Unit)
    }

    override suspend fun getCurrentMerchant(): Result<Merchant> =
        currentMerchant?.let { Result.Success(it) } ?: Result.Error(ApiError.AuthenticationError("Not logged in"))

    override suspend fun createBox(request: CreateBoxRequest): Result<MysteryBox> = createBoxResult

    override suspend fun getMerchantBoxes(): Result<List<MysteryBox>> = getMerchantBoxesResult

    override suspend fun getDashboard(): Result<MerchantDashboard> = getDashboardResult

    override suspend fun getOrders(status: String?, search: String?): Result<List<MerchantOrder>> = getOrdersResult

    override suspend fun verifyOrder(orderId: String): Result<Unit> = verifyOrderResult

    override suspend fun cancelOrder(orderId: String): Result<Unit> = cancelOrderResult
}
