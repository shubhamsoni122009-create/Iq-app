# IQ Lock - Build & Installation Guide

## Quick Start

### Prerequisites
- **JDK 17+** - Download from [adoptium.net](https://adoptium.net)
- **Android SDK** - Install via Android Studio or command line
- **Git** - For cloning the repository

### Verify Your Setup

```bash
# Check Java version (must be 17+)
java -version

# Output should show: openjdk version "17.x.x"
```

## Building Locally

### Clone the Repository

```bash
git clone https://github.com/shubhamsoni122009-create/Iq-app.git
cd Iq-app/iq-lock
```

### Build Debug APK (Linux / macOS)

```bash
# Make gradlew executable (first time only)
chmod +x gradlew

# Build the APK
./gradlew clean assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Build Debug APK (Windows)

```cmd
# Navigate to project directory
cd iq-lock

# Build the APK
gradlew.bat clean assembleDebug

# Output: app\build\outputs\apk\debug\app-debug.apk
```

## Troubleshooting Local Builds

### Error: "JAVA_HOME is not set"

**macOS / Linux:**
```bash
# Add to ~/.bashrc or ~/.zshrc
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH

# Reload shell
source ~/.bashrc  # or source ~/.zshrc
```

**Windows:**
1. Open System Properties (Win + R → `sysdm.cpl`)
2. Click "Environment Variables"
3. Add `JAVA_HOME` pointing to your JDK 17 installation
4. Restart terminal

### Error: "Android SDK not found"

```bash
# Set ANDROID_HOME environment variable

# macOS / Linux:
export ANDROID_HOME=$HOME/Library/Android/sdk  # macOS
export ANDROID_HOME=$HOME/Android/Sdk          # Linux
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$PATH

# Windows:
# Set ANDROID_HOME to: C:\Users\YourUsername\AppData\Local\Android\sdk
```

### Gradle Build Cache Issues

```bash
# Clear Gradle cache
./gradlew clean --refresh-dependencies

# Or delete cache manually
rm -rf ~/.gradle/caches
```

### Out of Memory Errors

Edit `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
```

## Automated Builds via GitHub Actions

### Trigger a Build

1. Go to: https://github.com/shubhamsoni122009-create/Iq-app/actions
2. Click **"Build Debug APK"** workflow
3. Click **"Run workflow"** → **"Run workflow"**

### Download the APK

1. Wait for the workflow to complete (usually 10-15 minutes)
2. Click on the completed workflow run
3. Scroll to **"Artifacts"** section
4. Download **iq-lock-debug-apk.zip**
5. Extract and find `app-debug.apk`

### Automatic Triggers

The workflow automatically builds when:
- Code is pushed to `main` or `develop` branch
- Changes are made to files in `iq-lock/` directory
- A pull request is opened against `main` or `develop`

## Installing the APK

### On Android Device

**Via USB:**
```bash
adb install app-debug.apk
```

**Via File Manager:**
1. Copy `app-debug.apk` to your device
2. Open file manager
3. Tap the APK file
4. Tap **"Install"**

**Enable Installation from Unknown Sources:**
- Settings → Security → Unknown Sources → Enable

### On Android Emulator

```bash
# List running emulators
adb devices

# Install to specific emulator
adb -s emulator-5554 install app-debug.apk
```

## Build Output Location

After a successful build:

```
iq-lock/app/build/outputs/
├── apk/debug/
│   ├── app-debug.apk          ← Your installable APK
│   └── output-metadata.json
└── bundle/
    └── debug/
        └── app-debug.bundle
```

## Build Variants

### Debug Build (Default)
```bash
./gradlew assembleDebug
```
- Package: `com.iqlock.app.debug`
- Debuggable: Yes
- Minified: No
- Size: ~10-15 MB

### Release Build
```bash
./gradlew assembleRelease
```
- Package: `com.iqlock.app`
- Debuggable: No
- Minified: Yes (ProGuard)
- Requires signing key

## Advanced Gradle Tasks

```bash
# View all available tasks
./gradlew tasks

# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# View dependency tree
./gradlew :app:dependencies

# Show build info
./gradlew buildEnvironment

# Build and install to connected device
./gradlew installDebug

# Run app on connected device
./gradlew runDebug

# Generate APK and open reports
./gradlew assembleDebug --info
```

## Build Properties

View current build configuration:
```bash
./gradlew properties | grep -E "compileSdk|minSdk|targetSdk|versionCode|versionName"
```

Expected output:
```
compileSdk: 34
minSdk: 26
targetSdk: 34
versionCode: 1
versionName: 1.0.0
```

## Performance Tips

### Faster Builds
```bash
# Enable daemon (default) - reuses JVM
./gradlew assembleDebug

# Parallel build
./gradlew assembleDebug --parallel

# Configuration caching (enabled in gradle.properties)
./gradlew assembleDebug
```

### Incremental Builds
After first build, subsequent builds are faster:
```bash
# ~2-3 minutes first time
# ~30 seconds for incremental changes
./gradlew assembleDebug
```

## Cleaning Up

```bash
# Remove build artifacts
./gradlew clean

# Remove all build caches
rm -rf ~/.gradle/caches
./gradlew clean

# Free up disk space
./gradlew cleanBuildCache
```

## APK Signing (Release Only)

For production release builds, you need a keystore:

```bash
# Generate keystore
keytool -genkey -v -keystore my-app.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-app-alias

# Build signed APK
./gradlew assembleRelease
```

## Continuous Integration

This project uses GitHub Actions for CI/CD:

**Workflow File:** `.github/workflows/build-debug-apk.yml`

**What it does:**
- Checks out code
- Sets up JDK 17
- Runs `./gradlew clean assembleDebug`
- Uploads APK as artifact
- Generates build report

**View Builds:** https://github.com/shubhamsoni122009-create/Iq-app/actions

## Support & Help

For issues:
1. Check [Gradle troubleshooting](https://gradle.org/guides/executing-builds/)
2. Review build logs: `./gradlew assembleDebug --stacktrace`
3. Check Android SDK: `sdkmanager --list`
4. Verify JDK: `java -version` and `javac -version`

---

**Last Updated:** 2026-07-14  
**Build System:** Gradle 8.6  
**Android Gradle Plugin:** 8.3.2  
**Kotlin:** 1.9.23
