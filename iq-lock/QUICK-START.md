# 🚀 Quick Start - Download & Install APK

## ⚡ Fastest Way to Get Your APK

### Step 1: Trigger the Build (2 clicks)
1. Go to: https://github.com/shubhamsoni122009-create/Iq-app/actions
2. Click **"Build Debug APK"** workflow (left sidebar)
3. Click **"Run workflow"** button (top right)
4. Select branch: **main**
5. Click **"Run workflow"** (green button)

### Step 2: Wait for Build (⏱️ 10-15 minutes)
- The workflow will execute automatically
- Watch the progress in real-time
- You'll see a ✅ when complete

### Step 3: Download APK (1 click)
1. Click on the completed workflow run
2. Scroll down to **"Artifacts"** section
3. Download **iq-lock-debug-apk** (ZIP file)
4. Extract the ZIP file
5. Find **app-debug.apk** inside

---

## 📱 Install on Your Android Device

### Option A: USB Cable (Recommended)
1. Connect Android phone to computer via USB
2. Enable Developer Options on phone:
   - Settings → About Phone → tap Build Number 7 times
   - Go back → Developer Options → enable USB Debugging
3. Run command:
   ```bash
   adb install app-debug.apk
   ```
4. Wait for "Success" message
5. App is installed! ✅

### Option B: Direct File Installation
1. Copy `app-debug.apk` to your Android device (USB/cloud)
2. Open file manager on phone
3. Tap the APK file
4. Tap **"Install"**
5. Grant permissions if prompted
6. Done! ✅

### Option C: Android Emulator
1. Open Android Studio → AVD Manager
2. Start an emulator
3. Run:
   ```bash
   adb install app-debug.apk
   ```

---

## 🛠️ Build Locally (Alternative)

If you prefer to build on your machine instead of using GitHub Actions:

### Prerequisites
- JDK 17+ installed
- Git installed
- Android SDK (via Android Studio)

### Build Steps

**Linux / macOS:**
```bash
git clone https://github.com/shubhamsoni122009-create/Iq-app.git
cd Iq-app/iq-lock
chmod +x gradlew
./gradlew clean assembleDebug
```

**Windows:**
```cmd
git clone https://github.com/shubhamsoni122009-create/Iq-app.git
cd Iq-app\iq-lock
gradlew.bat clean assembleDebug
```

APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

---

## ❓ Troubleshooting

### Download Failed
- Check your internet connection
- Try again in a few minutes
- Check workflow logs for errors

### Installation Failed
- **Unknown App:** Enable installation from unknown sources
  - Settings → Security → Unknown Sources → On
- **Version Conflict:** Uninstall previous version first
  ```bash
  adb uninstall com.iqlock.app.debug
  ```
- **Storage Full:** Free up device storage

### Build Failed (GitHub Actions)
1. Click on the failed workflow
2. Check the logs for error details
3. Common fixes:
   - Wait 5 minutes and retry
   - Try local build instead
   - Check for recent code changes

### Can't Find ADB Command
```bash
# macOS / Linux - add to PATH
export PATH=$PATH:~/Library/Android/sdk/platform-tools

# Windows - add to PATH
C:\Users\YourUsername\AppData\Local\Android\sdk\platform-tools
```

---

## 📋 App Info

| Property | Value |
|----------|-------|
| **Package Name** | com.iqlock.app.debug |
| **Version** | 1.0.0 |
| **Min Android** | 8.0 (API 26) |
| **Target Android** | 14 (API 34) |
| **APK Size** | ~10-15 MB |
| **Build Type** | Debug |
| **Debuggable** | Yes |

---

## 🎯 Next Steps After Installation

1. **Grant Permissions:**
   - Open IQ Lock app
   - Grant accessibility permissions (required)
   - Grant usage stats permission (required)

2. **Test Features:**
   - Select apps to protect
   - Set difficulty level
   - Lock an app and solve riddles to unlock

3. **Enable Accessibility Service:**
   - Settings → Accessibility → IQ Lock Service → Enable

---

## 📞 Need Help?

### Documentation
- **Full Build Guide:** `iq-lock/BUILD.md`
- **Project Info:** `iq-lock/README.md`
- **Build Summary:** `iq-lock/BUILD-SUMMARY.md`

### Common Questions

**Q: How often can I rebuild?**
A: As often as you want! Each new push triggers an automatic build.

**Q: Does debug APK work on real phones?**
A: Yes! It works just like release APK but with debugging enabled.

**Q: Can I share the APK with others?**
A: Yes, anyone with an Android phone can install it.

**Q: Where do I find old APKs?**
A: Go to Actions → select past workflow run → download from artifacts.

**Q: How long are APKs kept?**
A: Debug APKs are kept for 30 days, then auto-deleted.

---

## ✅ Verification Checklist

Before submitting a build:
- [ ] Code is committed and pushed to GitHub
- [ ] No build errors in GitHub Actions logs
- [ ] APK is downloadable from artifacts
- [ ] APK installs without errors on device
- [ ] App launches successfully
- [ ] Permissions are properly granted

---

## 🎉 Success!

Your IQ Lock debug APK is ready to install and test!

**Time to APK:** ~15 minutes (via GitHub Actions) or ~5 minutes (local build)

---

**Last Updated:** 2026-07-14  
**GitHub Actions Status:** ✅ Active  
**Build System:** Gradle 8.6
