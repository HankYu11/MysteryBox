package com.example.mysterybox.auth

import android.app.Activity
import android.content.Intent
import com.example.mysterybox.data.network.ApiConfig

/**
 * Singleton manager for LINE SDK login operations
 */
object LineSdkLoginManager {
    private var activity: Activity? = null
    private var loginHelper: LineSdkLoginHelper? = null
    private var loginCallback: ((accessToken: String?, userId: String?, displayName: String?, error: String?) -> Unit)? = null

    fun setActivity(activity: Activity) {
        this.activity = activity
        this.loginHelper = LineSdkLoginHelper(activity, ApiConfig.LINE_CHANNEL_ID)
    }

    fun clearActivity() {
        this.activity = null
        this.loginHelper = null
    }

    /**
     * Start LINE Login
     */
    fun startLogin(onResult: (accessToken: String?, userId: String?, displayName: String?, error: String?) -> Unit) {
        loginCallback = onResult
        loginHelper?.startLogin() ?: run {
            onResult(null, null, null, "Activity not initialized")
        }
    }

    /**
     * Handle login result from MainActivity
     */
    fun handleLoginResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = loginCallback ?: return
        
        loginHelper?.handleLoginResult(
            requestCode,
            resultCode,
            data,
            onSuccess = { accessToken, userId, displayName ->
                callback(accessToken, userId, displayName, null)
                loginCallback = null
            },
            onFailure = { error ->
                callback(null, null, null, error)
                loginCallback = null
            }
        )
    }
}
