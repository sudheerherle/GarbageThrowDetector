# Fix for libc++_shared.so Missing Error

The app needs `libc++_shared.so` which is part of the Android NDK. To fix this:

## Option 1: Install Android NDK and Copy libc++_shared.so

1. Install Android Studio and Android NDK through SDK Manager
2. Find libc++_shared.so in:
   `~/Library/Android/sdk/ndk/<version>/sources/cxx-stl/llvm-libc++/libs/arm64-v8a/libc++_shared.so`
3. Copy it to:
   ```
   app/src/main/jniLibs/arm64-v8a/libc++_shared.so
   app/src/main/jniLibs/armeabi-v7a/libc++_shared.so
   ```
4. Rebuild: `./gradlew clean assembleDebug installDebug`

## Option 2: Use OpenCV Manager (Alternative)

Install OpenCV Manager from Google Play Store on your device. The app will then use dynamic loading.

## Quick Fix Script

If you have Android Studio installed with NDK:

```bash
NDK_VERSION=$(ls ~/Library/Android/sdk/ndk 2>/dev/null | head -1)
if [ -n "$NDK_VERSION" ]; then
    cp ~/Library/Android/sdk/ndk/$NDK_VERSION/sources/cxx-stl/llvm-libc++/libs/arm64-v8a/libc++_shared.so app/src/main/jniLibs/arm64-v8a/
    cp ~/Library/Android/sdk/ndk/$NDK_VERSION/sources/cxx-stl/llvm-libc++/libs/armeabi-v7a/libc++_shared.so app/src/main/jniLibs/armeabi-v7a/
    echo "✓ Copied libc++_shared.so from NDK"
else
    echo "NDK not found. Please install Android NDK via Android Studio SDK Manager"
fi
```

