# MysteryBox

A Kotlin Multiplatform mobile app for reducing food waste by connecting users with mystery boxes of discounted near-expiry food items from local merchants.

## Features

- Browse available mystery boxes from local stores
- Filter by availability status (Available, Almost Sold Out, Sold Out)
- Reserve mystery boxes for pickup
- View and manage reservations
- LINE OAuth authentication
- Merchant portal for uploading boxes

## Tech Stack

- **Kotlin Multiplatform** - Shared business logic for Android & iOS
- **Compose Multiplatform** - Shared UI across platforms
- **Koin** - Dependency injection
- **Ktor** - HTTP client for API calls with platform-specific engines (OkHttp on Android, Darwin on iOS).
- **Coroutines & Flow** - Async programming and reactive state

## Architecture

The app follows a modern mobile architecture based on declarative UI and reactive principles.

```
composeApp/src/
├── commonMain/          # Shared code
│   ├── data/
│   │   ├── auth/        # Authentication logic (Interceptor, Authenticator)
│   │   ├── model/       # Data classes
│   │   ├── network/     # Ktor HTTP client
│   │   └── repository/  # Data access layer
│   ├── di/              # Koin modules
│   └── ui/
│       ├── screens/     # Composable screens
│       ├── theme/       # Colors, typography
│       └── viewmodel/   # ViewModels with StateFlow
├── androidMain/         # Android-specific code
└── iosMain/             # iOS-specific code
```

### Authentication Flow

Authentication is managed centrally to ensure robustness and a clean separation of concerns. This architecture handles automatic token injection and background refreshing seamlessly on both Android and iOS.

#### Android

On Android, the Ktor client is configured to use the OkHttp engine, enabling advanced features:

-   **`AuthManager`**: The single source of truth for authentication state. It exposes a `StateFlow<AuthState>` that the UI layer observes to react to global login/logout events.
-   **`AuthInterceptor` (OkHttp)**: An interceptor that automatically attaches the JWT `Authorization` header to every outgoing Ktor request.
-   **`TokenAuthenticator` (OkHttp)**: When an API call returns a `401 Unauthorized` error (due to an expired token), this authenticator is triggered. It silently calls the refresh token endpoint, saves the new tokens via the repository, updates the `AuthManager`, and automatically retries the original request.
-   **`AuthRepository`**: Handles the underlying API calls for login, logout, and token refresh, as well as persisting the session data locally.

#### iOS

On iOS, which uses Ktor's native Darwin engine, the same behavior is achieved using the built-in `Auth` plugin:

-   **`AuthManager`**: Acts as the single source of truth, identical to the Android implementation.
-   **`Auth` Plugin (Ktor)**: This plugin is configured to manage bearer tokens.
    -   It automatically loads the access token from the `AuthRepository` before each request.
    -   When a `401 Unauthorized` error occurs, its `refreshTokens` lambda is triggered. This lambda calls the `authRepository.refreshToken()` method, saves the new tokens, updates the `AuthManager`, and allows Ktor to automatically retry the original request.
-   **`AuthRepository`**: The same shared repository handles the underlying API calls for login, logout, and token refresh.

This dual setup ensures that token expiry is handled gracefully in the background on both platforms, providing a consistent and smooth user experience.

## Build & Run

### Android
```shell
./gradlew :composeApp:assembleDebug
```

### iOS
Open `iosApp/iosApp.xcodeproj` in Xcode and run, or use the run configuration in Android Studio/Fleet.

## Requirements

- Android Studio Hedgehog or later
- Xcode 15+ (for iOS)
- JDK 17+

## License

MIT
