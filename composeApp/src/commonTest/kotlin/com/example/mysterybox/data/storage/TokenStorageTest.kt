package com.example.mysterybox.data.storage

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Contract tests for TokenStorage interface.
 * These tests verify the expected behavior of any TokenStorage implementation.
 */
class TokenStorageTest {

    /**
     * Creates a fresh TokenStorage instance for each test.
     * Implementations should override this to provide their specific storage.
     */
    private fun createTokenStorage(): TokenStorage {
        // Using MockTokenStorage for contract testing
        return MockTokenStorage()
    }

    // User Token Tests

    @Test
    fun `saveTokens stores both access and refresh tokens`() = runTest {
        val storage = createTokenStorage()

        storage.saveTokens("access123", "refresh456")

        assertEquals("access123", storage.getAccessToken())
        assertEquals("refresh456", storage.getRefreshToken())
    }

    @Test
    fun `getAccessToken returns null when no token saved`() = runTest {
        val storage = createTokenStorage()

        val token = storage.getAccessToken()

        assertNull(token)
    }

    @Test
    fun `getRefreshToken returns null when no token saved`() = runTest {
        val storage = createTokenStorage()

        val token = storage.getRefreshToken()

        assertNull(token)
    }

    @Test
    fun `saveTokens overwrites previous tokens`() = runTest {
        val storage = createTokenStorage()

        storage.saveTokens("old_access", "old_refresh")
        storage.saveTokens("new_access", "new_refresh")

        assertEquals("new_access", storage.getAccessToken())
        assertEquals("new_refresh", storage.getRefreshToken())
    }

    @Test
    fun `clearTokens removes all user tokens and data`() = runTest {
        val storage = createTokenStorage()

        storage.saveTokens("access123", "refresh456")
        storage.saveUserData("{\"id\":\"user1\"}")
        storage.clearTokens()

        assertNull(storage.getAccessToken())
        assertNull(storage.getRefreshToken())
        assertNull(storage.getUserData())
    }

    // User Data Tests

    @Test
    fun `saveUserData stores user data`() = runTest {
        val storage = createTokenStorage()
        val userData = "{\"id\":\"user123\",\"name\":\"Test User\"}"

        storage.saveUserData(userData)

        assertEquals(userData, storage.getUserData())
    }

    @Test
    fun `getUserData returns null when no data saved`() = runTest {
        val storage = createTokenStorage()

        val data = storage.getUserData()

        assertNull(data)
    }

    @Test
    fun `saveUserData overwrites previous data`() = runTest {
        val storage = createTokenStorage()

        storage.saveUserData("{\"id\":\"old\"}")
        storage.saveUserData("{\"id\":\"new\"}")

        assertEquals("{\"id\":\"new\"}", storage.getUserData())
    }

    // Flow Tests

    @Test
    fun `accessTokenFlow emits null initially`() = runTest {
        val storage = createTokenStorage()

        val token = storage.accessTokenFlow.first()

        assertNull(token)
    }

    @Test
    fun `accessTokenFlow emits saved token`() = runTest {
        val storage = createTokenStorage()

        storage.saveTokens("access123", "refresh456")
        val token = storage.accessTokenFlow.first()

        assertEquals("access123", token)
    }

    @Test
    fun `refreshTokenFlow emits saved token`() = runTest {
        val storage = createTokenStorage()

        storage.saveTokens("access123", "refresh456")
        val token = storage.refreshTokenFlow.first()

        assertEquals("refresh456", token)
    }

    @Test
    fun `userDataFlow emits saved data`() = runTest {
        val storage = createTokenStorage()
        val userData = "{\"id\":\"user123\"}"

        storage.saveUserData(userData)
        val data = storage.userDataFlow.first()

        assertEquals(userData, data)
    }

    // Merchant Token Tests

    @Test
    fun `saveMerchantToken stores merchant token`() = runTest {
        val storage = createTokenStorage()

        storage.saveMerchantToken("merchant_token_123")

        assertEquals("merchant_token_123", storage.getMerchantToken())
    }

    @Test
    fun `getMerchantToken returns null when no token saved`() = runTest {
        val storage = createTokenStorage()

        val token = storage.getMerchantToken()

        assertNull(token)
    }

    @Test
    fun `saveMerchantToken overwrites previous token`() = runTest {
        val storage = createTokenStorage()

        storage.saveMerchantToken("old_token")
        storage.saveMerchantToken("new_token")

        assertEquals("new_token", storage.getMerchantToken())
    }

    // Merchant Data Tests

