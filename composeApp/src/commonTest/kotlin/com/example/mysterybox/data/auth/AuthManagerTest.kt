package com.example.mysterybox.data.auth

import app.cash.turbine.test
import com.example.mysterybox.data.model.ApiError
import com.example.mysterybox.data.model.AuthSession
import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.model.User
import com.example.mysterybox.data.repository.AuthRepository
import com.example.mysterybox.data.storage.MockTokenStorage
import com.example.mysterybox.ui.state.AuthState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class AuthManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val json = Json { ignoreUnknownKeys = true }
    private val testUser = User(
        id = "user123",
        displayName = "Test User",
        pictureUrl = "https://example.com/pic.jpg"
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createAuthManager(
        tokenStorage: MockTokenStorage = MockTokenStorage(),
        authRepository: AuthRepository = FakeAuthRepository(),
        scope: CoroutineScope = CoroutineScope(SupervisorJob() + testDispatcher)
    ): AuthManager {
        return AuthManager(
            tokenStorage = tokenStorage,
            authRepository = authRepository,
            json = json,
            scope = scope
        )
    }

    // AuthState Reactivity Tests

    @Test
    fun `authState emits Loading initially`() = runTest {
        val authManager = createAuthManager()
        testDispatcher.scheduler.advanceUntilIdle()

        authManager.authState.test {
            val state = awaitItem()
            assertIs<AuthState.Loading>(state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `authState emits Unauthenticated when no tokens`() = runTest {
        val tokenStorage = MockTokenStorage()
        val authManager = createAuthManager(tokenStorage)
        testDispatcher.scheduler.advanceUntilIdle()

        authManager.authState.test {
            skipItems(1) // Skip Loading
            val state = awaitItem()
            assertIs<AuthState.Unauthenticated>(state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `authState emits Authenticated when both token and userData exist`() = runTest {
        val tokenStorage = MockTokenStorage()
        val authManager = createAuthManager(tokenStorage)

        // Save both token and user data
        tokenStorage.saveTokens("access123", "refresh456")
        tokenStorage.saveUserData(json.encodeToString(testUser))
        testDispatcher.scheduler.advanceUntilIdle()

        authManager.authState.test {
            skipItems(1)
            val state = awaitItem()
            assertIs<AuthState.Authenticated>(state)
            assertEquals(testUser, state.user)
            assertEquals("user123", state.user.id)
            assertEquals("Test User", state.user.displayName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `authState emits Unauthenticated when token exists but userData missing`() = runTest {
        val tokenStorage = MockTokenStorage()
        val authManager = createAuthManager(tokenStorage)

        // Save only token, no user data
        tokenStorage.saveTokens("access123", "refresh456")
        testDispatcher.scheduler.advanceUntilIdle()

        authManager.authState.test {
            skipItems(1) // Skip Loading
            val state = awaitItem()
            assertIs<AuthState.Unauthenticated>(state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `authState emits Unauthenticated when userData exists but token missing`() = runTest {
        val tokenStorage = MockTokenStorage()
        val authManager = createAuthManager(tokenStorage)

        // Save only user data, no token
        tokenStorage.saveUserData(json.encodeToString(testUser))
        testDispatcher.scheduler.advanceUntilIdle()

        authManager.authState.test {
            skipItems(1) // Skip Loading
            val state = awaitItem()
            assertIs<AuthState.Unauthenticated>(state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `authState emits Unauthenticated when userData is invalid JSON`() = runTest {
        val tokenStorage = MockTokenStorage()
        val authManager = createAuthManager(tokenStorage)

        // Save token and invalid JSON
        tokenStorage.saveTokens("access123", "refresh456")
        tokenStorage.saveUserData("invalid json {{{")
        testDispatcher.scheduler.advanceUntilIdle()

        authManager.authState.test {
            skipItems(1) // Skip Loading
            val state = awaitItem()
            assertIs<AuthState.Unauthenticated>(state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `authState updates when tokens are cleared`() = runTest {
        val tokenStorage = MockTokenStorage()
        val authManager = createAuthManager(tokenStorage)

        // Set authenticated state
        tokenStorage.saveTokens("access123", "refresh456")
        tokenStorage.saveUserData(json.encodeToString(testUser))
        testDispatcher.scheduler.advanceUntilIdle()

        authManager.authState.test {
            skipItems(1) // Skip initial state
            assertIs<AuthState.Authenticated>(awaitItem()) // Should be authenticated

            // Clear tokens
            tokenStorage.clearTokens()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertIs<AuthState.Unauthenticated>(state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Initialize Tests

    @Test
    fun `initialize with valid tokens fetches user data`() = runTest {
        val tokenStorage = MockTokenStorage()
        tokenStorage.saveTokens("valid_token", "refresh_token")

        val fakeRepo = FakeAuthRepository(
            getCurrentUserResult = Result.Success(testUser)
        )
        val authManager = createAuthManager(tokenStorage, fakeRepo)

        authManager.initialize()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify getCurrentUser was called
        assertEquals(1, fakeRepo.getCurrentUserCallCount)
    }

    @Test
    fun `initialize with no tokens does nothing`() = runTest {
        val tokenStorage = MockTokenStorage()
        val fakeRepo = FakeAuthRepository()
        val authManager = createAuthManager(tokenStorage, fakeRepo)

        authManager.initialize()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify no repository calls were made
        assertEquals(0, fakeRepo.getCurrentUserCallCount)
        assertEquals(0, fakeRepo.refreshTokenCallCount)
    }

    @Test
    fun `initialize attempts refresh when getCurrentUser fails`() = runTest {
        val tokenStorage = MockTokenStorage()
        tokenStorage.saveTokens("invalid_token", "refresh_token")

        val fakeRepo = FakeAuthRepository(
            getCurrentUserResult = Result.Error(ApiError.AuthenticationError("Invalid token")),
            refreshTokenResult = Result.Success(AuthSession("new_access", "new_refresh", 3600, testUser))
        )
        val authManager = createAuthManager(tokenStorage, fakeRepo)

        authManager.initialize()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify refresh was attempted and getCurrentUser called again after refresh
        assertEquals(2, fakeRepo.getCurrentUserCallCount) // Called twice: initial + after refresh
        assertEquals(1, fakeRepo.refreshTokenCallCount)
    }

    @Test
    fun `initialize clears tokens when refresh fails`() = runTest {
        val tokenStorage = MockTokenStorage()
        tokenStorage.saveTokens("invalid_token", "invalid_refresh")

        val fakeRepo = FakeAuthRepository(
            getCurrentUserResult = Result.Error(ApiError.AuthenticationError("Invalid token")),
            refreshTokenResult = Result.Error(ApiError.AuthenticationError("Invalid refresh token"))
        )
        val authManager = createAuthManager(tokenStorage, fakeRepo)

        authManager.initialize()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify tokens were cleared
        assertNull(tokenStorage.getAccessToken())
        assertNull(tokenStorage.getRefreshToken())
        assertNull(tokenStorage.getUserData())
    }

    // Logout Tests

    @Test
    fun `logout calls repository logout`() = runTest {
        val fakeRepo = FakeAuthRepository(
            logoutResult = Result.Success(Unit)
        )
        val authManager = createAuthManager(authRepository = fakeRepo)

        val result = authManager.logout()

        assertIs<Result.Success<Unit>>(result)
        assertEquals(1, fakeRepo.logoutCallCount)
    }

    @Test
    fun `logout returns repository result`() = runTest {
        val error = ApiError.NetworkError("Connection failed")
        val fakeRepo = FakeAuthRepository(
            logoutResult = Result.Error(error)
        )
        val authManager = createAuthManager(authRepository = fakeRepo)

        val result = authManager.logout()

        assertIs<Result.Error>(result)
        assertEquals(error, result.error)
    }

    // GetCurrentUser Tests

    @Test
    fun `getCurrentUser returns user when authenticated`() = runTest {
        val tokenStorage = MockTokenStorage()
        tokenStorage.saveTokens("access123", "refresh456")
        tokenStorage.saveUserData(json.encodeToString(testUser))

        val authManager = createAuthManager(tokenStorage)
        testDispatcher.scheduler.advanceUntilIdle()

        // Wait for auth state to update
        authManager.authState.test {
            skipItems(1) // Skip Loading
            awaitItem() // Wait for Authenticated

            val user = authManager.getCurrentUser()
            assertEquals(testUser, user)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getCurrentUser returns null when loading`() = runTest {
        val authManager = createAuthManager()
        testDispatcher.scheduler.advanceUntilIdle()

        // Check immediately (should be Loading)
        val user = authManager.getCurrentUser()
        assertNull(user)
    }

    @Test
    fun `getCurrentUser returns null when unauthenticated`() = runTest {
        val tokenStorage = MockTokenStorage()
        val authManager = createAuthManager(tokenStorage)
        testDispatcher.scheduler.advanceUntilIdle()

        authManager.authState.test {
            skipItems(1) // Skip Loading
            awaitItem() // Wait for Unauthenticated

            val user = authManager.getCurrentUser()
            assertNull(user)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

/**
 * Fake AuthRepository for testing.
 * Allows controlling return values and tracking call counts.
 */
private class FakeAuthRepository(
    private val getCurrentUserResult: Result<User>? = null,
    private val refreshTokenResult: Result<AuthSession>? = null,
    private val logoutResult: Result<Unit>? = null
) : AuthRepository {

    var getCurrentUserCallCount = 0
        private set
    var refreshTokenCallCount = 0
        private set
    var logoutCallCount = 0
        private set

    override suspend fun loginWithLineToken(accessToken: String): Result<AuthSession> {
        error("Not implemented in fake")
    }

    override suspend fun getCurrentUser(): Result<User> {
        getCurrentUserCallCount++
        return getCurrentUserResult ?: Result.Error(ApiError.UnknownError)
    }

    override suspend fun refreshToken(): Result<AuthSession> {
        refreshTokenCallCount++
        return refreshTokenResult ?: Result.Error(ApiError.UnknownError)
    }

    override suspend fun logout(): Result<Unit> {
        logoutCallCount++
        return logoutResult ?: Result.Success(Unit)
    }

    override suspend fun getCurrentSession(): Result<AuthSession?> {
        return Result.Success(null)
    }
}
