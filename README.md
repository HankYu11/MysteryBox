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
- **Compose Multiplatform** - Declarative UI across platforms
- **Koin** - Dependency injection
- **Ktor Client** - HTTP networking with platform-specific engines (OkHttp/Darwin)
- **Ktor Auth Plugin** - Automatic token management and refresh
- **Coroutines & StateFlow** - Reactive state management
- **DataStore** (Android) / **Keychain** (iOS) - Secure token storage

## Architecture

This app follows [Android's official architecture guidelines](https://developer.android.com/topic/architecture) with clear separation of concerns and unidirectional data flow (UDF).

For detailed architecture documentation, see **[CLAUDE.md](CLAUDE.md)**.

### Quick Overview

```
composeApp/src/
├── commonMain/              # Shared KMP code
│   ├── auth/                # LINE auth utilities
│   ├── data/
│   │   ├── auth/            # AuthManager - global auth state holder
│   │   ├── dto/             # Network DTOs with domain mapping
│   │   ├── model/           # Domain models (User, MysteryBox, Result, ApiError)
│   │   ├── network/         # Ktor HTTP client, API service
│   │   ├── repository/      # Repository interfaces & implementations
│   │   └── storage/         # TokenStorage interface & DataStore impl
│   ├── di/                  # Koin dependency injection modules
│   ├── ui/
│   │   ├── navigation/      # Type-safe navigation routes
│   │   ├── screens/         # Stateless composable screens
│   │   ├── state/           # UI state classes (AuthState, etc.)
│   │   ├── theme/           # Material3 theming
│   │   ├── utils/           # UI helper functions
│   │   └── viewmodel/       # Screen-specific ViewModels
│   └── utils/               # Common utilities
├── androidMain/             # Android-specific implementations
│   ├── auth/                # LINE SDK integration
│   ├── data/
│   │   ├── network/         # OkHttp engine factory
│   │   └── storage/         # DataStore with FBE encryption
│   ├── di/                  # Android platform module
│   ├── ui/theme/            # Android theme configuration
│   └── utils/               # Android utilities
└── iosMain/                 # iOS-specific implementations
    ├── auth/                # Browser-based OAuth flow
    ├── data/
    │   ├── network/         # Darwin engine factory
    │   └── storage/         # Keychain secure storage
    ├── di/                  # iOS platform module
    ├── ui/theme/            # iOS theme configuration
    └── utils/               # iOS utilities
```

### Key Architectural Patterns

#### 1. Unidirectional Data Flow (UDF)
```
User Action → ViewModel → Repository → Data Source
                ↓
          StateFlow<State>
                ↓
           UI (Screen)
```

#### 2. Single Source of Truth
- **AuthManager**: Global authentication state shared across screens
- **ViewModels**: Screen-specific UI state (loading, errors, form data)
- **TokenStorage**: Secure credential persistence

#### 3. Reactive State Management
- ViewModels expose `StateFlow<T>` for UI observation
- `AuthManager` combines token and user data flows reactively
- UI uses `collectAsState()` for automatic recomposition

## Authentication Architecture

### Unified Token Management (Both Platforms)

Both Android and iOS now use **Ktor's Auth Plugin** for consistent token handling:

```kotlin
HttpClient {
    install(Auth) {
        bearer {
            loadTokens {
                // Load from secure storage (DataStore/Keychain)
            }

            refreshTokens {
                // Automatic refresh on 401
                // Updates AuthManager reactively
            }

            sendWithoutRequest {
                // Skip auth for public endpoints
            }
        }
    }
}
```

### Components

#### AuthManager (Shared)
- **Single source of truth** for authentication state
- Exposes `StateFlow<AuthState>` (Loading/Authenticated/Unauthenticated)
- Reactively combines `TokenStorage` flows
- Shared across all ViewModels

#### Secure Token Storage

**Android** - DataStore with File-Based Encryption:
- Uses AndroidX DataStore for coroutines-first storage
- File-Based Encryption (FBE) on Android 7.0+
- Atomic operations, no race conditions
- Reactive flows built-in

**iOS** - Keychain Services:
- Uses iOS Keychain for secure credential storage
- Hardware-backed encryption via Secure Enclave
- `kSecAttrAccessibleWhenUnlockedThisDeviceOnly` protection
- Reactive flow wrappers for consistency

#### Repositories
All async operations return `Result<T>`:
- `Result.Success<T>` - Contains data
- `Result.Error` - Contains typed `ApiError`

Enables clean error handling in ViewModels without exceptions.

### Authentication Flow

1. **Login** (LINE SDK on Android, Web OAuth on iOS)
   ```
   User taps login → LINE SDK/Browser → Access Token
                                            ↓
   LoginViewModel → AuthRepository.loginWithLineToken()
                                            ↓
   Backend verification → Save tokens → Update AuthManager
                                            ↓
                          AuthState.Authenticated emitted
                                            ↓
                          UI navigates to home
   ```

2. **Automatic Token Refresh** (Transparent to UI)
   ```
   API call returns 401 → Auth Plugin detects → refreshTokens()
                                                       ↓
                          AuthRepository.refreshToken() → New tokens
                                                       ↓
                          Save to TokenStorage → Update AuthManager
                                                       ↓
                          Original request retried → Success
   ```

3. **Logout**
   ```
   User taps logout → ProfileViewModel.logout()
                                            ↓
   AuthRepository.logout() → Backend call + Clear local tokens
                                            ↓
                          AuthState.Unauthenticated emitted
                                            ↓
                          UI navigates to login
   ```

## Security Features

- ✅ **Secure token storage** - Keychain (iOS) / DataStore with FBE (Android)
- ✅ **Automatic token refresh** - Handled transparently by Ktor Auth
- ✅ **No token logging** - Sensitive data excluded from logs
- ✅ **Result-based error handling** - No exceptions with sensitive info
- ✅ **HTTPS enforcement** - Network security config for production
- ✅ **Bearer token authentication** - Industry-standard OAuth flow

## Testing

Run unit tests:
```shell
./gradlew :composeApp:testDebugUnitTest
```

Test coverage includes:
- AuthManager state reactivity
- Token refresh flows
- ViewModel state management
- Repository error handling
- Mock implementations with turbine for Flow testing

## Build & Run

### Android
```shell
./gradlew :composeApp:assembleDebug
```

### iOS
Open `iosApp/iosApp.xcodeproj` in Xcode and run, or use Android Studio run configuration.

### Development Commands

See [CLAUDE.md](CLAUDE.md) for comprehensive development commands and architecture details.

## Requirements

- Android Studio Hedgehog or later
- Xcode 15+ (for iOS)
- JDK 17+

## API Configuration

Backend API configuration in `ApiConfig.kt`:
- Base URL: Currently local development (192.168.0.59:8080)
- LINE Channel ID: 2008724728

## Contributing

This project follows Android's [recommended architecture patterns](https://developer.android.com/topic/architecture/recommendations). Please read [CLAUDE.md](CLAUDE.md) before contributing to understand:

- Architecture principles (UDF, SSOT, separation of concerns)
- ViewModel guidelines (screen-specific state)
- Repository patterns (Result-based error handling)
- State management best practices
- Testing requirements

## License

MIT
