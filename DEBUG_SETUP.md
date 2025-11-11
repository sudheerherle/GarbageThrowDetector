# Quick Debug Setup Guide

## Prerequisites

1. **Android Studio** installed (recommended for easiest setup)
2. **Android device** with USB debugging enabled
3. **OpenCV Android SDK** (see options below)

## Option 1: Debug Using Android Studio (Easiest)

1. **Open the project in Android Studio:**
   ```bash
   # If Android Studio is installed
   open -a "Android Studio" .
   ```

2. **Integrate OpenCV SDK:**
   - Download OpenCV Android SDK from: https://opencv.org/releases/
   - Extract the archive
   - In Android Studio: File → New → Import Module
   - Select the `<OpenCV_SDK>/sdk` folder
   - Make sure the module name is `opencv` (or update `settings.gradle`)

3. **Connect your device:**
   - Enable Developer Options on your Android phone
   - Enable USB Debugging
   - Connect via USB
   - Verify device appears in Android Studio's device selector

4. **Build and Run:**
   - Click the "Run" button (green play icon) or press Shift+F10
   - Select your connected device
   - Wait for the app to build and install

## Option 2: Use Setup Script

1. **Download OpenCV Android SDK:**
   ```bash
   # Download from https://opencv.org/releases/
   # Extract it somewhere, e.g., ~/Downloads/OpenCV-android-sdk
   ```

2. **Run the setup script:**
   ```bash
   chmod +x setup_opencv.sh
   ./setup_opencv.sh ~/Downloads/OpenCV-android-sdk
   ```

3. **Build using Gradle:**
   ```bash
   ./gradlew build
   ./gradlew installDebug
   ```

## Option 3: Use OpenCV Manager (Quick Test)

If you want to test without integrating the full SDK:

1. **Install OpenCV Manager on your device:**
   - Download from Google Play: "OpenCV Manager"
   - Install it on your device

2. **Comment out the OpenCV module dependency temporarily:**
   - Edit `app/build.gradle`
   - Comment out: `implementation project(path: ':opencv')`
   - Comment out: `include ':opencv'` in `settings.gradle`

3. **Add OpenCV dependency via Maven** (if available):
   - We'll need to use a different approach

**Note:** Option 3 requires modifying the code to work with OpenCV Manager only.

## Troubleshooting

### Device not detected:
```bash
# Check if ADB is available (if Android Studio is installed)
export PATH=$PATH:~/Library/Android/sdk/platform-tools

# Verify device connection
adb devices
```

### Build errors:
- Make sure OpenCV SDK is properly integrated
- Check that `compileSdk` version matches your Android SDK installation
- Ensure Java 8+ is installed

### OpenCV not loading:
- If using OpenCV Manager: Make sure it's installed on the device
- If using static library: Verify native libraries are in `opencv/libs/`

## Quick Check Commands

```bash
# Check if device is connected
adb devices

# Install APK directly (if already built)
adb install app/build/outputs/apk/debug/app-debug.apk

# View logs
adb logcat | grep -i "GarbageThrowDetector\|OpenCV"
```


