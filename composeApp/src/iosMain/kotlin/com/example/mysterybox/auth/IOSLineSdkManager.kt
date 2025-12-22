package com.example.mysterybox.auth

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import platform.UIKit.UIViewController

/**
 * Singleton manager for iOS LINE SDK integration.
 * Provides bridge between Swift LINE SDK callbacks and Kotlin Compose code.
 *
 * Flow:
 * 1. Kotlin calls startLogin(callback) - stores callback and notifies Swift via callback
 * 2. Swift (LineSdkBridge) detects login request and triggers LINE SDK
 * 3. Swift LINE SDK completes login - calls handleLoginSuccess or handleLoginError
 * 4. Manager invokes stored callback on main thread
 * 5. Callback reaches LoginViewModel (same as Android)
 *
 * NOTE: This manager is exported to Objective-C/Swift and can be called from iOS code.
 */
object IOSLineSdkManager {
    private val scope = MainScope()
    private var currentCallback: ((accessToken: String?, error: String?) -> Unit)? = null
    private var rootViewController: UIViewController? = null
    private var loginRequestHandler: (() -> Unit)? = null

    /**
     * Store the root view controller for presenting LINE login.
     * Called from ContentView.swift when the view controller is created.
     */
    fun setRootViewController(viewController: UIViewController) {
        rootViewController = viewController
    }

    /**
     * Get the root view controller for presenting LINE login.
     * Called from Swift LineSdkBridge when triggering login.
     */
    fun getRootViewController(): UIViewController? = rootViewController

    /**
     * Set a handler that Swift will call when login is requested.
     * Called from Swift LineSdkBridge during initialization.
     */
    fun setLoginRequestHandler(handler: () -> Unit) {
        loginRequestHandler = handler
    }

    /**
     * Start LINE login with callback.
     * Called from Kotlin Compose code via rememberLineSdkLauncher.
     *
     * @param callback Invoked with (accessToken, null) on success or (null, error) on failure
     */
    fun startLogin(callback: (accessToken: String?, error: String?) -> Unit) {
        currentCallback = callback

        // Notify Swift that login was requested
        loginRequestHandler?.invoke() ?: run {
            // If no handler is set, return error
            scope.launch {
                callback(null, "Login handler not initialized")
                currentCallback = null
            }
        }
    }

    /**
     * Called from Swift when LINE login completes successfully.
     * Invokes the stored callback on main thread with the access token.
     *
     * @param accessToken The LINE access token to send to backend
     */
    fun handleLoginSuccess(accessToken: String) {
        scope.launch {
            currentCallback?.invoke(accessToken, null)
            currentCallback = null
        }
    }

    /**
     * Called from Swift when LINE login fails or is cancelled.
     * Invokes the stored callback on main thread with the error message.
     *
     * @param error Human-readable error message
     */
    fun handleLoginError(error: String) {
        scope.launch {
            currentCallback?.invoke(null, error)
            currentCallback = null
        }
    }
}
