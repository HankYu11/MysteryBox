package com.example.mysterybox.data.network

import com.example.mysterybox.data.storage.MockTokenStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TokenManagerTest {

    @Test
    fun `initial state has no tokens`() = runTest {
        val mockStorage = MockTokenStorage()
        val tokenManager = TokenManager(mockStorage)

        assertNull(tokenManager.getAccessToken())
        assertNull(tokenManager.getRefreshToken())
        assertNull(tokenManager.getMerchantToken())
    }

    @Test
    fun `saveUserTokens stores access and refresh tokens`() = runTest {
        val mockStorage = MockTokenStorage()
        val tokenManager = TokenManager(mockStorage)

        tokenManager.saveUserTokens("access-123", "refresh-456")

        assertEquals("access-123", tokenManager.getAccessToken())
        assertEquals("refresh-456", tokenManager.getRefreshToken())
    }

    @Test
    fun `saveMerchantToken stores merchant token`() = runTest {
        val mockStorage = MockTokenStorage()
        val tokenManager = TokenManager(mockStorage)

        tokenManager.saveMerchantToken("merchant-789")

        assertEquals("merchant-789", tokenManager.getMerchantToken())
    }

    @Test
    fun `clearUserTokens removes user tokens but keeps merchant token`() = runTest {
        val mockStorage = MockTokenStorage()
        val tokenManager = TokenManager(mockStorage)
        tokenManager.saveUserTokens("access", "refresh")
        tokenManager.saveMerchantToken("merchant")

        tokenManager.clearUserTokens()

        assertNull(tokenManager.getAccessToken())
        assertNull(tokenManager.getRefreshToken())
        assertEquals("merchant", tokenManager.getMerchantToken())
    }

    @Test
    fun `clearMerchantToken removes merchant token but keeps user tokens`() = runTest {
        val mockStorage = MockTokenStorage()
        val tokenManager = TokenManager(mockStorage)
        tokenManager.saveUserTokens("access", "refresh")
        tokenManager.saveMerchantToken("merchant")

        tokenManager.clearMerchantToken()

        assertEquals("access", tokenManager.getAccessToken())
        assertEquals("refresh", tokenManager.getRefreshToken())
        assertNull(tokenManager.getMerchantToken())
    }

    @Test
    fun `clearAllTokens removes all tokens`() = runTest {
        val mockStorage = MockTokenStorage()
        val tokenManager = TokenManager(mockStorage)
        tokenManager.saveUserTokens("access", "refresh")
        tokenManager.saveMerchantToken("merchant")

        tokenManager.clearAllTokens()

        assertNull(tokenManager.getAccessToken())
        assertNull(tokenManager.getRefreshToken())
        assertNull(tokenManager.getMerchantToken())
    }

    @Test
    fun `isUserAuthenticated returns false when no access token`() = runTest {
        val mockStorage = MockTokenStorage()
        val tokenManager = TokenManager(mockStorage)
        assertFalse(tokenManager.isUserAuthenticated())
    }

    @Test
    fun `isUserAuthenticated returns true when access token exists`() = runTest {
        val mockStorage = MockTokenStorage()
        val tokenManager = TokenManager(mockStorage)
        tokenManager.saveUserTokens("access", "refresh")
        assertTrue(tokenManager.isUserAuthenticated())
    }

    @Test
    fun `isMerchantAuthenticated returns false when no merchant token`() = runTest {
        val mockStorage = MockTokenStorage()
        val tokenManager = TokenManager(mockStorage)
        assertFalse(tokenManager.isMerchantAuthenticated())
    }

    @Test
    fun `isMerchantAuthenticated returns true when merchant token exists`() = runTest {
        val mockStorage = MockTokenStorage()
        val tokenManager = TokenManager(mockStorage)
        tokenManager.saveMerchantToken("merchant")
        assertTrue(tokenManager.isMerchantAuthenticated())
    }

    @Test
    fun `user and merchant authentication are independent`() = runTest {
        val mockStorage = MockTokenStorage()
        val tokenManager = TokenManager(mockStorage)

        tokenManager.saveUserTokens("access", "refresh")
        assertTrue(tokenManager.isUserAuthenticated())
        assertFalse(tokenManager.isMerchantAuthenticated())

        tokenManager.saveMerchantToken("merchant")
        assertTrue(tokenManager.isUserAuthenticated())
        assertTrue(tokenManager.isMerchantAuthenticated())

        tokenManager.clearUserTokens()
        assertFalse(tokenManager.isUserAuthenticated())
        assertTrue(tokenManager.isMerchantAuthenticated())
    }

    @Test
    fun `overwriting tokens works correctly`() = runTest {
        val mockStorage = MockTokenStorage()
        val tokenManager = TokenManager(mockStorage)

        tokenManager.saveUserTokens("access1", "refresh1")
        assertEquals("access1", tokenManager.getAccessToken())

        tokenManager.saveUserTokens("access2", "refresh2")
        assertEquals("access2", tokenManager.getAccessToken())
        assertEquals("refresh2", tokenManager.getRefreshToken())
    }
}
