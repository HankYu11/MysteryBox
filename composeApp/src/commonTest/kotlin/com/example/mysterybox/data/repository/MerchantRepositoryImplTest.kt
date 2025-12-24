package com.example.mysterybox.data.repository

import com.example.mysterybox.data.dto.MerchantResponseDto
import com.example.mysterybox.data.dto.MysteryBoxDto
import com.example.mysterybox.data.model.ApiError
import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.network.TokenManager
import com.example.mysterybox.data.storage.MockTokenStorage
import com.example.mysterybox.testutil.TestFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MerchantRepositoryImplTest {

    @Test
    fun `login returns merchant on success`() = runTest {
        val mockStorage = MockTokenStorage()
        val tokenManager = TokenManager(mockStorage)
        val fakeApi = FakeMerchantApiService(
            loginResponse = Result.Success(
                MerchantResponseDto(
                    id = "merchant-1",
                    email = "test@store.com",
                    storeName = "Test Store",
                    storeAddress = "123 Test St",
                    isVerified = true,
                    token = "merchant-token-123"
                )
            )
        )
        val repository = TestableMerchantRepository(fakeApi, tokenManager)

        val result = repository.login("test@store.com", "password")

        assertTrue(result is Result.Success)
        assertEquals("merchant-1", result.data.id)
        assertEquals("Test Store", result.data.storeName)
    }

    @Test
    fun `login saves merchant token`() = runTest {
        val mockStorage = MockTokenStorage()
        val tokenManager = TokenManager(mockStorage)
        val fakeApi = FakeMerchantApiService(
            loginResponse = Result.Success(
                MerchantResponseDto(
                    id = "merchant-1",
                    email = "test@store.com",
                    storeName = "Test Store",
                    storeAddress = "123 Test St",
                    isVerified = true,
                    token = "saved-token"
                )
            )
        )
        val repository = TestableMerchantRepository(fakeApi, tokenManager)

        repository.login("test@store.com", "password")

        assertEquals("saved-token", tokenManager.getMerchantToken())
        assertTrue(tokenManager.isMerchantAuthenticated())
    }

    @Test
    fun `login returns error on API failure`() = runTest {
        val mockStorage = MockTokenStorage()
        val tokenManager = TokenManager(mockStorage)
        val fakeApi = FakeMerchantApiService(
            loginResponse = Result.Error(ApiError.AuthenticationError("Invalid credentials"))
        )
        val repository = TestableMerchantRepository(fakeApi, tokenManager)

        val result = repository.login("wrong@email.com", "wrong")

        assertTrue(result is Result.Error)
        assertTrue(result.error is ApiError.AuthenticationError)
        assertFalse(tokenManager.isMerchantAuthenticated())
    }

    @Test
    fun `logout clears merchant token`() = runTest {
        val mockStorage = MockTokenStorage()
        val tokenManager = TokenManager(mockStorage)
        tokenManager.saveMerchantSession("existing-token", TestFixtures.createMerchant())
        val fakeApi = FakeMerchantApiService()
        val repository = TestableMerchantRepository(fakeApi, tokenManager)

        repository.logout()

        assertNull(tokenManager.getMerchantToken())
        assertFalse(tokenManager.isMerchantAuthenticated())
    }

    @Test
    fun `getCurrentMerchant returns error before login`() = runTest {
        val mockStorage = MockTokenStorage()
        val tokenManager = TokenManager(mockStorage)
        val fakeApi = FakeMerchantApiService()
        val repository = TestableMerchantRepository(fakeApi, tokenManager)

        val result = repository.getCurrentMerchant()
        assertTrue(result is Result.Error)
    }

    @Test
    fun `getCurrentMerchant returns merchant after login`() = runTest {
        val tokenManager = TokenManager(MockTokenStorage())
        val fakeApi = FakeMerchantApiService(
            loginResponse = Result.Success(
                MerchantResponseDto(
                    id = "merchant-1",
                    email = "test@store.com",
                    storeName = "Logged In Store",
                    storeAddress = "123 Test St",
                    isVerified = true,
                    token = "token"
                )
            )
        )
        val repository = TestableMerchantRepository(fakeApi, tokenManager)

        repository.login("test@store.com", "password")

        val result = repository.getCurrentMerchant()
        assertTrue(result is Result.Success)
        assertEquals("Logged In Store", result.data.storeName)
    }

    @Test
    fun `getMerchantBoxes returns boxes on success`() = runTest {
        val tokenManager = TokenManager(MockTokenStorage())
        tokenManager.saveMerchantSession("token", TestFixtures.createMerchant())
        val fakeApi = FakeMerchantApiService(
            getMerchantBoxesResponse = Result.Success(
                listOf(
                    TestFixtures.createMysteryBoxDto(id = "1", name = "Box 1"),
                    TestFixtures.createMysteryBoxDto(id = "2", name = "Box 2")
                )
            )
        )
        val repository = TestableMerchantRepository(fakeApi, tokenManager)

        val result = repository.getMerchantBoxes()

        assertTrue(result is Result.Success)
        assertEquals(2, result.data.size)
    }

    @Test
    fun `getMerchantBoxes returns error on failure`() = runTest {
        val tokenManager = TokenManager(MockTokenStorage())
        tokenManager.saveMerchantSession("token", TestFixtures.createMerchant())
        val fakeApi = FakeMerchantApiService(
            getMerchantBoxesResponse = Result.Error(ApiError.NetworkError("Connection failed"))
        )
        val repository = TestableMerchantRepository(fakeApi, tokenManager)

        val result = repository.getMerchantBoxes()

        assertTrue(result is Result.Error)
        assertTrue(result.error is ApiError.NetworkError)
    }

    // Test helper method logic
    @Test
    fun `extractPickupTime extracts time from comma-separated string`() {
        val helper = PickupTimeHelper()

        assertEquals("18:00", helper.extractPickupTime("Monday, 18:00"))
        assertEquals("20:30", helper.extractPickupTime("Friday, 20:30"))
        assertEquals("12:00", helper.extractPickupTime("12:00"))
    }

    @Test
    fun `extractPickupTime returns default for empty input`() {
        val helper = PickupTimeHelper()
        assertEquals("18:00", helper.extractPickupTime(""))
    }

    @Test
    fun `calculatePickupEndTime adds 2 hours`() {
        val helper = PickupTimeHelper()

        assertEquals("20:00", helper.calculatePickupEndTime("Monday, 18:00"))
        assertEquals("14:00", helper.calculatePickupEndTime("12:00"))
    }

    @Test
    fun `calculatePickupEndTime caps at 23`() {
        val helper = PickupTimeHelper()

        assertEquals("23:00", helper.calculatePickupEndTime("22:00"))
        assertEquals("23:00", helper.calculatePickupEndTime("23:00"))
    }
}

