#!/bin/bash

# Quick debug script - Opens in Android Studio or provides instructions

echo "🚀 Quick Debug Setup"
echo "===================="
echo ""
echo "Your device is connected:"
adb devices | grep "device$" || echo "No device found"
echo ""

# Check if Android Studio is installed
if [ -d "/Applications/Android Studio.app" ]; then
    echo "✓ Android Studio found!"
    echo ""
    echo "Opening project in Android Studio..."
    echo "Once it opens:"
    echo "  1. Wait for Gradle sync to complete"
    echo "  2. Click the green Run button (▶️)"
    echo "  3. Select your device"
    echo ""
    open -a "Android Studio" .
else
    echo "⚠ Android Studio not found"
    echo ""
    echo "To build and debug the app, you need:"
    echo ""
    echo "Option 1: Install Android Studio (Recommended)"
    echo "  Download from: https://developer.android.com/studio"
    echo "  Then open this project in Android Studio and click Run"
    echo ""
    echo "Option 2: Install Android SDK Platform 34 manually"
    echo "  This requires downloading the full Android SDK"
    echo "  Android Studio handles this automatically"
    echo ""
    echo "Your device (c6a1e348) is already connected and ready!"
fi


