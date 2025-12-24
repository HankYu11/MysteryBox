package com.example.mysterybox.ui.viewmodel

import app.cash.turbine.test
import com.example.mysterybox.data.auth.AuthManager
import com.example.mysterybox.data.model.ApiError
import com.example.mysterybox.data.model.BoxStatus
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Reservation
import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.repository.BoxRepository
import com.example.mysterybox.data.repository.ReservationRepository
import com.example.mysterybox.testutil.TestFixtures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import io.mockk.mockk

@OptIn(ExperimentalCoroutinesApi::class)
class BoxViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadBoxes updates boxes state on success`() = runTest {
        val boxes = listOf(
            TestFixtures.createMysteryBox(id = "1", name = "Box 1"),
            TestFixtures.createMysteryBox(id = "2", name = "Box 2")
        )
        val fakeBoxRepo = FakeBoxRepository(getBoxesResult = Result.Success(boxes))
        val fakeReservationRepo = FakeReservationRepository()

        val viewModel = BoxViewModel(fakeBoxRepo, fakeReservationRepo, mockk<AuthManager>(relaxed = true))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.filteredBoxes.test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("Box 1", result[0].name)
            assertEquals("Box 2", result[1].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadBoxes sets empty list on error`() = runTest {
        val fakeBoxRepo = FakeBoxRepository(
            getBoxesResult = Result.Error(ApiError.NetworkError("Failed"))
        )
        val fakeReservationRepo = FakeReservationRepository()

        val viewModel = BoxViewModel(fakeBoxRepo, fakeReservationRepo, mockk<AuthManager>(relaxed = true))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.filteredBoxes.test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setFilter updates selectedFilter`() = runTest {
        val fakeBoxRepo = FakeBoxRepository()
        val fakeReservationRepo = FakeReservationRepository()

        val viewModel = BoxViewModel(fakeBoxRepo, fakeReservationRepo, mockk<AuthManager>(relaxed = true))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setFilter(BoxFilter.SOLD_OUT)

        viewModel.selectedFilter.test {
            assertEquals(BoxFilter.SOLD_OUT, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filteredBoxes returns only AVAILABLE boxes when filter is AVAILABLE`() = runTest {
        val boxes = listOf(
            TestFixtures.createMysteryBox(id = "1", status = BoxStatus.AVAILABLE),
            TestFixtures.createMysteryBox(id = "2", status = BoxStatus.SOLD_OUT),
            TestFixtures.createMysteryBox(id = "3", status = BoxStatus.AVAILABLE)
        )
        val fakeBoxRepo = FakeBoxRepository(getBoxesResult = Result.Success(boxes))
        val fakeReservationRepo = FakeReservationRepository()

        val viewModel = BoxViewModel(fakeBoxRepo, fakeReservationRepo, mockk<AuthManager>(relaxed = true))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setFilter(BoxFilter.AVAILABLE)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.filteredBoxes.test {
            // Skip initial emissions until we get the filtered result
            var filtered = awaitItem()
            while (filtered.isEmpty()) {
                filtered = awaitItem()
            }
            assertEquals(2, filtered.size)
            assertTrue(filtered.all { it.status == BoxStatus.AVAILABLE })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filteredBoxes returns only SOLD_OUT boxes when filter is SOLD_OUT`() = runTest {
        val boxes = listOf(
            TestFixtures.createMysteryBox(id = "1", status = BoxStatus.AVAILABLE),
            TestFixtures.createMysteryBox(id = "2", status = BoxStatus.SOLD_OUT),
            TestFixtures.createMysteryBox(id = "3", status = BoxStatus.SOLD_OUT)
        )
        val fakeBoxRepo = FakeBoxRepository(getBoxesResult = Result.Success(boxes))
        val fakeReservationRepo = FakeReservationRepository()

        val viewModel = BoxViewModel(fakeBoxRepo, fakeReservationRepo, mockk<AuthManager>(relaxed = true))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setFilter(BoxFilter.SOLD_OUT)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.filteredBoxes.test {
            // Skip initial emissions until we get the filtered result
            var filtered = awaitItem()
            while (filtered.isEmpty()) {
                filtered = awaitItem()
            }
            assertEquals(2, filtered.size)
            assertTrue(filtered.all { it.status == BoxStatus.SOLD_OUT })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filteredBoxes returns all boxes when filter is ALL`() = runTest {
        val boxes = listOf(
            TestFixtures.createMysteryBox(id = "1", status = BoxStatus.AVAILABLE),
            TestFixtures.createMysteryBox(id = "2", status = BoxStatus.SOLD_OUT),
            TestFixtures.createMysteryBox(id = "3", status = BoxStatus.ALMOST_SOLD_OUT)
        )
        val fakeBoxRepo = FakeBoxRepository(getBoxesResult = Result.Success(boxes))
        val fakeReservationRepo = FakeReservationRepository()

        val viewModel = BoxViewModel(fakeBoxRepo, fakeReservationRepo, mockk<AuthManager>(relaxed = true))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setFilter(BoxFilter.ALL)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.filteredBoxes.test {
            // Skip initial emissions until we get the filtered result
            var filtered = awaitItem()
            while (filtered.isEmpty()) {
                filtered = awaitItem()
            }
            assertEquals(3, filtered.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadBoxDetail updates selectedBox on success`() = runTest {
        val box = TestFixtures.createMysteryBox(id = "detail-box", name = "Detail Box")
        val fakeBoxRepo = FakeBoxRepository(getBoxByIdResult = Result.Success(box))
        val fakeReservationRepo = FakeReservationRepository()

        val viewModel = BoxViewModel(fakeBoxRepo, fakeReservationRepo, mockk<AuthManager>(relaxed = true))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadBoxDetail("detail-box")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectedBox.test {
            val result = awaitItem()
            assertEquals("detail-box", result?.id)
            assertEquals("Detail Box", result?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadBoxDetail sets selectedBox to null on error`() = runTest {
        val fakeBoxRepo = FakeBoxRepository(
            getBoxByIdResult = Result.Error(ApiError.NotFoundError("Not found"))
        )
        val fakeReservationRepo = FakeReservationRepository()

        val viewModel = BoxViewModel(fakeBoxRepo, fakeReservationRepo, mockk<AuthManager>(relaxed = true))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadBoxDetail("nonexistent")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectedBox.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearSelectedBox sets selectedBox to null`() = runTest {
        val box = TestFixtures.createMysteryBox()
        val fakeBoxRepo = FakeBoxRepository(getBoxByIdResult = Result.Success(box))
        val fakeReservationRepo = FakeReservationRepository()

        val viewModel = BoxViewModel(fakeBoxRepo, fakeReservationRepo, mockk<AuthManager>(relaxed = true))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadBoxDetail("box-1")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.clearSelectedBox()

        viewModel.selectedBox.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `createReservation updates reservationState to Success`() = runTest {
        val box = TestFixtures.createMysteryBox()
        val reservation = TestFixtures.createReservation(box = box)
        val fakeBoxRepo = FakeBoxRepository()
        val fakeReservationRepo = FakeReservationRepository(
            createReservationResult = Result.Success(reservation)
        )

        val viewModel = BoxViewModel(fakeBoxRepo, fakeReservationRepo, mockk<AuthManager>(relaxed = true))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.createReservation(box)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.reservationState.test {
            val state = awaitItem()
            assertTrue(state is ReservationState.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `createReservation updates reservationState to Error on failure`() = runTest {
        val box = TestFixtures.createMysteryBox()
        val fakeBoxRepo = FakeBoxRepository()
        val fakeReservationRepo = FakeReservationRepository(
            createReservationResult = Result.Error(ApiError.AuthenticationError("Not logged in"))
        )

        val viewModel = BoxViewModel(fakeBoxRepo, fakeReservationRepo, mockk<AuthManager>(relaxed = true))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.createReservation(box)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.reservationState.test {
            val state = awaitItem()
            assertTrue(state is ReservationState.Error)
            assertEquals("Not logged in", (state as ReservationState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resetReservationState sets state to Idle`() = runTest {
        val box = TestFixtures.createMysteryBox()
        val reservation = TestFixtures.createReservation()
        val fakeBoxRepo = FakeBoxRepository()
        val fakeReservationRepo = FakeReservationRepository(
            createReservationResult = Result.Success(reservation)
        )

        val viewModel = BoxViewModel(fakeBoxRepo, fakeReservationRepo, mockk<AuthManager>(relaxed = true))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.createReservation(box)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.resetReservationState()

        viewModel.reservationState.test {
            assertEquals(ReservationState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial reservationState is Idle`() = runTest {
        val fakeBoxRepo = FakeBoxRepository()
        val fakeReservationRepo = FakeReservationRepository()

        val viewModel = BoxViewModel(fakeBoxRepo, fakeReservationRepo, mockk<AuthManager>(relaxed = true))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.reservationState.test {
            assertEquals(ReservationState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}

// Fake implementations
private class FakeBoxRepository(
    private val getBoxesResult: Result<List<MysteryBox>> = Result.Success(emptyList()),
    private val getBoxByIdResult: Result<MysteryBox> = Result.Error(ApiError.NotFoundError("Not found"))
) : BoxRepository {
    override suspend fun getBoxes(): Result<List<MysteryBox>> = getBoxesResult
    override suspend fun getBoxById(id: String): Result<MysteryBox> = getBoxByIdResult
}

private class FakeReservationRepository(
    private val getReservationsResult: Result<List<Reservation>> = Result.Success(emptyList()),
    private val getReservationByIdResult: Result<Reservation> = Result.Error(ApiError.NotFoundError("Not found")),
    private val createReservationResult: Result<Reservation> = Result.Error(ApiError.UnknownError),
    private val cancelReservationResult: Result<Unit> = Result.Success(Unit)
) : ReservationRepository {
    override suspend fun getReservations(): Result<List<Reservation>> = getReservationsResult
    override suspend fun getReservationById(id: String): Result<Reservation> = getReservationByIdResult
    override suspend fun createReservation(box: MysteryBox): Result<Reservation> = createReservationResult
    override suspend fun cancelReservation(id: String): Result<Unit> = cancelReservationResult
}
