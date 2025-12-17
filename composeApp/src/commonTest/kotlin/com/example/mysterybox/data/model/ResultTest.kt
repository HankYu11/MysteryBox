package com.example.mysterybox.data.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ResultTest {

    @Test
    fun `map transforms Success value`() {
        val result: Result<Int> = Result.Success(5)
        val mapped = result.map { it * 2 }

        assertTrue(mapped is Result.Success)
        assertEquals(10, (mapped as Result.Success).data)
    }

    @Test
    fun `map preserves Error without calling transform`() {
        val error = ApiError.NetworkError("Connection failed")
        val result: Result<Int> = Result.Error(error)
        var transformCalled = false

        val mapped = result.map {
            transformCalled = true
            it * 2
        }

        assertTrue(mapped is Result.Error)
        assertEquals(error, (mapped as Result.Error).error)
        assertFalse(transformCalled)
    }

    @Test
    fun `onSuccess executes action for Success`() {
        val result: Result<String> = Result.Success("test")
        var captured: String? = null

        result.onSuccess { captured = it }

        assertEquals("test", captured)
    }

    @Test
    fun `onSuccess does not execute action for Error`() {
        val result: Result<String> = Result.Error(ApiError.UnknownError)
        var actionCalled = false

        result.onSuccess { actionCalled = true }

        assertFalse(actionCalled)
    }

    @Test
    fun `onError executes action for Error`() {
        val error = ApiError.AuthenticationError("Unauthorized")
        val result: Result<String> = Result.Error(error)
        var captured: ApiError? = null

        result.onError { captured = it }

        assertEquals(error, captured)
    }

    @Test
    fun `onError does not execute action for Success`() {
        val result: Result<String> = Result.Success("test")
        var actionCalled = false

        result.onError { actionCalled = true }

        assertFalse(actionCalled)
    }

    @Test
    fun `getOrNull returns data for Success`() {
        val result: Result<Int> = Result.Success(42)
        assertEquals(42, result.getOrNull())
    }

    @Test
    fun `getOrNull returns null for Error`() {
        val result: Result<Int> = Result.Error(ApiError.UnknownError)
        assertNull(result.getOrNull())
    }

    @Test
    fun `chaining onSuccess and onError works correctly for Success`() {
        val result: Result<Int> = Result.Success(10)
        var successValue: Int? = null
        var errorValue: ApiError? = null

        result
            .onSuccess { successValue = it }
            .onError { errorValue = it }

        assertEquals(10, successValue)
        assertNull(errorValue)
    }

    @Test
    fun `chaining onSuccess and onError works correctly for Error`() {
        val error = ApiError.NetworkError("Failed")
        val result: Result<Int> = Result.Error(error)
        var successValue: Int? = null
        var errorValue: ApiError? = null

        result
            .onSuccess { successValue = it }
            .onError { errorValue = it }

        assertNull(successValue)
        assertEquals(error, errorValue)
    }

    @Test
    fun `map can change result type`() {
        val result: Result<Int> = Result.Success(42)
        val mapped: Result<String> = result.map { "Value: $it" }

        assertTrue(mapped is Result.Success)
        assertEquals("Value: 42", (mapped as Result.Success).data)
    }

    @Test
    fun `error companion function creates Error with HttpError`() {
        val result = Result.error("Something went wrong")

        assertTrue(result is Result.Error)
        val error = (result as Result.Error).error
        assertTrue(error is ApiError.HttpError)
        assertEquals(0, (error as ApiError.HttpError).code)
        assertEquals("Something went wrong", error.message)
    }
}
