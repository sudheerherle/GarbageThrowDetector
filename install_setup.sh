#!/bin/bash

# Installation script for Android development tools and OpenCV setup

set -e

echo "=========================================="
echo "Garbage Throw Detector - Setup Installer"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if running on macOS
if [[ "$OSTYPE" != "darwin"* ]]; then
    echo -e "${RED}This script is designed for macOS.${NC}"
    exit 1
fi

echo "This script will help you install:"
echo "1. Android SDK Platform Tools (ADB)"
echo "2. Download and setup OpenCV Android SDK"
echo ""

# Function to check command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to download file
download_file() {
    local url=$1
    local output=$2
    
    if command_exists curl; then
        curl -L -o "$output" "$url"
    elif command_exists wget; then
        wget -O "$output" "$url"
    else
        echo -e "${RED}Error: Neither curl nor wget found. Please install one.${NC}"
        exit 1
    fi
}

# Step 1: Install Android SDK Platform Tools
echo "Step 1: Installing Android SDK Platform Tools..."
echo "------------------------------------------------"

SDK_DIR="$HOME/Library/Android/sdk"
PLATFORM_TOOLS_DIR="$SDK_DIR/platform-tools"

if [ -d "$PLATFORM_TOOLS_DIR" ] && [ -f "$PLATFORM_TOOLS_DIR/adb" ]; then
    echo -e "${GREEN}✓ Platform Tools already installed at: $PLATFORM_TOOLS_DIR${NC}"
else
    echo "Downloading Android SDK Platform Tools..."
    mkdir -p "$SDK_DIR"
    # Determine system architecture
    ARCH=$(uname -m)
    if [ "$ARCH" == "arm64" ]; then
        PLATFORM_TOOLS_URL="https://dl.google.com/android/repository/platform-tools-latest-darwin.zip"
    else
        PLATFORM_TOOLS_URL="https://dl.google.com/android/repository/platform-tools-latest-darwin.zip"
    fi
    
    TEMP_FILE="/tmp/platform-tools.zip"
    echo "Downloading from: $PLATFORM_TOOLS_URL"
    download_file "$PLATFORM_TOOLS_URL" "$TEMP_FILE"
    
    echo "Extracting..."
    unzip -q -o "$TEMP_FILE" -d "$SDK_DIR" || {
        echo -e "${RED}Error: Failed to extract platform tools. Do you have unzip installed?${NC}"
        echo "Try: brew install unzip"
        exit 1
    }
    
    rm "$TEMP_FILE"
    
    if [ -f "$PLATFORM_TOOLS_DIR/adb" ]; then
        echo -e "${GREEN}✓ Platform Tools installed successfully${NC}"
    else
        echo -e "${RED}✗ Installation failed${NC}"
        exit 1
    fi
fi

# Add to PATH in .zshrc
if [ -f "$HOME/.zshrc" ]; then
    if ! grep -q "Android/sdk/platform-tools" "$HOME/.zshrc"; then
        echo "" >> "$HOME/.zshrc"
        echo "# Android SDK Platform Tools" >> "$HOME/.zshrc"
        echo "export PATH=\$PATH:\$HOME/Library/Android/sdk/platform-tools" >> "$HOME/.zshrc"
        echo -e "${GREEN}✓ Added Android SDK to PATH in ~/.zshrc${NC}"
        echo -e "${YELLOW}  Run: source ~/.zshrc  (or restart terminal)${NC}"
    else
        echo -e "${GREEN}✓ Android SDK already in PATH${NC}"
    fi
fi

# Step 2: Download OpenCV Android SDK
echo ""
echo "Step 2: Downloading OpenCV Android SDK..."
echo "------------------------------------------"

OPENCV_VERSION="4.8.0"
OPENCV_DIR="$HOME/Downloads/OpenCV-android-sdk"
OPENCV_ZIP="$HOME/Downloads/opencv-android-sdk.zip"
OPENCV_URL="https://github.com/opencv/opencv/releases/download/${OPENCV_VERSION}/opencv-${OPENCV_VERSION}-android-sdk.zip"

if [ -d "$OPENCV_DIR" ]; then
    echo -e "${GREEN}✓ OpenCV SDK already downloaded at: $OPENCV_DIR${NC}"
    echo "  Using existing installation..."
else
    echo "Downloading OpenCV Android SDK ${OPENCV_VERSION}..."
    echo "This may take a few minutes (size: ~200MB)..."
    echo ""
    
    mkdir -p "$HOME/Downloads"
    download_file "$OPENCV_URL" "$OPENCV_ZIP"
    
    echo "Extracting OpenCV SDK..."
    unzip -q -o "$OPENCV_ZIP" -d "$HOME/Downloads" || {
        echo -e "${RED}Error: Failed to extract OpenCV SDK${NC}"
        exit 1
    }
    
    # Rename extracted folder if needed
    if [ -d "$HOME/Downloads/OpenCV-android-sdk" ]; then
        echo -e "${GREEN}✓ OpenCV SDK extracted successfully${NC}"
    elif [ -d "$HOME/Downloads/opencv-${OPENCV_VERSION}-android-sdk" ]; then
        mv "$HOME/Downloads/opencv-${OPENCV_VERSION}-android-sdk" "$OPENCV_DIR"
        echo -e "${GREEN}✓ OpenCV SDK extracted successfully${NC}"
    else
        echo -e "${YELLOW}Warning: Could not verify extraction. Check $HOME/Downloads${NC}"
    fi
    
    rm "$OPENCV_ZIP" 2>/dev/null || true
fi

# Step 3: Set up OpenCV in the project
echo ""
echo "Step 3: Integrating OpenCV into the project..."
echo "-----------------------------------------------"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

if [ -d "$OPENCV_DIR" ]; then
    echo "Running OpenCV setup script..."
    if [ -f "$SCRIPT_DIR/setup_opencv.sh" ]; then
        chmod +x "$SCRIPT_DIR/setup_opencv.sh"
        "$SCRIPT_DIR/setup_opencv.sh" "$OPENCV_DIR"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ OpenCV integrated into project${NC}"
    else
        echo -e "${YELLOW}⚠ OpenCV setup had some issues. You may need to do this manually.${NC}"
    fi
else
    echo -e "${YELLOW}⚠ OpenCV SDK not found. Please run setup manually:${NC}"
    echo "   ./setup_opencv.sh $OPENCV_DIR"
fi

# Final summary
echo ""
echo "=========================================="
echo -e "${GREEN}Installation Complete!${NC}"
echo "=========================================="
echo ""
echo "Next steps:"
echo ""
echo "1. ${YELLOW}Source your shell configuration:${NC}"
echo "   source ~/.zshrc"
echo ""
echo "2. ${YELLOW}Connect your Android device via USB${NC}"
echo "   - Enable Developer Options (tap Build Number 7 times)"
echo "   - Enable USB Debugging"
echo ""
echo "3. ${YELLOW}Verify device connection:${NC}"
echo "   ./check_device.sh"
echo ""
echo "4. ${YELLOW}Build and install the app:${NC}"
echo "   source ~/.zshrc  # If you haven't already"
echo "   ./gradlew installDebug"
echo ""
echo "Or open the project in Android Studio for easier debugging!"
echo ""

