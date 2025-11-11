# Building and Debugging the App

## Quick Option: Use Android Studio (Recommended)

The easiest way to build and debug:

1. **Open Android Studio**
2. **File → Open** → Select this project folder (`/Users/sudheer/GarbageThrowDetector`)
3. Android Studio will:
   - Download missing SDK components automatically
   - Set up Gradle
   - Sync the project
4. **Connect your phone** (already connected: c6a1e348 ✓)
5. **Click the green Run button** (▶️) or press `Shift+F10`
6. Select your device and the app will build and install!

## Command Line Option (Requires Full SDK)

If you prefer command line, you need to install Android SDK Platform 34:

```bash
# Install Android Studio to get the full SDK Manager
# OR manually download and install SDK Platform 34 from:
# https://developer.android.com/studio/command-line/sdkmanager
```

Then:
```bash
./gradlew installDebug
```

## Current Status

- ✅ Device connected: c6a1e348
- ✅ ADB installed
- ✅ OpenCV SDK integrated
- ⚠️ Need Android SDK Platform 34 (easiest with Android Studio)

## Alternative: Lower SDK Version

If you want to avoid installing Platform 34, we can change to SDK 33 or 32 in `app/build.gradle`:

```gradle
compileSdk 33  // Instead of 34
targetSdk 33   // Instead of 34
```

But using Android Studio is still recommended as it handles everything automatically!