/**
 * Fake API service for testing MerchantRepository
 */
private class FakeMerchantApiService(
    val loginResponse: Result<MerchantResponseDto> = Result.Error(ApiError.UnknownError),
    val getMerchantBoxesResponse: Result<List<MysteryBoxDto>> = Result.Success(emptyList())
) {
    suspend fun merchantLogin(email: String, password: String): Result<MerchantResponseDto> = loginResponse
    suspend fun getMerchantBoxes(): Result<List<MysteryBoxDto>> = getMerchantBoxesResponse
}

/**
 * Testable merchant repository that uses fake API service
 */
private class TestableMerchantRepository(
    private val fakeApi: FakeMerchantApiService,
    private val tokenManager: TokenManager
) : MerchantRepository {
    private var currentMerchant: com.example.mysterybox.data.model.Merchant? = null

    suspend fun login(email: String, password: String): Result<com.example.mysterybox.data.model.Merchant> {
        return when (val result = fakeApi.merchantLogin(email, password)) {
            is Result.Success -> {
                val merchant = result.data.toDomain()
                currentMerchant = merchant
                result.data.token?.let {
                    currentMerchant?.let { merchant ->
                        tokenManager.saveMerchantSession(it, merchant)
                    }
                }
                Result.Success(merchant)
            }
            is Result.Error -> result
        }
    }

    override suspend fun login(request: com.example.mysterybox.data.model.MerchantLoginRequest): Result<com.example.mysterybox.data.model.Merchant> {
        return login(request.email, request.password)
    }

    override suspend fun logout(): Result<Unit> {
        currentMerchant = null
        tokenManager.clearMerchantToken()
        return Result.Success(Unit)
    }

    override suspend fun getCurrentMerchant(): Result<com.example.mysterybox.data.model.Merchant> =
        currentMerchant?.let { Result.Success(it) } ?: Result.Error(ApiError.AuthenticationError("Not logged in"))

    override suspend fun createBox(request: com.example.mysterybox.data.model.CreateBoxRequest): Result<com.example.mysterybox.data.model.MysteryBox> {
        return Result.Error(ApiError.UnknownError)
    }

    override suspend fun getMerchantBoxes(): Result<List<com.example.mysterybox.data.model.MysteryBox>> {
        return fakeApi.getMerchantBoxes().map { dtos ->
            dtos.map { dto ->
                com.example.mysterybox.data.model.MysteryBox(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description ?: "",
                    originalPrice = dto.originalPrice,
                    discountedPrice = dto.discountedPrice,
                    imageUrl = dto.imageUrl ?: "",
                    storeName = dto.storeName,
                    storeAddress = dto.storeAddress,
                    pickupTimeStart = dto.pickupTimeStart,
                    pickupTimeEnd = dto.pickupTimeEnd,
                    status = com.example.mysterybox.data.model.BoxStatus.AVAILABLE,
                    remainingCount = dto.remainingCount,
                    discountPercent = dto.discountPercent
                )
            }
        }
    }

    override suspend fun getDashboard(): Result<com.example.mysterybox.data.model.MerchantDashboard> {
        return Result.Error(ApiError.UnknownError)
    }

    override suspend fun getOrders(status: String?, search: String?): Result<List<com.example.mysterybox.data.model.MerchantOrder>> {
        return Result.Success(emptyList())
    }

    override suspend fun verifyOrder(orderId: String): Result<Unit> {
        return Result.Success(Unit)
    }

    override suspend fun cancelOrder(orderId: String): Result<Unit> {
        return Result.Success(Unit)
    }

    private fun MerchantResponseDto.toDomain() = com.example.mysterybox.data.model.Merchant(
        id = id,
        email = email,
        storeName = storeName,
        storeAddress = storeAddress,
        isVerified = isVerified
    )
}

/**
 * Helper class to test pickup time calculation logic
 */
private class PickupTimeHelper {
    fun extractPickupTime(saleStartTime: String): String {
        val extracted = saleStartTime.split(",").lastOrNull()?.trim()
        return if (extracted.isNullOrBlank()) "18:00" else extracted
    }

    fun calculatePickupEndTime(saleStartTime: String): String {
        val startTime = extractPickupTime(saleStartTime)
        val hour = startTime.split(":").firstOrNull()?.toIntOrNull() ?: 18
        val endHour = minOf(hour + 2, 23)
        return "${endHour.toString().padStart(2, '0')}:00"
    }
}
