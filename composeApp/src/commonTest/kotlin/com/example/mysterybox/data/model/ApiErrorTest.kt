package com.example.mysterybox.data.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ApiErrorTest {

    @Test
    fun `HttpError toMessage includes code and message`() {
        val error = ApiError.HttpError(404, "Not Found")
        assertEquals("HTTP 404: Not Found", error.toMessage())
    }

    @Test
    fun `HttpError toMessage with different codes`() {
        assertEquals("HTTP 500: Internal Server Error", ApiError.HttpError(500, "Internal Server Error").toMessage())
        assertEquals("HTTP 401: Unauthorized", ApiError.HttpError(401, "Unauthorized").toMessage())
        assertEquals("HTTP 400: Bad Request", ApiError.HttpError(400, "Bad Request").toMessage())
    }

    @Test
    fun `NetworkError toMessage returns message`() {
        val error = ApiError.NetworkError("Connection timeout")
        assertEquals("Connection timeout", error.toMessage())
    }

    @Test
    fun `NetworkError with cause still returns message`() {
        val cause = RuntimeException("Socket closed")
        val error = ApiError.NetworkError("Network unavailable", cause)
        assertEquals("Network unavailable", error.toMessage())
    }

    @Test
    fun `AuthenticationError toMessage returns message`() {
        val error = ApiError.AuthenticationError("Token expired")
        assertEquals("Token expired", error.toMessage())
    }

    @Test
    fun `NotFoundError toMessage returns message`() {
        val error = ApiError.NotFoundError("Resource not found")
        assertEquals("Resource not found", error.toMessage())
    }

    @Test
    fun `ValidationError toMessage returns message`() {
        val error = ApiError.ValidationError("Invalid email format")
        assertEquals("Invalid email format", error.toMessage())
    }

    @Test
    fun `UnknownError toMessage returns generic message`() {
        val error = ApiError.UnknownError
        assertEquals("Unknown error occurred", error.toMessage())
    }

    @Test
    fun `different error types are distinct`() {
        val httpError = ApiError.HttpError(500, "Server Error")
        val networkError = ApiError.NetworkError("Server Error")
        val authError = ApiError.AuthenticationError("Server Error")

        // Same message but different error types
        assertEquals("HTTP 500: Server Error", httpError.toMessage())
        assertEquals("Server Error", networkError.toMessage())
        assertEquals("Server Error", authError.toMessage())
    }
}
