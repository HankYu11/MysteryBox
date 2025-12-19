# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Build & Run
```shell
# Build Android APK
./gradlew :composeApp:assembleDebug

# Run tests 
./gradlew :composeApp:testDebugUnitTest

# iOS: Open iosApp/iosApp.xcodeproj in Xcode or use Android Studio run configuration
```

### Testing
- Tests are located in `composeApp/src/commonTest/`
- Uses kotlin-test, kotlinx-coroutines-test, turbine for flow testing, and ktor-client-mock for network mocking
- Run tests with: `./gradlew :composeApp:testDebugUnitTest`
- Test fixtures available in `testutil/TestFixtures.kt`

## Architecture Overview

**Kotlin Multiplatform + Compose Multiplatform** app with shared business logic and UI.

### Key Architectural Patterns

1. **Repository Pattern**: Data layer abstraction with interfaces in `data/repository/`
   - All repositories have impl classes (e.g., `AuthRepositoryImpl`, `BoxRepositoryImpl`)
   - Network layer uses Ktor HTTP client with platform-specific implementations

2. **Dependency Injection**: Koin modules in `di/AppModule.kt`
   - `networkModule`: HTTP client, TokenManager, API service
   - `repositoryModule`: Repository implementations
   - `viewModelModule`: ViewModels with StateFlow

3. **MVVM**: ViewModels in `ui/viewmodel/` use StateFlow for reactive state management
   - `AuthViewModel`: LINE OAuth authentication
   - `BoxViewModel`: Mystery box listing and filtering
   - `MerchantViewModel`: Merchant portal functionality
   - `ReservationViewModel`: User reservations

### Directory Structure
```
composeApp/src/
├── commonMain/          # Shared KMP code
│   ├── data/
│   │   ├── dto/         # Network DTOs with mapping functions
│   │   ├── model/       # Domain models (MysteryBox, User, etc.)
│   │   ├── network/     # Ktor HTTP client, API service, TokenManager
│   │   └── repository/  # Data access layer with interfaces
│   ├── di/              # Koin dependency injection modules
│   └── ui/
│       ├── screens/     # Composable screens (9 total)
│       ├── theme/       # Material3 theming
│       └── viewmodel/   # StateFlow-based ViewModels
├── androidMain/         # Android-specific (LINE SDK, HTTP client)
└── iosMain/             # iOS-specific implementations
```

## Key Technologies & Libraries

- **LINE SDK**: Android-only auth integration via `LineSdkLoginManager` and `LineSdkLauncherProvider`
- **Ktor**: HTTP client with platform-specific engines (OkHttp/Darwin)
- **Coil**: Image loading with Ktor networking support
- **Navigation**: Jetpack Compose navigation with type-safe routes in `navigation/Routes.kt`

## Authentication Flow

1. **LINE SDK (Android)**: Direct token verification via `/api/auth/line/verify-token`
2. **Web OAuth (iOS)**: Browser-based flow via `/api/auth/line` with callback handling
3. **Token Management**: `TokenManager` handles JWT storage and refresh logic

## API Configuration

Backend configuration in `data/network/ApiConfig.kt`:
- Base URL: Currently set to local network IP (192.168.0.59:8080)
- LINE Channel ID: 2008724728
- Comprehensive endpoint definitions for auth, boxes, reservations, merchant operations

## Platform-Specific Notes

- **Android**: LINE SDK integration requires cleartext network config for local development
- **iOS**: Uses browser-based LINE OAuth flow
- **Network**: Platform-specific HTTP client factories in respective `HttpClientFactory` files