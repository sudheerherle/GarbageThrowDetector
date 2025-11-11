# Quick Start Guide

## ✅ Installation Status

All required tools have been installed:
- ✓ Android SDK Platform Tools (ADB)
- ✓ OpenCV Android SDK (v4.8.0)
- ✓ OpenCV integrated into project

## 🚀 Ready to Debug!

### Step 1: Connect Your Android Device

1. Connect your Android phone via USB to your Mac
2. Enable Developer Options on your device:
   - Go to **Settings → About Phone**
   - Tap **"Build Number"** 7 times
   - You'll see "You are now a developer!"
3. Enable USB Debugging:
   - Go to **Settings → Developer Options**
   - Enable **"USB Debugging"**
   - Accept any security prompts

### Step 2: Verify Device Connection

Run:
```bash
source ~/.zshrc  # Reload PATH
./check_device.sh
```

You should see your device listed. If not, try:
```bash
$HOME/Library/Android/sdk/platform-tools/adb kill-server
$HOME/Library/Android/sdk/platform-tools/adb start-server
$HOME/Library/Android/sdk/platform-tools/adb devices
```

### Step 3: Build and Install the App

#### Option A: Using Gradle (Command Line)

```bash
# Make sure PATH is set
export PATH=$PATH:$HOME/Library/Android/sdk/platform-tools

# Build and install
./gradlew installDebug
```

#### Option B: Using Android Studio (Recommended)

1. Open Android Studio
2. File → Open → Select this project folder
3. Wait for Gradle sync to complete
4. Click the green "Run" button (▶️) or press `Shift+F10`
5. Select your connected device from the list

### Step 4: Test the App

1. The app should launch on your device
2. Grant camera permission when prompted
3. Wait for OpenCV to initialize
4. Click "Start Detection" to begin monitoring
5. Try throwing an object in front of the camera!

## 📱 Using the App

- **Start Detection**: Begins monitoring for throwing actions
- **Stop Detection**: Pauses detection
- **Status Display**: Shows current detection state:
  - "OpenCV initialized. Ready to detect." - Ready to start
  - "Human detected. Waiting for object..." - Detected a person
  - "Monitoring: Human and object detected" - Both detected, analyzing motion
  - "⚠️ GARBAGE THROW DETECTED!" - Throw action detected!

## 🔍 Viewing Logs

To see detailed logs while debugging:

```bash
$HOME/Library/Android/sdk/platform-tools/adb logcat | grep -i "GarbageThrowDetector\|OpenCV"
```

Or filter by tag:
```bash
$HOME/Library/Android/sdk/platform-tools/adb logcat -s GarbageThrowDetector OpenCV
```

## 🐛 Troubleshooting

### App crashes on launch
- Check logs: `adb logcat | grep -i error`
- Make sure camera permission was granted
- Verify OpenCV libraries are included (check `opencv/libs/`)

### Camera not working
- Ensure camera permission is granted in Settings
- Check if another app is using the camera
- Restart the app

### OpenCV not loading
- Check logs for OpenCV initialization errors
- Verify native libraries exist in `opencv/libs/`
- Try reinstalling: `./setup_opencv.sh ~/Downloads/OpenCV-android-sdk`

### Device not detected
- Check USB cable connection
- Try different USB port
- Enable "File Transfer" mode on device
- Run: `adb kill-server && adb start-server`

## 📝 Next Steps

The app is ready to use! The detection algorithm uses:
- Motion detection via frame differencing
- Contour analysis for object identification
- Velocity and trajectory analysis for throw detection

For production use, consider integrating:
- TensorFlow Lite for better object detection
- Human pose estimation models
- Machine learning-based action recognition


