package com.example.mysterybox.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import com.linecorp.linesdk.LineApiResponseCode
import com.linecorp.linesdk.Scope
import com.linecorp.linesdk.auth.LineAuthenticationParams
import com.linecorp.linesdk.auth.LineLoginApi
import com.linecorp.linesdk.auth.LineLoginResult

/**
 * Activity Result Contract for LINE SDK login
 */
class LineLoginContract(
    private val channelId: String
) : ActivityResultContract<Unit, LineLoginResult?>() {

    override fun createIntent(context: Context, input: Unit): Intent {
        return LineLoginApi.getLoginIntent(
            context,
            channelId,
            LineAuthenticationParams.Builder()
                .scopes(listOf(Scope.PROFILE))
                .nonce(java.util.UUID.randomUUID().toString())
                .build()
        )
    }

    override fun parseResult(resultCode: Int, intent: Intent?): LineLoginResult? {
        return LineLoginApi.getLoginResultFromIntent(intent)
    }
}

/**
 * Helper object for LINE SDK authentication
 */
object LineSdkLoginHelper {
    /**
     * Handle the login result from LINE SDK
     */
    fun handleLoginResult(
        result: LineLoginResult?,
        onSuccess: (accessToken: String) -> Unit,
        onFailure: (error: String) -> Unit
    ) {
        if (result == null) {
            onFailure("Login result is null")
            return
        }

        when (result.responseCode) {
            LineApiResponseCode.SUCCESS -> {
                val accessToken = result.lineCredential?.accessToken?.tokenString

                if (accessToken != null) {
                    onSuccess(accessToken)
                } else {
                    onFailure("Failed to get access token")
                }
            }
            LineApiResponseCode.CANCEL -> {
                onFailure("Login canceled by user")
            }
            else -> {
                val errorMsg = result.errorData?.message ?: "Login failed"
                onFailure(errorMsg)
            }
        }
    }
}
