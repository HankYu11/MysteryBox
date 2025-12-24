package com.example.mysterybox.data.network

import com.example.mysterybox.data.dto.*
import com.example.mysterybox.data.model.ApiError
import com.example.mysterybox.data.model.Result
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class MysteryBoxApiService(
    private val httpClient: HttpClient,
    private val tokenManager: TokenManager
) {
    private var isRefreshing = false

    private suspend fun <T> safeApiCall(
        block: suspend () -> HttpResponse,
        parseResponse: suspend (HttpResponse) -> T,
        retryOnUnauthorized: Boolean = true
    ): Result<T> {
        return try {
            val response = block()
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    Result.Success(parseResponse(response))
                }
                HttpStatusCode.Unauthorized -> {
                    // Try to refresh token and retry once
                    if (retryOnUnauthorized && !isRefreshing) {
                        when (val refreshResult = attemptTokenRefresh()) {
                            is Result.Success -> {
                                // Retry the original request with new token
                                safeApiCall(block, parseResponse, retryOnUnauthorized = false)
                            }
                            is Result.Error -> {
                                // Refresh failed, return unauthorized
                                Result.Error(ApiError.AuthenticationError("Session expired"))
                            }
                        }
                    } else {
                        Result.Error(ApiError.AuthenticationError("Unauthorized"))
                    }
                }
                HttpStatusCode.NotFound -> {
                    Result.Error(ApiError.NotFoundError("Resource not found"))
                }
                HttpStatusCode.BadRequest -> {
                    val error = try {
                        response.body<ErrorResponseDto>()
                    } catch (e: Exception) {
                        ErrorResponseDto(error = "Bad request")
                    }
                    Result.Error(ApiError.ValidationError(error.error))
                }
                else -> {
                    Result.Error(ApiError.HttpError(response.status.value, response.status.description))
                }
            }
        } catch (e: Exception) {
            Result.Error(ApiError.NetworkError(e.message ?: "Network error", e))
        }
    }

    private suspend fun attemptTokenRefresh(): Result<Unit> {
        if (isRefreshing) {
            return Result.Error(ApiError.AuthenticationError("Already refreshing"))
        }

        isRefreshing = true
        return try {
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken != null) {
                when (val result = refreshToken(refreshToken)) {
                    is Result.Success -> {
                        val response = result.data
                        if (response.success && response.accessToken != null && response.refreshToken != null) {
                            val currentUser = tokenManager.getCurrentUser()
                            if (currentUser != null) {
                                tokenManager.saveUserSession(
                                    response.accessToken,
                                    response.refreshToken,
                                    currentUser
                                )
                                Result.Success(Unit)
                            } else {
                                // No user data, clear tokens
                                tokenManager.clearUserTokens()
                                Result.Error(ApiError.AuthenticationError("No user data"))
                            }
                        } else {
                            // Refresh failed, clear tokens
                            tokenManager.clearUserTokens()
                            Result.Error(ApiError.AuthenticationError("Refresh failed"))
                        }
                    }
                    is Result.Error -> {
                        // Refresh request failed, clear tokens
                        tokenManager.clearUserTokens()
                        result
                    }
                }
            } else {
                // No refresh token, clear any remaining tokens
                tokenManager.clearUserTokens()
                Result.Error(ApiError.AuthenticationError("No refresh token"))
            }
        } finally {
            isRefreshing = false
        }
    }

    // === Auth APIs ===

    /**
     * Verify LINE access token and create session
     * Client sends LINE access token, backend verifies with LINE API and creates session
     */
    suspend fun verifyLineAccessToken(lineAccessToken: String): Result<AuthResponseDto> =
        safeApiCall(
            block = {
                httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.AUTH_LINE_VERIFY}") {
                    contentType(ContentType.Application.Json)
                    setBody(LineTokenVerifyRequest(lineAccessToken = lineAccessToken))
                }
            },
            parseResponse = { it.body() }
        )

    suspend fun refreshToken(refreshToken: String): Result<RefreshTokenResponseDto> =
        safeApiCall(
            block = {
                httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.AUTH_REFRESH}") {
                    contentType(ContentType.Application.Json)
                    setBody(RefreshTokenRequestDto(refreshToken = refreshToken))
                }
            },
            parseResponse = { it.body() },
            retryOnUnauthorized = false
        )

    suspend fun logout(): Result<Unit> {
        val token = tokenManager.getAccessToken()
        return if (token != null) {
            safeApiCall(
                block = {
                    httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.AUTH_LOGOUT}") {
                        bearerAuth(token)
                    }
                },
                parseResponse = { Unit }
            )
        } else {
            Result.Success(Unit)
        }
    }

    suspend fun getCurrentUser(): Result<CurrentUserResponseDto> {
        val token = tokenManager.getAccessToken()
            ?: return Result.Error(ApiError.AuthenticationError("Not authenticated"))

        return safeApiCall(
            block = {
                httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.AUTH_ME}") {
                    bearerAuth(token)
                }
            },
            parseResponse = { it.body() }
        )
    }

    // === Box APIs ===

    suspend fun getBoxes(status: String? = null): Result<List<MysteryBoxDto>> =
        safeApiCall(
            block = {
                httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.BOXES}") {
                    status?.let { parameter("status", it) }
                }
            },
            parseResponse = { it.body() }
        )

    suspend fun getBoxById(id: String): Result<MysteryBoxDto> =
        safeApiCall(
            block = {
                httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.boxDetail(id)}")
            },
            parseResponse = { it.body() }
        )

    // === Reservation APIs (Authenticated) ===

    suspend fun getReservations(): Result<List<ReservationDto>> {
        val token = tokenManager.getAccessToken()
            ?: return Result.Error(ApiError.AuthenticationError("Not authenticated"))

        return safeApiCall(
            block = {
                httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.RESERVATIONS}") {
                    bearerAuth(token)
                }
            },
            parseResponse = { it.body() }
        )
    }

    suspend fun createReservation(boxId: String): Result<ReservationCreatedDto> {
        val token = tokenManager.getAccessToken()
            ?: return Result.Error(ApiError.AuthenticationError("Not authenticated"))

        return safeApiCall(
            block = {
                httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.RESERVATIONS}") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(CreateReservationRequestDto(boxId = boxId))
                }
            },
            parseResponse = { it.body() }
        )
    }

    suspend fun cancelReservation(id: String): Result<Unit> {
        val token = tokenManager.getAccessToken()
            ?: return Result.Error(ApiError.AuthenticationError("Not authenticated"))

        return safeApiCall(
            block = {
                httpClient.delete("${ApiConfig.BASE_URL}${ApiConfig.cancelReservation(id)}") {
                    bearerAuth(token)
                }
            },
            parseResponse = { Unit }
        )
    }

    // === Merchant APIs ===

    suspend fun merchantLogin(email: String, password: String): Result<MerchantResponseDto> =
        safeApiCall(
            block = {
                httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.MERCHANT_LOGIN}") {
                    contentType(ContentType.Application.Json)
                    setBody(MerchantLoginRequestDto(email = email, password = password))
                }
            },
            parseResponse = { it.body() }
        )

    suspend fun getMerchantBoxes(): Result<List<MysteryBoxDto>> {
        val token = tokenManager.getMerchantToken()
            ?: return Result.Error(ApiError.AuthenticationError("Merchant not authenticated"))

        return safeApiCall(
            block = {
                httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.MERCHANT_BOXES}") {
                    bearerAuth(token)
                }
            },
            parseResponse = { it.body() }
        )
    }

    suspend fun createBox(request: CreateBoxRequestDto): Result<MysteryBoxDto> {
        val token = tokenManager.getMerchantToken()
            ?: return Result.Error(ApiError.AuthenticationError("Merchant not authenticated"))

        return safeApiCall(
            block = {
                httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.MERCHANT_BOXES}") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            },
            parseResponse = { it.body() }
        )
    }

    suspend fun getMerchantDashboard(): Result<MerchantDashboardDto> {
        val token = tokenManager.getMerchantToken()
            ?: return Result.Error(ApiError.AuthenticationError("Merchant not authenticated"))

        return safeApiCall(
            block = {
                httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.MERCHANT_DASHBOARD}") {
                    bearerAuth(token)
                }
            },
            parseResponse = { it.body() }
        )
    }

    suspend fun getMerchantOrders(status: String? = null, search: String? = null): Result<List<MerchantOrderDto>> {
        val token = tokenManager.getMerchantToken()
            ?: return Result.Error(ApiError.AuthenticationError("Merchant not authenticated"))

        return safeApiCall(
            block = {
                httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.MERCHANT_ORDERS}") {
                    bearerAuth(token)
                    status?.let { parameter("status", it) }
                    search?.let { parameter("search", it) }
                }
            },
            parseResponse = { it.body() }
        )
    }

    suspend fun verifyOrder(orderId: String): Result<VerifyOrderResponseDto> {
        val token = tokenManager.getMerchantToken()
            ?: return Result.Error(ApiError.AuthenticationError("Merchant not authenticated"))

        return safeApiCall(
            block = {
                httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.merchantOrderVerify(orderId)}") {
                    bearerAuth(token)
                }
            },
            parseResponse = { it.body() }
        )
    }

    suspend fun cancelMerchantOrder(orderId: String): Result<VerifyOrderResponseDto> {
        val token = tokenManager.getMerchantToken()
            ?: return Result.Error(ApiError.AuthenticationError("Merchant not authenticated"))

        return safeApiCall(
            block = {
                httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.merchantOrderCancel(orderId)}") {
                    bearerAuth(token)
                }
            },
            parseResponse = { it.body() }
        )
    }
}
