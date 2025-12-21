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

**Kotlin Multiplatform + Compose Multiplatform** app following [Android's official architecture guidelines](https://developer.android.com/topic/architecture).

### Architecture Principles

This app follows the [recommended app architecture](https://developer.android.com/topic/architecture/recommendations):

1. **Separation of Concerns**: UI layer, Domain layer (implicit), Data layer clearly separated
2. **Drive UI from Data Models**: UI reflects data layer state via ViewModels
3. **Single Source of Truth**: Each data type has a single authoritative source
4. **Unidirectional Data Flow (UDF)**: State flows down, events flow up
5. **Reactive Programming**: StateFlow for observable state management

### Architectural Layers

#### UI Layer
- **Screens** (`ui/screens/`): Composable functions for UI rendering
- **ViewModels** (`ui/viewmodel/`): Manage UI state and business logic
  - `LoginViewModel`: Handles LINE login flow with isolated loading/error states
  - `WelcomeViewModel`: Observes global auth state for navigation
  - `ProfileViewModel`: Manages profile display and logout with Result-based flow
  - `BoxViewModel`: Mystery box listing, filtering, and search
  - `MerchantViewModel`: Merchant portal functionality
  - `ReservationViewModel`: User reservation management
- **State Classes** (`ui/state/`): Sealed classes representing UI states
  - `AuthState`: Global authentication state (Idle, Loading, Authenticated, Error)
  - `LoginUiState`: Screen-specific login states
  - `ProfileUiState`: Screen-specific profile states

#### Data Layer
- **Repositories** (`data/repository/`): Abstract data sources, expose clean APIs
  - All repositories return `Result<T>` for proper error handling
  - Interfaces define contracts, Impl classes provide implementations
  - Examples: `AuthRepository`, `BoxRepository`, `MerchantRepository`, `ReservationRepository`
- **Data Sources**:
  - **Network** (`data/network/`): Ktor HTTP client, API service
  - **Local** (`data/storage/`): TokenStorage for secure credential management
- **Models**:
  - **DTOs** (`data/dto/`): Network response models with mapping to domain models
  - **Domain Models** (`data/model/`): App's business objects (User, MysteryBox, etc.)

#### Shared State Management
- **AuthManager** (`data/auth/`): Singleton managing global authentication state
  - Single source of truth for authentication status
  - Exposes `StateFlow<AuthState>` for reactive observation
  - Used by multiple ViewModels to access shared auth state
  - Follows [state holder pattern](https://developer.android.com/topic/architecture/ui-layer/state-holders)

#### Dependency Injection
- **Koin modules** (`di/AppModule.kt`):
  - `networkModule`: HTTP client, TokenManager, AuthManager, API service
  - `repositoryModule`: Repository implementations
  - `viewModelModule`: ViewModel definitions with proper scoping

### Directory Structure
```
composeApp/src/
├── commonMain/          # Shared KMP code
│   ├── data/            # Data Layer
│   │   ├── auth/        # AuthManager - shared authentication state holder
│   │   ├── dto/         # Network DTOs with mapping to domain models
│   │   ├── model/       # Domain models (MysteryBox, User, Result, ApiError)
│   │   ├── network/     # Ktor HTTP client, API service, TokenManager
│   │   ├── repository/  # Repository interfaces and implementations
│   │   └── storage/     # Local data storage (TokenStorage)
│   ├── di/              # Dependency injection (Koin modules)
│   └── ui/              # UI Layer
│       ├── navigation/  # Type-safe navigation routes
│       ├── screens/     # Composable screens (stateless UI)
│       ├── state/       # UI state classes (AuthState, etc.)
│       ├── theme/       # Material3 theming
│       └── viewmodel/   # ViewModels (state holders for screens)
├── androidMain/         # Android-specific implementations
│   ├── auth/            # LINE SDK integration
│   └── data/network/    # OkHttp client factory
└── iosMain/             # iOS-specific implementations
    └── data/network/    # Darwin client factory
```

### State Management Patterns

#### Unidirectional Data Flow (UDF)
Following [Android's UDF recommendations](https://developer.android.com/topic/architecture/ui-layer#udf):

```
┌─────────────┐
│   Screen    │  ← observes state via collectAsState()
└──────┬──────┘
       │ user events (onClick, etc.)
       ↓
┌─────────────┐
│  ViewModel  │  ← manages UI state with StateFlow
└──────┬──────┘
       │ calls suspend functions
       ↓
┌─────────────┐
│ Repository  │  ← returns Result<T> for operations
└──────┬──────┘
       │ network/storage calls
       ↓
┌─────────────┐
│ Data Source │  ← API service, TokenStorage, etc.
└─────────────┘
```

#### State Isolation Strategy

**Screen-Specific State** (in ViewModel):
- Loading states for async operations
- Error messages for user feedback
- Form input validation states
- Screen-specific UI states

**Shared State** (in AuthManager):
- Global authentication status
- Current user information
- Access tokens

This separation prevents state pollution between screens while maintaining a single source of truth for shared data.

#### Error Handling
All repository methods return `Result<T>` sealed class:
- `Result.Success<T>`: Contains successful data
- `Result.Error`: Contains `ApiError` with type-safe error handling

ViewModels map these results to appropriate UI states for user feedback.

## Key Technologies & Libraries

- **Kotlin Coroutines**: Async operations with structured concurrency
- **StateFlow**: Reactive state management following [StateFlow best practices](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)
- **Jetpack Compose**: Declarative UI framework
- **Navigation Compose**: Type-safe navigation with serializable routes
- **Koin**: Dependency injection framework
- **LINE SDK**: Android-only auth integration via `LineSdkLoginManager` and `LineSdkLauncherProvider`
- **Ktor**: HTTP client with platform-specific engines (OkHttp/Darwin)
- **Coil**: Image loading with Ktor networking support

## Authentication Flow

Following [recommended auth patterns](https://developer.android.com/topic/architecture/data-layer#make-decisions):

1. **LINE SDK (Android)**: Direct token verification via `/api/auth/line/verify-token`
   - `LoginViewModel` manages login-specific UI state
   - `AuthManager` stores global authentication state
2. **Web OAuth (iOS)**: Browser-based flow via `/api/auth/line` with callback handling
3. **Token Management**: `TokenManager` handles JWT storage and refresh logic
4. **Session Initialization**: `AuthManager.initialize()` called on app startup to restore session

## API Configuration

Backend configuration in `data/network/ApiConfig.kt`:
- Base URL: Currently set to local network IP (192.168.0.59:8080)
- LINE Channel ID: 2008724728
- Comprehensive endpoint definitions for auth, boxes, reservations, merchant operations

## Platform-Specific Notes

- **Android**: LINE SDK integration requires cleartext network config for local development
- **iOS**: Uses browser-based LINE OAuth flow
- **Network**: Platform-specific HTTP client factories in respective `HttpClientFactory` files

## Architecture Best Practices

When modifying or extending this codebase, follow these guidelines:

### ViewModel Guidelines
1. **ViewModels should be screen-specific**: Each screen should have its own ViewModel managing only that screen's state
2. **No shared ViewModels**: Avoid sharing ViewModel instances between screens (use state holders like `AuthManager` for shared state instead)
3. **StateFlow for state**: Expose UI state as `StateFlow<T>`, never `MutableStateFlow`
4. **Launch coroutines in viewModelScope**: Use `viewModelScope.launch` for async operations
5. **Handle all Result cases**: When calling repository methods, handle both Success and Error cases

### Repository Guidelines
1. **Return Result<T>**: All async operations should return `Result<Success>` or `Result.Error`
2. **Single responsibility**: Each repository handles one domain area (auth, boxes, etc.)
3. **Interface-based**: Define interface in `repository/`, implement in `repository/` with `Impl` suffix
4. **Don't expose implementation details**: Return domain models, not DTOs

### State Management Guidelines
1. **Shared state via state holders**: Use dedicated singleton classes (like `AuthManager`) for app-wide state
2. **Screen state in ViewModels**: Keep screen-specific loading/error states in ViewModels
3. **Sealed classes for UI state**: Use sealed classes to represent distinct UI states
4. **Single source of truth**: Each piece of data should have exactly one authoritative source

### Composable Guidelines
1. **Stateless UI**: Screens should be stateless, accepting state via parameters
2. **Events flow up**: User interactions should be callbacks passed down from parent
3. **Observe state with collectAsState()**: Use `by viewModel.state.collectAsState()` for reactive updates
4. **LaunchedEffect for side effects**: Use `LaunchedEffect` for navigation, toasts, etc.

### Dependency Injection Guidelines
1. **Constructor injection**: All dependencies injected via constructor
2. **Prefer `single` for stateful services**: Use Koin `single` for repositories, managers, etc.
3. **Use `viewModel` DSL**: Koin provides special `viewModel` scope for ViewModels
4. **Module organization**: Keep modules organized by layer (network, repository, viewModel)

## References

- [Guide to app architecture](https://developer.android.com/topic/architecture)
- [Recommendations for Android architecture](https://developer.android.com/topic/architecture/recommendations)
- [UI layer architecture](https://developer.android.com/topic/architecture/ui-layer)
- [Data layer architecture](https://developer.android.com/topic/architecture/data-layer)
- [StateFlow and SharedFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)