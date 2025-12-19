package com.example.mysterybox.auth

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.linecorp.linesdk.LineApiResponseCode
import com.linecorp.linesdk.Scope
import com.linecorp.linesdk.auth.LineAuthenticationParams
import com.linecorp.linesdk.auth.LineLoginApi
import com.linecorp.linesdk.auth.LineLoginResult

/**
 * Helper class for LINE SDK authentication
 */
class LineSdkLoginHelper(
    private val activity: Activity,
    private val channelId: String
) {
    companion object {
        const val REQUEST_CODE_LINE_LOGIN = 1001
    }

    /**
     * Start LINE Login using LINE SDK
     * This will use the LINE app if installed, otherwise falls back to browser
     */
    fun startLogin() {
        try {
            val loginIntent = LineLoginApi.getLoginIntent(
                activity,
                channelId,
                LineAuthenticationParams.Builder()
                    .scopes(listOf(Scope.PROFILE))
                    // Add nonce for additional security (optional but recommended)
                    .nonce(generateNonce())
                    .build()
            )
            activity.startActivityForResult(loginIntent, REQUEST_CODE_LINE_LOGIN)
        } catch (e: Exception) {
            // Handle error
            e.printStackTrace()
        }
    }

    /**
     * Handle the login result from LINE SDK
     */
    fun handleLoginResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        onSuccess: (accessToken: String, userId: String, displayName: String) -> Unit,
        onFailure: (error: String) -> Unit
    ) {
        if (requestCode != REQUEST_CODE_LINE_LOGIN) {
            return
        }

        val result = LineLoginApi.getLoginResultFromIntent(data)

        when (result.responseCode) {
            LineApiResponseCode.SUCCESS -> {
                // Login successful
                val accessToken = result.lineCredential?.accessToken?.tokenString
                val lineProfile = result.lineProfile
                
                if (accessToken != null && lineProfile != null) {
                    onSuccess(
                        accessToken,
                        lineProfile.userId,
                        lineProfile.displayName
                    )
                } else {
                    onFailure("Failed to get user information")
                }
            }
            LineApiResponseCode.CANCEL -> {
                // User canceled
                onFailure("Login canceled by user")
            }
            else -> {
                // Login failed
                val errorMsg = result.errorData?.message ?: "Login failed"
                onFailure(errorMsg)
            }
        }
    }

    /**
     * Generate a random nonce for CSRF protection
     */
    private fun generateNonce(): String {
        return java.util.UUID.randomUUID().toString()
    }
}
