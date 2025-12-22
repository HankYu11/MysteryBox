import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    // Use AppDelegate for LINE SDK setup and URL handling
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}