#!/bin/bash

# Quick setup script for OpenCV Android SDK integration

echo "OpenCV Android SDK Setup Script"
echo "================================"
echo ""

# Check if OpenCV SDK path is provided
if [ -z "$1" ]; then
    echo "Usage: ./setup_opencv.sh <path_to_opencv_android_sdk>"
    echo ""
    echo "Example: ./setup_opencv.sh ~/Downloads/OpenCV-android-sdk"
    echo ""
    echo "If you don't have OpenCV SDK yet:"
    echo "1. Download from: https://opencv.org/releases/"
    echo "2. Extract the archive"
    echo "3. Run this script with the path to the extracted sdk folder"
    exit 1
fi

OPENCV_SDK_PATH=$1

if [ ! -d "$OPENCV_SDK_PATH" ]; then
    echo "Error: Directory $OPENCV_SDK_PATH does not exist"
    exit 1
fi

echo "Setting up OpenCV from: $OPENCV_SDK_PATH"
echo ""

# Create opencv module directories
mkdir -p opencv/src/main/java/org/opencv
mkdir -p opencv/libs

# Determine the correct SDK path
SDK_PATH=""
if [ -d "$OPENCV_SDK_PATH/sdk" ]; then
    SDK_PATH="$OPENCV_SDK_PATH/sdk"
elif [ -d "$OPENCV_SDK_PATH" ]; then
    SDK_PATH="$OPENCV_SDK_PATH"
fi

if [ -z "$SDK_PATH" ] || [ ! -d "$SDK_PATH" ]; then
    echo "Error: Could not find OpenCV SDK structure in $OPENCV_SDK_PATH"
    echo "Expected structure: <path>/sdk/..."
    exit 1
fi

echo "Using SDK path: $SDK_PATH"

# Copy Java source files
if [ -d "$SDK_PATH/src/main/java/org/opencv" ]; then
    echo "Copying Java source files..."
    cp -r "$SDK_PATH/src/main/java/org/opencv"/* opencv/src/main/java/org/opencv/ 2>/dev/null || true
    echo "✓ Java sources copied"
elif [ -d "$SDK_PATH/java/src" ]; then
    echo "Copying Java source files (alternative structure)..."
    # macOS compatible copy
    (cd "$SDK_PATH/java/src" && find . -name "*.java" | while read file; do
        mkdir -p "opencv/src/main/java/$(dirname "$file")"
        cp "$SDK_PATH/java/src/$file" "opencv/src/main/java/$file"
    done)
    # Fallback: direct copy if directory structure exists
    if [ -d "$SDK_PATH/src/main/java" ]; then
        cp -r "$SDK_PATH/src/main/java"/* opencv/src/main/ 2>/dev/null || true
    fi
    echo "✓ Java sources copied"
else
    echo "Warning: Java source directory not found at expected location."
    echo "The app may need OpenCV Manager installed on the device."
fi

# Copy native libraries
echo "Copying native libraries..."
if [ -d "$SDK_PATH/native/libs" ]; then
    cp -r "$SDK_PATH/native/libs"/* opencv/libs/ 2>/dev/null || true
    echo "✓ Native libraries copied"
    # Count copied architectures
    ARCH_COUNT=$(find opencv/libs -mindepth 1 -maxdepth 1 -type d | wc -l | tr -d ' ')
    echo "  Copied $ARCH_COUNT architecture(s): $(ls opencv/libs 2>/dev/null | tr '\n' ' ')"
else
    echo "Warning: Native libraries not found at $SDK_PATH/native/libs"
    echo "The app will use OpenCV Manager (requires internet on first run)."
fi

echo ""
echo "Setup complete!"
echo ""
echo "Next steps:"
echo "1. Connect your Android device via USB"
echo "2. Enable USB debugging on your device"
echo "3. Open the project in Android Studio"
echo "4. Build and run the app"

