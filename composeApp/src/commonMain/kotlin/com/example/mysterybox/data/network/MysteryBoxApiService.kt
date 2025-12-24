package com.example.mysterybox.data.network

import com.example.mysterybox.data.dto.*
import com.example.mysterybox.data.model.ApiError
import com.example.mysterybox.data.model.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class MysteryBoxApiService(
    private val httpClient: HttpClient
) {

    private suspend fun <T> safeApiCall(
        block: suspend () -> HttpResponse,
        parseResponse: suspend (HttpResponse) -> T
    ): Result<T> {
        return try {
            val response = block()
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    Result.Success(parseResponse(response))
                }
                HttpStatusCode.Unauthorized -> {
                    Result.Error(ApiError.AuthenticationError("Unauthorized"))
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

    // === Auth APIs ===

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
            parseResponse = { it.body() }
        )

    suspend fun logout(): Result<Unit> =
        safeApiCall(
            block = {
                httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.AUTH_LOGOUT}")
            },
            parseResponse = { Unit }
        )

    suspend fun getCurrentUser(): Result<CurrentUserResponseDto> =
        safeApiCall(
            block = {
                httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.AUTH_ME}")
            },
            parseResponse = { it.body() }
        )

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

    suspend fun getReservations(): Result<List<ReservationDto>> =
        safeApiCall(
            block = {
                httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.RESERVATIONS}")
            },
            parseResponse = { it.body() }
        )

    suspend fun createReservation(boxId: String): Result<ReservationCreatedDto> =
        safeApiCall(
            block = {
                httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.RESERVATIONS}") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateReservationRequestDto(boxId = boxId))
                }
            },
            parseResponse = { it.body() }
        )

    suspend fun cancelReservation(id: String): Result<Unit> =
        safeApiCall(
            block = {
                httpClient.delete("${ApiConfig.BASE_URL}${ApiConfig.cancelReservation(id)}")
            },
            parseResponse = { Unit }
        )

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

    suspend fun getMerchantBoxes(): Result<List<MysteryBoxDto>> =
        safeApiCall(
            block = {
                httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.MERCHANT_BOXES}")
            },
            parseResponse = { it.body() }
        )

    suspend fun createBox(request: CreateBoxRequestDto): Result<MysteryBoxDto> =
        safeApiCall(
            block = {
                httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.MERCHANT_BOXES}") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            },
            parseResponse = { it.body() }
        )

    suspend fun getMerchantDashboard(): Result<MerchantDashboardDto> =
        safeApiCall(
            block = {
                httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.MERCHANT_DASHBOARD}")
            },
            parseResponse = { it.body() }
        )

    suspend fun getMerchantOrders(status: String? = null, search: String? = null): Result<List<MerchantOrderDto>> =
        safeApiCall(
            block = {
                httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.MERCHANT_ORDERS}") {
                    status?.let { parameter("status", it) }
                    search?.let { parameter("search", it) }
                }
            },
            parseResponse = { it.body() }
        )

    suspend fun verifyOrder(orderId: String): Result<VerifyOrderResponseDto> =
        safeApiCall(
            block = {
                httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.merchantOrderVerify(orderId)}")
            },
            parseResponse = { it.body() }
        )

    suspend fun cancelMerchantOrder(orderId: String): Result<VerifyOrderResponseDto> =
        safeApiCall(
            block = {
                httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.merchantOrderCancel(orderId)}")
            },
            parseResponse = { it.body() }
        )
}
