# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build debug APK
gradlew assembleDebug

# Build release APK
gradlew assembleRelease

# Clean and rebuild
gradlew clean assembleDebug

# Build app bundle for Play Store
gradlew bundleRelease
```

## Testing

```bash
# Run unit tests (JVM)
gradlew test

# Run specific unit test
gradlew test --tests ExampleUnitTest

# Run instrumented tests (requires device/emulator)
gradlew connectedAndroidTest

# Run all tests
gradlew test connectedAndroidTest
```

## Running the App

```bash
# Install and run on connected device/emulator
gradlew installDebug
```

## Architecture

**Single Activity + Jetpack Compose** - Modern Android architecture using:
- Single `MainActivity` as the sole activity entry point
- Jetpack Compose for all UI (no XML layouts)
- Material Design 3 theming system
- Declarative, stateless composable functions

**Package Structure:**
```
com.example.healthypettracker
├── MainActivity.kt           # App entry point
└── ui/theme/
    ├── Color.kt              # Color palette definitions
    ├── Theme.kt              # HealthyPetTrackerTheme composable
    └── Type.kt               # Typography configuration
```

## Key Technologies

- **Kotlin 2.0.21** with JVM target 11
- **Jetpack Compose BOM 2024.09.00** with Material3
- **Target SDK 36** (Android 15), Min SDK 30 (Android 11)
- **Gradle 8.13** with version catalog (`gradle/libs.versions.toml`)

## Compose Patterns

Theme wrapper for all screens:
```kotlin
HealthyPetTrackerTheme {
    // Composable content
}
```

Composable preview for development:
```kotlin
@Preview(showBackground = true)
@Composable
fun MyPreview() {
    HealthyPetTrackerTheme {
        MyComposable()
    }
}
```

Edge-to-edge display is enabled via `enableEdgeToEdge()` in MainActivity.
