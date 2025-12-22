import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let viewController = MainViewControllerKt.MainViewController()

        // Store reference in IOSLineSdkManager for LINE SDK presentation
        // Use main queue to ensure thread safety
        DispatchQueue.main.async {
            IOSLineSdkManager.shared.setRootViewController(viewController: viewController)
        }

        return viewController
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}



