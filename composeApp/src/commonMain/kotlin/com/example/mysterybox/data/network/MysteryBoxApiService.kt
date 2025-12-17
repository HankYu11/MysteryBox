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
    private suspend inline fun <reified T> safeApiCall(
        crossinline block: suspend () -> HttpResponse
    ): Result<T> {
        return try {
            val response = block()
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    Result.Success(response.body<T>())
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

    suspend fun loginWithLine(code: String, state: String?, redirectUri: String): Result<AuthResponseDto> =
        safeApiCall {
            httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.AUTH_LINE}") {
                contentType(ContentType.Application.Json)
                setBody(LineAuthRequestDto(code = code, state = state, redirectUri = redirectUri))
            }
        }

    suspend fun refreshToken(refreshToken: String): Result<RefreshTokenResponseDto> =
        safeApiCall {
            httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.AUTH_REFRESH}") {
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequestDto(refreshToken = refreshToken))
            }
        }

    suspend fun logout(): Result<Unit> {
        val token = tokenManager.getAccessToken()
        return if (token != null) {
            safeApiCall {
                httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.AUTH_LOGOUT}") {
                    bearerAuth(token)
                }
            }
        } else {
            Result.Success(Unit)
        }
    }

    // === Box APIs ===

    suspend fun getBoxes(status: String? = null): Result<List<MysteryBoxDto>> =
        safeApiCall {
            httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.BOXES}") {
                status?.let { parameter("status", it) }
            }
        }

    suspend fun getBoxById(id: String): Result<MysteryBoxDto> =
        safeApiCall {
            httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.boxDetail(id)}")
        }

    // === Reservation APIs (Authenticated) ===

    suspend fun getReservations(): Result<List<ReservationDto>> {
        val token = tokenManager.getAccessToken()
            ?: return Result.Error(ApiError.AuthenticationError("Not authenticated"))

        return safeApiCall {
            httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.RESERVATIONS}") {
                bearerAuth(token)
            }
        }
    }

    suspend fun createReservation(boxId: String): Result<ReservationDto> {
        val token = tokenManager.getAccessToken()
            ?: return Result.Error(ApiError.AuthenticationError("Not authenticated"))

        return safeApiCall {
            httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.RESERVATIONS}") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(CreateReservationRequestDto(boxId = boxId))
            }
        }
    }

    suspend fun cancelReservation(id: String): Result<Unit> {
        val token = tokenManager.getAccessToken()
            ?: return Result.Error(ApiError.AuthenticationError("Not authenticated"))

        return safeApiCall {
            httpClient.delete("${ApiConfig.BASE_URL}${ApiConfig.cancelReservation(id)}") {
                bearerAuth(token)
            }
        }
    }

    // === Merchant APIs ===

    suspend fun merchantLogin(email: String, password: String): Result<MerchantResponseDto> =
        safeApiCall {
            httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.MERCHANT_LOGIN}") {
                contentType(ContentType.Application.Json)
                setBody(MerchantLoginRequestDto(email = email, password = password))
            }
        }

    suspend fun getMerchantBoxes(): Result<List<MysteryBoxDto>> {
        val token = tokenManager.getMerchantToken()
            ?: return Result.Error(ApiError.AuthenticationError("Merchant not authenticated"))

        return safeApiCall {
            httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.MERCHANT_BOXES}") {
                bearerAuth(token)
            }
        }
    }

    suspend fun createBox(request: CreateBoxRequestDto): Result<MysteryBoxDto> {
        val token = tokenManager.getMerchantToken()
            ?: return Result.Error(ApiError.AuthenticationError("Merchant not authenticated"))

        return safeApiCall {
            httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.MERCHANT_BOXES}") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }
}
