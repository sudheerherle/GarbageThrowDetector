#!/bin/bash

# Script to check Android device connection and setup ADB

echo "Android Device Connection Checker"
echo "=================================="
echo ""

# Try to find ADB in common locations
ADB_PATHS=(
    "$HOME/Library/Android/sdk/platform-tools/adb"
    "$HOME/Android/Sdk/platform-tools/adb"
    "/Applications/Android Studio.app/Contents/Mac/android-studio/platform-tools/adb"
    "adb"  # In PATH
)

ADB=""
for path in "${ADB_PATHS[@]}"; do
    if [ -f "$path" ] || command -v "$path" &> /dev/null; then
        ADB="$path"
        echo "✓ Found ADB at: $path"
        break
    fi
done

if [ -z "$ADB" ]; then
    echo "⚠ ADB not found in common locations"
    echo ""
    echo "Options:"
    echo "1. Install Android Studio (recommended): https://developer.android.com/studio"
    echo "2. Install Android SDK Platform Tools manually"
    echo "3. Add Android SDK to PATH:"
    echo "   export PATH=\$PATH:\$HOME/Library/Android/sdk/platform-tools"
    echo ""
    exit 1
fi

# Check device connection
echo ""
echo "Checking for connected devices..."
echo ""

DEVICES=$($ADB devices 2>/dev/null)

if [ $? -ne 0 ]; then
    echo "Error: Could not run ADB. Make sure Android SDK is properly installed."
    exit 1
fi

DEVICE_COUNT=$(echo "$DEVICES" | grep -v "List of devices" | grep "device$" | wc -l | tr -d ' ')

if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo "⚠ No devices found!"
    echo ""
    echo "Make sure:"
    echo "1. Your Android device is connected via USB"
    echo "2. USB Debugging is enabled on your device:"
    echo "   - Go to Settings → About Phone"
    echo "   - Tap 'Build Number' 7 times"
    echo "   - Go to Settings → Developer Options"
    echo "   - Enable 'USB Debugging'"
    echo "3. Accept the USB debugging prompt on your device"
    echo ""
    echo "If device is connected, try:"
    echo "  $ADB kill-server"
    echo "  $ADB start-server"
    echo "  $ADB devices"
else
    echo "✓ Found $DEVICE_COUNT device(s):"
    echo "$DEVICES" | grep "device$"
    echo ""
    echo "Device is ready for debugging!"
    echo ""
    echo "Next steps:"
    echo "1. Set up OpenCV SDK (see DEBUG_SETUP.md)"
    echo "2. Build and install the app:"
    echo "   ./gradlew installDebug"
    echo "3. View logs:"
    echo "   $ADB logcat | grep -i GarbageThrowDetector"
fi


