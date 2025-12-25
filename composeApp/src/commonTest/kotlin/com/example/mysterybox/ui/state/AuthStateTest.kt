package com.example.mysterybox.ui.state

import com.example.mysterybox.data.model.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AuthStateTest {

    @Test
    fun `Loading is a data object singleton`() {
        val loading1 = AuthState.Loading
        val loading2 = AuthState.Loading

        // Data objects are singletons
        assertTrue(loading1 === loading2)
        assertEquals(loading1, loading2)
    }

    @Test
    fun `Unauthenticated is a data object singleton`() {
        val unauth1 = AuthState.Unauthenticated
        val unauth2 = AuthState.Unauthenticated

        // Data objects are singletons
        assertTrue(unauth1 === unauth2)
        assertEquals(unauth1, unauth2)
    }

    @Test
    fun `Authenticated contains User data`() {
        val user = User(
            id = "user123",
            displayName = "Test User",
            pictureUrl = "https://example.com/pic.jpg",
            createdAt = "2024-01-01"
        )

        val authState = AuthState.Authenticated(user)

        assertEquals(user, authState.user)
        assertEquals("user123", authState.user.id)
        assertEquals("Test User", authState.user.displayName)
    }

    @Test
    fun `Authenticated with different users are not equal`() {
        val user1 = User(
            id = "user1",
            displayName = "User One",
            pictureUrl = null
        )

        val user2 = User(
            id = "user2",
            displayName = "User Two",
            pictureUrl = null
        )

        val auth1 = AuthState.Authenticated(user1)
        val auth2 = AuthState.Authenticated(user2)

        assertNotEquals(auth1, auth2)
    }

    @Test
    fun `Authenticated with same user are equal`() {
        val user = User(
            id = "user123",
            displayName = "Test User",
            pictureUrl = null
        )

        val auth1 = AuthState.Authenticated(user)
        val auth2 = AuthState.Authenticated(user)

        assertEquals(auth1, auth2)
    }

    @Test
    fun `Pattern matching on Loading state`() {
        val state: AuthState = AuthState.Loading

        when (state) {
            is AuthState.Loading -> {
                // Success
                assertTrue(true)
            }
            is AuthState.Authenticated -> throw AssertionError("Should be Loading")
            is AuthState.Unauthenticated -> throw AssertionError("Should be Loading")
        }
    }

    @Test
    fun `Pattern matching on Authenticated state`() {
        val user = User(
            id = "user123",
            displayName = "Test User",
            pictureUrl = null
        )
        val state: AuthState = AuthState.Authenticated(user)

        when (state) {
            is AuthState.Loading -> throw AssertionError("Should be Authenticated")
            is AuthState.Authenticated -> {
                assertEquals("user123", state.user.id)
                assertTrue(true)
            }
            is AuthState.Unauthenticated -> throw AssertionError("Should be Authenticated")
        }
    }

    @Test
    fun `Pattern matching on Unauthenticated state`() {
        val state: AuthState = AuthState.Unauthenticated

        when (state) {
            is AuthState.Loading -> throw AssertionError("Should be Unauthenticated")
            is AuthState.Authenticated -> throw AssertionError("Should be Unauthenticated")
            is AuthState.Unauthenticated -> {
                // Success
                assertTrue(true)
            }
        }
    }

    @Test
    fun `AuthState subclasses are correctly typed`() {
        val loading: AuthState = AuthState.Loading
        val authenticated: AuthState = AuthState.Authenticated(
            User(id = "id", displayName = "name", pictureUrl = null)
        )
        val unauthenticated: AuthState = AuthState.Unauthenticated

        assertIs<AuthState.Loading>(loading)
        assertIs<AuthState.Authenticated>(authenticated)
        assertIs<AuthState.Unauthenticated>(unauthenticated)
    }

    @Test
    fun `Different AuthState types are not equal`() {
        val loading = AuthState.Loading
        val authenticated = AuthState.Authenticated(
            User(id = "id", displayName = "name", pictureUrl = null)
        )
        val unauthenticated = AuthState.Unauthenticated

        assertNotEquals<AuthState>(loading, authenticated)
        assertNotEquals<AuthState>(loading, unauthenticated)
        assertNotEquals<AuthState>(authenticated, unauthenticated)
    }
}
