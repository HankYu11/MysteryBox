package com.example.mysterybox.data.auth

import com.example.mysterybox.data.model.Result
import com.example.mysterybox.data.repository.AuthRepository
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.header
import io.ktor.http.auth.AuthScheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val authRepository: AuthRepository,
    private val authManager: AuthManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // We need to have a refresh token to attempt a refresh
        val currentSession = runBlocking { authRepository.getCurrentSession().first() }
        if (currentSession is Result.Error || currentSession.data?.refreshToken == null) {
            runBlocking { authManager.clearSession() }
            return null // No refresh token, logout
        }

        // Synchronously refresh the token
        val tokenResponse = runBlocking { authRepository.refreshToken() }

        return if (tokenResponse is Result.Success && tokenResponse.data != null) {
            // New tokens received, save them
            runBlocking {
                authRepository.saveSession(tokenResponse.data)
                // Also update the in-memory auth state
                val userResult = authRepository.getCurrentUser()
                if (userResult is Result.Success) {
                    authManager.setAuthenticated(userResult.data, tokenResponse.data.accessToken)
                }
            }
            // Retry the original request with the new access token
            response.request.newBuilder()
                .header("Authorization", "Bearer ${tokenResponse.data.accessToken}")
                .build()
        } else {
            // Refresh failed, force logout
            runBlocking { authManager.clearSession() }
            null // Returning null causes the original request to fail
        }
    }
}

class AuthInterceptor(
    private val authRepository: AuthRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            val session = authRepository.getCurrentSession().first()
            if (session is Result.Success) session.data?.accessToken else null
        }

        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
