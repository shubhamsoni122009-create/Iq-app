# IQ Lock - Android App

An intelligent lock screen application that presents riddles and challenges to unlock your phone.

## Project Structure

```
iq-lock/
├── app/                           # Main Android application module
│   ├── src/
│   │   └── main/
│   │       ├── kotlin/            # Kotlin source code
│   │       │   └── com/iqlock/app/
│   │       │       ├── ui/        # UI activities
│   │       │       ├── service/   # Accessibility service
│   │       │       ├── receiver/  # Boot receiver
│   │       │       ├── data/      # Room database entities & DAOs
│   │       │       ├── di/        # Hilt dependency injection
│   │       │       ├── engine/    # Challenge/riddle engines
│   │       │       ├── viewmodel/ # MVVM ViewModels
│   │       │       └── adapter/   # RecyclerView adapters
│   │       ├── res/               # Android resources
│   │       │   ├── layout/        # XML layout files
│   │       │   ├── drawable/      # Vector drawables
│   │       │   ├── values/        # Strings, colors, styles
│   │       │   ├── xml/           # Accessibility config
│   │       │   └── mipmap/        # App icons
│   │       └── AndroidManifest.xml
│   ├── build.gradle.kts           # App-level Gradle config
│   └── proguard-rules.pro         # ProGuard obfuscation rules
├── gradle/
│   ├── wrapper/                   # Gradle wrapper
│   └── libs.versions.toml         # Version catalog
├── build.gradle.kts               # Project-level Gradle config
├── settings.gradle.kts            # Gradle settings
├── gradle.properties              # Gradle properties
├── gradlew                        # Gradle wrapper (Linux/Mac)
├── gradlew.bat                    # Gradle wrapper (Windows)
└── README.md
```

## Build Requirements

- **JDK:** 17+
- **Android SDK:** API 34 (compileSdk)
- **Minimum SDK:** API 26 (Android 8.0)
- **Target SDK:** API 34

## Dependencies

- **AndroidX Core:** 1.13.1
- **Material Design 3:** 1.12.0
- **Room Database:** 2.6.1
- **Hilt DI:** 2.51
- **Kotlin Coroutines:** 1.8.1
- **WorkManager:** 2.9.0
- **Glide:** 4.16.0

See `gradle/libs.versions.toml` for complete dependency list.

## Building the Project

### Prerequisites

1. Install JDK 17+ (required for Java 17 bytecode)
2. Install Android SDK with API 34

### Build Commands

#### Linux / macOS

```bash
cd iq-lock
./gradlew clean assembleDebug      # Build debug APK
./gradlew clean assembleRelease    # Build release APK
```

#### Windows

```cmd
cd iq-lock
gradlew.bat clean assembleDebug    # Build debug APK
gradlew.bat clean assembleRelease  # Build release APK
```

### Gradle Tasks

- `./gradlew clean` - Clean build artifacts
- `./gradlew build` - Compile and run tests
- `./gradlew assembleDebug` - Build debug APK
- `./gradlew assembleRelease` - Build signed release APK
- `./gradlew test` - Run unit tests
- `./gradlew connectedAndroidTest` - Run instrumented tests on device/emulator
- `./gradlew :app:dependencies` - View dependency tree

## Output Artifacts

After building, the APK files are located at:

- **Debug:** `app/build/outputs/apk/debug/app-debug.apk`
- **Release:** `app/build/outputs/bundle/release/app-release.aab`

## Automated Builds

This project uses **GitHub Actions** to automatically build debug APKs.

### GitHub Actions Workflow

The `.github/workflows/build-debug-apk.yml` workflow:

1. Triggers on push to `main` or `develop` branches
2. Sets up JDK 17
3. Runs `./gradlew clean assembleDebug`
4. Uploads the APK as an artifact
5. Generates a build summary

**Download APK:**
1. Go to the Actions tab
2. Select the latest build
3. Download the `iq-lock-debug-apk` artifact

## ProGuard Configuration

Release builds use ProGuard for code obfuscation. Rules are defined in `app/proguard-rules.pro` to:

- Preserve Room entity classes
- Keep Hilt generated components
- Maintain accessibility service functionality
- Protect boot receiver
- Preserve Glide and Coroutines functionality

## Permissions

The app requires the following permissions (see `AndroidManifest.xml`):

- `BIND_ACCESSIBILITY_SERVICE` - Detect foreground app changes
- `PACKAGE_USAGE_STATS` - Read per-app usage statistics
- `SYSTEM_ALERT_WINDOW` - Display lock screen overlay
- `RECEIVE_BOOT_COMPLETED` - Auto-start on device reboot
- `FOREGROUND_SERVICE` - Run foreground service
- `POST_NOTIFICATIONS` - Send notifications (API 33+)

## Troubleshooting

### Build Fails with "JAVA_HOME not set"

Ensure JDK 17+ is installed and JAVA_HOME points to it:

```bash
# Linux/macOS
export JAVA_HOME=/path/to/jdk17
export PATH=$JAVA_HOME/bin:$PATH

# Verify
java -version  # Should show Java 17+
```

### Gradle Cache Issues

Clear the Gradle cache:

```bash
./gradlew clean --refresh-dependencies
```

### Android SDK Issues

Ensure API 34 is installed:

```bash
${ANDROID_SDK}/cmdline-tools/latest/bin/sdkmanager "platforms;android-34"
```

### Kapt/Hilt Compilation Errors

These often resolve after a clean rebuild:

```bash
./gradlew clean build
```

## Development

### Code Style

- Kotlin official style guide (configured via `kotlin.code.style=official`)
- AndroidX recommended practices
- MVVM architecture with Hilt DI

### Adding Dependencies

Update `gradle/libs.versions.toml` with new version, then add to `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.your.new.library)
}
```

## License

See LICENSE file for details.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit changes
4. Push to branch
5. Open a pull request

---

**Built with:** Kotlin • Jetpack • Material Design 3
