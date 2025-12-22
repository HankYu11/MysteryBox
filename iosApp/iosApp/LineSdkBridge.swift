import Foundation
import UIKit
import LineSDK
import ComposeApp

/**
 * Bridge between Kotlin IOSLineSdkManager and Swift LINE SDK.
 *
 * This class handles:
 * 1. Registering as the login handler with Kotlin
 * 2. Triggering LINE login via LoginManager when Kotlin requests it
 * 3. Extracting access token from result
 * 4. Calling back into Kotlin with success or error
 */
class LineSdkBridge {
    static let shared = LineSdkBridge()

    private init() {
        // Register this bridge as the login request handler
        IOSLineSdkManager.shared.setLoginRequestHandler {
            self.triggerLogin()
        }
    }

    /**
     * Trigger LINE login.
     * Called via the handler when Kotlin requests login.
     */
    private func triggerLogin() {
        // Get the root view controller from Kotlin
        guard let viewController = IOSLineSdkManager.shared.getRootViewController() else {
            IOSLineSdkManager.shared.handleLoginError(error: "No view controller available")
            return
        }

        // Dispatch to main queue since LoginManager.shared.login requires @MainActor
        DispatchQueue.main.async {
            // Start LINE SDK login
            // Requests .profile permission to get user name and picture
            LoginManager.shared.login(
                permissions: [.profile],
                in: viewController
            ) { result in
                switch result {
                case .success(let loginResult):
                    // Extract access token and send to Kotlin
                    let accessToken = loginResult.accessToken.value
                    IOSLineSdkManager.shared.handleLoginSuccess(accessToken: accessToken)

                case .failure(let error):
                    // Format error message and send to Kotlin
                    let errorMessage = self.formatLineError(error)
                    IOSLineSdkManager.shared.handleLoginError(error: errorMessage)
                }
            }
        }
    }

    /**
     * Format LINE SDK errors into user-friendly messages.
     * Matches the error handling pattern from Android implementation.
     */
    private func formatLineError(_ error: LineSDKError) -> String {
        // Use the error's localized description
        // LINE SDK provides user-friendly error messages
        return error.localizedDescription
    }
}
