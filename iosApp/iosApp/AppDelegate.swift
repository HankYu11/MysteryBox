import UIKit
import LineSDK

/**
 * AppDelegate for LINE SDK initialization and URL handling.
 *
 * LINE SDK requires:
 * 1. Setup during app launch with channel ID
 * 2. URL handling for LINE app callback
 */
class AppDelegate: NSObject, UIApplicationDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // Initialize LINE SDK with channel ID from ApiConfig.kt
        // Channel ID: 2008724728
        LoginManager.shared.setup(
            channelID: "2008724728",
            universalLinkURL: nil
        )

        // Initialize the LINE SDK bridge to connect Kotlin and Swift
        _ = LineSdkBridge.shared

        return true
    }

    /**
     * Handle URL callbacks from LINE SDK.
     * This is called when LINE app redirects back to our app after authentication.
     */
    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey : Any] = [:]
    ) -> Bool {
        return LoginManager.shared.application(app, open: url)
    }
}
