# Garbage Throw Detector - Android App

An Android application that uses OpenCV to detect when a human throws garbage using the device's camera stream.

## Features

- Real-time camera feed processing
- Human detection using motion analysis
- Object/garbage detection
- Throw motion detection using velocity and trajectory analysis
- Visual feedback with bounding boxes and detection indicators

## Requirements

- Android SDK 24 (Android 7.0) or higher
- Target SDK 34
- OpenCV Android SDK 4.x

## Setup Instructions

### 1. OpenCV Integration

You need to integrate the OpenCV Android SDK. There are two options:

#### Option A: Using OpenCV Manager (Easier for development)
1. Download the OpenCV Android SDK from https://opencv.org/releases/
2. Extract it to a location on your machine
3. Import the OpenCV module:
   - In Android Studio: File → New → Import Module
   - Select `<OpenCV_SDK>/sdk` directory
   - Update the module name to match `opencv` in `settings.gradle`

#### Option B: Static Initialization (Recommended for production)
1. Download OpenCV Android SDK
2. Copy native libraries to `opencv/libs/` directory:
   - `armeabi-v7a/libopencv_java4.so`
   - `arm64-v8a/libopencv_java4.so`
   - `x86/libopencv_java4.so`
   - `x86_64/libopencv_java4.so`
3. Copy Java sources to `opencv/src/`

### 2. Build the Project

```bash
./gradlew build
```

### 3. Install on Device

```bash
./gradlew installDebug
```

## How It Works

### Detection Process

1. **Motion Detection**: Uses frame differencing to detect moving objects
2. **Human Detection**: Identifies humans based on:
   - Contour analysis
   - Aspect ratio (typically taller than wide)
   - Minimum area threshold
   - Shape characteristics

3. **Object Detection**: Identifies potential garbage/objects based on:
   - Size constraints (smaller than humans)
   - Motion patterns
   - Contour characteristics

4. **Throw Detection**: Analyzes motion patterns to detect throwing:
   - High velocity of object
   - Upward motion trajectory
   - Object moving away from human
   - Significant displacement in short time

### Detection Algorithm

The app uses computer vision techniques:
- **Frame Differencing**: Detects moving objects by comparing consecutive frames
- **Contour Analysis**: Identifies object boundaries
- **Motion Tracking**: Tracks object positions over time
- **Velocity Calculation**: Measures speed and direction of movement
- **Trajectory Analysis**: Determines if motion pattern indicates throwing

## Usage

1. Launch the app
2. Grant camera permission when prompted
3. Wait for OpenCV initialization
4. Press "Start Detection" to begin monitoring
5. The app will show:
   - Green rectangles around detected humans
   - Blue rectangles around detected objects
   - Yellow arrows showing motion vectors
   - Red indicators when a throw is detected

## Limitations

- The current implementation uses simplified detection methods
- For better accuracy, consider integrating:
  - Pre-trained ML models (TensorFlow Lite, ML Kit)
  - Object detection models (YOLO, MobileNet SSD)
  - Human pose estimation models
  - Deep learning-based action recognition

## Future Improvements

- [ ] Integrate TensorFlow Lite for better object detection
- [ ] Add human pose estimation for more accurate throwing detection
- [ ] Implement recording and playback features
- [ ] Add notification system for detected throws
- [ ] Improve detection accuracy with machine learning models
- [ ] Add location tracking and reporting features

## License

This project is provided as-is for educational purposes.


