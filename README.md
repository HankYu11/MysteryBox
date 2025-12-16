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
- **Ktor** - HTTP client for API calls
- **Coroutines & Flow** - Async programming and reactive state

## Architecture

```
composeApp/src/
├── commonMain/          # Shared code
│   ├── data/
│   │   ├── model/       # Data classes
│   │   ├── network/     # HTTP client
│   │   └── repository/  # Data access layer
│   ├── di/              # Koin modules
│   └── ui/
│       ├── screens/     # Composable screens
│       ├── theme/       # Colors, typography
│       └── viewmodel/   # ViewModels with StateFlow
├── androidMain/         # Android-specific code
└── iosMain/             # iOS-specific code
```

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
