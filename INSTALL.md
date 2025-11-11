# Installation Guide

## Quick Install (Automated)

Run the automated installation script:

```bash
./install_setup.sh
```

This will:
1. Install Android SDK Platform Tools (ADB)
2. Download OpenCV Android SDK
3. Integrate OpenCV into the project
4. Set up PATH for ADB

## Manual Installation

### 1. Install Android SDK Platform Tools

#### Option A: Using Homebrew (macOS)
```bash
brew install android-platform-tools
```

#### Option B: Download Manually
1. Download from: https://developer.android.com/tools/releases/platform-tools
2. Extract to: `~/Library/Android/sdk/`
3. Add to PATH:
```bash
echo 'export PATH=$PATH:$HOME/Library/Android/sdk/platform-tools' >> ~/.zshrc
source ~/.zshrc
```

### 2. Download OpenCV Android SDK

1. Download from: https://opencv.org/releases/
   - Or direct link: https://github.com/opencv/opencv/releases/latest
   - Look for `opencv-X.X.X-android-sdk.zip`

2. Extract the downloaded ZIP file

3. Run the setup script:
```bash
./setup_opencv.sh ~/Downloads/OpenCV-android-sdk
```

### 3. Verify Installation

Check if ADB is working:
```bash
./check_device.sh
```

## Troubleshooting

### ADB not found after installation
```bash
source ~/.zshrc
# or restart your terminal
```

### OpenCV setup fails
- Make sure you downloaded the full Android SDK (not just the source)
- Check that the path to OpenCV SDK is correct
- Verify the extracted folder contains `sdk/` subdirectory

### Device not detected
- Make sure USB debugging is enabled on your Android device
- Try different USB cable/port
- On some devices, you need to select "File Transfer" mode in USB options


