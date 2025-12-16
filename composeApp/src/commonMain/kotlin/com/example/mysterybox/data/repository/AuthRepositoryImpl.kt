package com.example.mysterybox.data.repository

import com.example.mysterybox.data.model.AuthSession
import com.example.mysterybox.data.model.AuthState
import com.example.mysterybox.data.model.LineAuthRequest
import com.example.mysterybox.data.model.LineAuthResponse
import com.example.mysterybox.data.model.User
import com.example.mysterybox.data.network.ApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepositoryImpl(
    private val httpClient: HttpClient
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var currentSession: AuthSession? = null
    private var storedState: String? = null

    override suspend fun exchangeLineCode(code: String, state: String): Result<AuthSession> {
        _authState.value = AuthState.Loading

        return try {
            val response: LineAuthResponse = httpClient.post("${ApiConfig.BASE_URL}/auth/line/callback") {
                contentType(ContentType.Application.Json)
                setBody(
                    LineAuthRequest(
                        code = code,
                        state = state,
                        redirectUri = ApiConfig.REDIRECT_URI
                    )
                )
            }.body()

            if (response.success && response.session != null) {
                currentSession = response.session
                _authState.value = AuthState.Authenticated(
                    user = response.session.user,
                    accessToken = response.session.accessToken
                )
                Result.Success(response.session)
            } else {
                val error = response.error ?: "Authentication failed"
                _authState.value = AuthState.Error(error, response.errorCode)
                Result.Error(error)
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Network error"
            _authState.value = AuthState.Error(errorMsg)
            Result.Error(errorMsg)
        }
    }

    override fun getCurrentUser(): User? = currentSession?.user

    override fun isAuthenticated(): Boolean = currentSession != null

    override suspend fun logout() {
        try {
            currentSession?.accessToken?.let { token ->
                httpClient.post("${ApiConfig.BASE_URL}/auth/logout") {
                    header("Authorization", "Bearer $token")
                }
            }
        } catch (e: Exception) {
            // Logout locally even if server call fails
        } finally {
            currentSession = null
            storedState = null
            _authState.value = AuthState.Idle
        }
    }

    override fun validateState(receivedState: String): Boolean {
        return storedState == receivedState
    }

    override fun storeState(state: String) {
        storedState = state
    }

    companion object {
        private var instance: AuthRepositoryImpl? = null

        fun getInstance(httpClient: HttpClient): AuthRepositoryImpl {
            return instance ?: AuthRepositoryImpl(httpClient).also { instance = it }
        }
    }
}
