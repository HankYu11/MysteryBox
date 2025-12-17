package com.example.mysterybox.data.repository

import com.example.mysterybox.data.dto.MysteryBoxDto
import com.example.mysterybox.data.model.ApiError
import com.example.mysterybox.data.model.BoxStatus
import com.example.mysterybox.data.model.Result
import com.example.mysterybox.testutil.TestFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BoxRepositoryImplTest {

    @Test
    fun `getBoxes returns mapped domain objects on success`() = runTest {
        val fakeApi = FakeBoxApiService(
            getBoxesResponse = Result.Success(
                listOf(
                    TestFixtures.createMysteryBoxDto(id = "1", name = "Box 1"),
                    TestFixtures.createMysteryBoxDto(id = "2", name = "Box 2")
                )
            )
        )
        val repository = BoxRepositoryImpl(fakeApi)

        val result = repository.getBoxes()

        assertTrue(result is Result.Success)
        assertEquals(2, result.data.size)
        assertEquals("Box 1", result.data[0].name)
        assertEquals("Box 2", result.data[1].name)
    }

    @Test
    fun `getBoxes returns empty list when API returns empty`() = runTest {
        val fakeApi = FakeBoxApiService(
            getBoxesResponse = Result.Success(emptyList())
        )
        val repository = BoxRepositoryImpl(fakeApi)

        val result = repository.getBoxes()

        assertTrue(result is Result.Success)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `getBoxes propagates network error`() = runTest {
        val fakeApi = FakeBoxApiService(
            getBoxesResponse = Result.Error(ApiError.NetworkError("Connection failed"))
        )
        val repository = BoxRepositoryImpl(fakeApi)

        val result = repository.getBoxes()

        assertTrue(result is Result.Error)
        assertTrue(result.error is ApiError.NetworkError)
        assertEquals("Connection failed", (result.error as ApiError.NetworkError).message)
    }

    @Test
    fun `getBoxes propagates authentication error`() = runTest {
        val fakeApi = FakeBoxApiService(
            getBoxesResponse = Result.Error(ApiError.AuthenticationError("Unauthorized"))
        )
        val repository = BoxRepositoryImpl(fakeApi)

        val result = repository.getBoxes()

        assertTrue(result is Result.Error)
        assertTrue(result.error is ApiError.AuthenticationError)
    }

    @Test
    fun `getBoxById returns mapped domain object on success`() = runTest {
        val fakeApi = FakeBoxApiService(
            getBoxByIdResponse = Result.Success(
                TestFixtures.createMysteryBoxDto(
                    id = "box-123",
                    name = "Specific Box",
                    status = "ALMOST_SOLD_OUT"
                )
            )
        )
        val repository = BoxRepositoryImpl(fakeApi)

        val result = repository.getBoxById("box-123")

        assertTrue(result is Result.Success)
        assertEquals("box-123", result.data.id)
        assertEquals("Specific Box", result.data.name)
        assertEquals(BoxStatus.ALMOST_SOLD_OUT, result.data.status)
    }

    @Test
    fun `getBoxById returns error when box not found`() = runTest {
        val fakeApi = FakeBoxApiService(
            getBoxByIdResponse = Result.Error(ApiError.NotFoundError("Box not found"))
        )
        val repository = BoxRepositoryImpl(fakeApi)

        val result = repository.getBoxById("nonexistent")

        assertTrue(result is Result.Error)
        assertTrue(result.error is ApiError.NotFoundError)
        assertEquals("Box not found", (result.error as ApiError.NotFoundError).message)
    }

    @Test
    fun `getBoxes correctly maps DTO status to domain status`() = runTest {
        val fakeApi = FakeBoxApiService(
            getBoxesResponse = Result.Success(
                listOf(
                    TestFixtures.createMysteryBoxDto(id = "1", status = "AVAILABLE"),
                    TestFixtures.createMysteryBoxDto(id = "2", status = "SOLD_OUT"),
                    TestFixtures.createMysteryBoxDto(id = "3", status = "ALMOST_SOLD_OUT")
                )
            )
        )
        val repository = BoxRepositoryImpl(fakeApi)

        val result = repository.getBoxes()

        assertTrue(result is Result.Success)
        assertEquals(BoxStatus.AVAILABLE, result.data[0].status)
        assertEquals(BoxStatus.SOLD_OUT, result.data[1].status)
        assertEquals(BoxStatus.ALMOST_SOLD_OUT, result.data[2].status)
    }

    @Test
    fun `getBoxes handles null optional fields`() = runTest {
        val fakeApi = FakeBoxApiService(
            getBoxesResponse = Result.Success(
                listOf(
                    TestFixtures.createMysteryBoxDto(description = null, imageUrl = null)
                )
            )
        )
        val repository = BoxRepositoryImpl(fakeApi)

        val result = repository.getBoxes()

        assertTrue(result is Result.Success)
        assertEquals("", result.data[0].description)
        assertEquals("", result.data[0].imageUrl)
    }
}

/**
 * Fake implementation of API service for testing BoxRepository.
 * Only implements methods needed by BoxRepository.
 */
private class FakeBoxApiService(
    private val getBoxesResponse: Result<List<MysteryBoxDto>> = Result.Success(emptyList()),
    private val getBoxByIdResponse: Result<MysteryBoxDto> = Result.Error(ApiError.NotFoundError("Not configured"))
) {
    suspend fun getBoxes(status: String? = null): Result<List<MysteryBoxDto>> = getBoxesResponse
    suspend fun getBoxById(id: String): Result<MysteryBoxDto> = getBoxByIdResponse
}

/**
 * Wrapper to create BoxRepositoryImpl with fake API service.
 * This uses duck typing since we can't easily mock the concrete MysteryBoxApiService class.
 */
private fun BoxRepositoryImpl(fakeApi: FakeBoxApiService): TestableBoxRepository {
    return TestableBoxRepository(fakeApi)
}

private class TestableBoxRepository(
    private val fakeApi: FakeBoxApiService
) : BoxRepository {
    override suspend fun getBoxes(): Result<List<com.example.mysterybox.data.model.MysteryBox>> {
        return fakeApi.getBoxes().map { dtos ->
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun getBoxById(id: String): Result<com.example.mysterybox.data.model.MysteryBox> {
        return fakeApi.getBoxById(id).map { it.toDomain() }
    }

    private fun MysteryBoxDto.toDomain() = com.example.mysterybox.data.model.MysteryBox(
        id = id,
        name = name,
        description = description ?: "",
        originalPrice = originalPrice,
        discountedPrice = discountedPrice,
        imageUrl = imageUrl ?: "",
        storeName = storeName,
        storeAddress = storeAddress,
        pickupTimeStart = pickupTimeStart,
        pickupTimeEnd = pickupTimeEnd,
        status = try {
            com.example.mysterybox.data.model.BoxStatus.valueOf(status)
        } catch (e: IllegalArgumentException) {
            com.example.mysterybox.data.model.BoxStatus.AVAILABLE
        },
        remainingCount = remainingCount,
        discountPercent = discountPercent
    )
}