    @Test
    fun `saveMerchantData stores merchant data`() = runTest {
        val storage = createTokenStorage()
        val merchantData = "{\"id\":\"merchant123\",\"name\":\"Test Shop\"}"

        storage.saveMerchantData(merchantData)

        assertEquals(merchantData, storage.getMerchantData())
    }

    @Test
    fun `getMerchantData returns null when no data saved`() = runTest {
        val storage = createTokenStorage()

        val data = storage.getMerchantData()

        assertNull(data)
    }

    @Test
    fun `saveMerchantData overwrites previous data`() = runTest {
        val storage = createTokenStorage()

        storage.saveMerchantData("{\"id\":\"old\"}")
        storage.saveMerchantData("{\"id\":\"new\"}")

        assertEquals("{\"id\":\"new\"}", storage.getMerchantData())
    }

    @Test
    fun `clearMerchantToken removes merchant token and data`() = runTest {
        val storage = createTokenStorage()

        storage.saveMerchantToken("merchant_token")
        storage.saveMerchantData("{\"id\":\"merchant1\"}")
        storage.clearMerchantToken()

        assertNull(storage.getMerchantToken())
        assertNull(storage.getMerchantData())
    }

    // Isolation Tests (Critical!)

    @Test
    fun `clearTokens does not affect merchant tokens`() = runTest {
        val storage = createTokenStorage()

        // Save both user and merchant data
        storage.saveTokens("user_access", "user_refresh")
        storage.saveUserData("{\"id\":\"user1\"}")
        storage.saveMerchantToken("merchant_token")
        storage.saveMerchantData("{\"id\":\"merchant1\"}")

        // Clear only user tokens
        storage.clearTokens()

        // User tokens should be cleared
        assertNull(storage.getAccessToken())
        assertNull(storage.getRefreshToken())
        assertNull(storage.getUserData())

        // Merchant tokens should still exist
        assertNotNull(storage.getMerchantToken())
        assertNotNull(storage.getMerchantData())
        assertEquals("merchant_token", storage.getMerchantToken())
        assertEquals("{\"id\":\"merchant1\"}", storage.getMerchantData())
    }

    @Test
    fun `clearMerchantToken does not affect user tokens`() = runTest {
        val storage = createTokenStorage()

        // Save both user and merchant data
        storage.saveTokens("user_access", "user_refresh")
        storage.saveUserData("{\"id\":\"user1\"}")
        storage.saveMerchantToken("merchant_token")
        storage.saveMerchantData("{\"id\":\"merchant1\"}")

        // Clear only merchant tokens
        storage.clearMerchantToken()

        // Merchant tokens should be cleared
        assertNull(storage.getMerchantToken())
        assertNull(storage.getMerchantData())

        // User tokens should still exist
        assertNotNull(storage.getAccessToken())
        assertNotNull(storage.getRefreshToken())
        assertNotNull(storage.getUserData())
        assertEquals("user_access", storage.getAccessToken())
        assertEquals("user_refresh", storage.getRefreshToken())
        assertEquals("{\"id\":\"user1\"}", storage.getUserData())
    }

    @Test
    fun `concurrent user and merchant sessions work independently`() = runTest {
        val storage = createTokenStorage()

        // Simulate concurrent authentication
        storage.saveTokens("user_access", "user_refresh")
        storage.saveUserData("{\"id\":\"user123\",\"name\":\"John\"}")
        storage.saveMerchantToken("merchant_jwt_token")
        storage.saveMerchantData("{\"id\":\"merchant456\",\"shop\":\"My Shop\"}")

        // Both sessions should coexist
        assertEquals("user_access", storage.getAccessToken())
        assertEquals("user_refresh", storage.getRefreshToken())
        assertEquals("{\"id\":\"user123\",\"name\":\"John\"}", storage.getUserData())
        assertEquals("merchant_jwt_token", storage.getMerchantToken())
        assertEquals("{\"id\":\"merchant456\",\"shop\":\"My Shop\"}", storage.getMerchantData())
    }

    @Test
    fun `empty string is stored and retrieved correctly`() = runTest {
        val storage = createTokenStorage()

        storage.saveUserData("")
        storage.saveMerchantData("")

        assertEquals("", storage.getUserData())
        assertEquals("", storage.getMerchantData())
    }

    @Test
    fun `large JSON data is stored correctly`() = runTest {
        val storage = createTokenStorage()
        val largeJson = "{\"id\":\"user1\"," +
                "\"data\":\"" + "x".repeat(1000) + "\"," +
                "\"metadata\":{\"nested\":true}}"

        storage.saveUserData(largeJson)

        assertEquals(largeJson, storage.getUserData())
    }
}
