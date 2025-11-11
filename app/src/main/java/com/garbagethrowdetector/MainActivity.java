package com.garbagethrowdetector;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int STORAGE_PERMISSION_REQUEST = 101;

    private CameraView mOpenCvCameraView;
    private TextView statusTextView;
    private TextView litterCountText;
    private Button startButton;
    private SeekBar humanSensitivitySlider;
    private SeekBar throwSensitivitySlider;
    private TextView humanSensitivityValue;
    private TextView throwSensitivityValue;
    private boolean isProcessing = false;
    
    // For image capture
    private Mat currentFrameForCapture;
    private boolean shouldCaptureImage = false;
    
    // Litter count
    private int litterCount = 0;

    private DetectionProcessor detectionProcessor;
    private ThrowDetector throwDetector;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.d(TAG, "OpenCV loaded successfully");
                    Log.d(TAG, "Enabling camera view...");
                    // Check camera permission before enabling
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "Camera permission granted, enabling view");
                        // Ensure camera permission is set for Camera2
                        mOpenCvCameraView.setCameraPermissionGranted();
                        mOpenCvCameraView.enableView();
                    } else {
                        Log.e(TAG, "Camera permission not granted!");
                        runOnUiThread(() -> {
                            statusTextView.setText("Camera permission required!");
                        });
                    }
                    detectionProcessor = new DetectionProcessor();
                    throwDetector = new ThrowDetector();
                    // Initialize sensitivity from sliders
                    detectionProcessor.setHumanSensitivity(humanSensitivitySlider.getProgress());
                    throwDetector.setThrowSensitivity(throwSensitivitySlider.getProgress());
                    statusTextView.setText("OpenCV initialized. Ready to detect.");
                    startButton.setEnabled(true);
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = findViewById(R.id.camera_view);
        statusTextView = findViewById(R.id.status_text);
        litterCountText = findViewById(R.id.litter_count_text);
        startButton = findViewById(R.id.start_button);
        humanSensitivitySlider = findViewById(R.id.human_sensitivity_slider);
        throwSensitivitySlider = findViewById(R.id.throw_sensitivity_slider);
        humanSensitivityValue = findViewById(R.id.human_sensitivity_value);
        throwSensitivityValue = findViewById(R.id.throw_sensitivity_value);
        
        // Initialize litter count display
        updateLitterCount();

        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        // Use back camera (better for detecting people throwing garbage)
        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isProcessing = !isProcessing;
                if (isProcessing) {
                    startButton.setText("Stop Detection");
                    statusTextView.setText("Detecting...");
                } else {
                    startButton.setText("Start Detection");
                    statusTextView.setText("Paused");
                    if (throwDetector != null) {
                        throwDetector.reset();
                    }
                }
            }
        });
        
        // Human sensitivity slider
        humanSensitivitySlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (detectionProcessor != null) {
                    detectionProcessor.setHumanSensitivity(progress);
                }
                updateSensitivityLabel(humanSensitivityValue, progress, "Human");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Throw sensitivity slider
        throwSensitivitySlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (throwDetector != null) {
                    throwDetector.setThrowSensitivity(progress);
                }
                updateSensitivityLabel(throwSensitivityValue, progress, "Throw");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        checkCameraPermission();
    }

    private void updateSensitivityLabel(TextView textView, int value, String type) {
        String level;
        if (value < 25) {
            level = "Very Low";
        } else if (value < 50) {
            level = "Low";
        } else if (value < 75) {
            level = "Medium";
        } else {
            level = "High";
        }
        textView.setText(level + " (" + value + "%)");
    }
    
    private void updateLitterCount() {
        runOnUiThread(() -> {
            litterCountText.setText(String.valueOf(litterCount));
        });
    }
    
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            // For Camera2 API, we need to explicitly grant camera permission
            mOpenCvCameraView.setCameraPermissionGranted();
            checkStoragePermission();
            initializeOpenCV();
        }
    }
    
    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        STORAGE_PERMISSION_REQUEST);
            }
        } else {
            // Android 12 and below use WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // For Camera2 API, we need to explicitly grant camera permission
                mOpenCvCameraView.setCameraPermissionGranted();
                checkStoragePermission();
                initializeOpenCV();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show();
                finish();
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission needed to save images", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeOpenCV() {
        // Use static OpenCV library bundled with the app (no OpenCV Manager needed)
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV library loaded successfully from app package");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.e(TAG, "OpenCV initialization failed! Native libraries may not be included.");
            runOnUiThread(() -> {
                statusTextView.setText("Error: OpenCV initialization failed!");
                Toast.makeText(this, "OpenCV failed to initialize. Native libraries may be missing.", Toast.LENGTH_LONG).show();
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reinitialize OpenCV if needed (using static libraries)
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV ready on resume");
            if (mOpenCvCameraView != null) {
                mOpenCvCameraView.enableView();
            }
        } else {
            Log.w(TAG, "OpenCV not ready on resume, will retry");
            initializeOpenCV();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG, "Camera view started: " + width + "x" + height);
        runOnUiThread(() -> {
            statusTextView.setText("Camera active: " + width + "x" + height);
        });
    }

    @Override
    public void onCameraViewStopped() {
        Log.d(TAG, "Camera view stopped");
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();
        Mat gray = inputFrame.gray();
        
        // Rotate the frame 90 degrees clockwise to fix orientation in portrait mode
        Mat rotated = new Mat();
        Mat M = Imgproc.getRotationMatrix2D(new Point(rgba.cols() / 2.0, rgba.rows() / 2.0), -90, 1.0);
        Imgproc.warpAffine(rgba, rotated, M, new Size(rgba.rows(), rgba.cols()));
        
        // Also rotate gray frame for processing
        Mat grayRotated = new Mat();
        Imgproc.warpAffine(gray, grayRotated, M, new Size(gray.rows(), gray.cols()));
        gray = grayRotated;

        if (isProcessing && detectionProcessor != null && throwDetector != null) {
            // Detect objects (human and garbage)
            DetectionResult result = detectionProcessor.detect(gray, rotated);
            
            // Check for throwing motion
            if (result.hasHuman && result.hasGarbage) {
                boolean throwingDetected = throwDetector.detectThrow(
                    result.humanPosition,
                    result.garbagePosition,
                    rotated
                );

                if (throwingDetected) {
                    // Capture image when throw is detected
                    if (!shouldCaptureImage) {
                        shouldCaptureImage = true;
                        litterCount++; // Increment litter count
                        updateLitterCount();
                        rotated.copyTo(currentFrameForCapture = new Mat());
                        saveImageToGallery(currentFrameForCapture);
                    }
                    
                    runOnUiThread(() -> {
                        statusTextView.setText("⚠️ GARBAGE THROW DETECTED! Total: " + litterCount);
                        statusTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
                    });
                } else {
                    shouldCaptureImage = false; // Reset flag when throw is no longer detected
                    runOnUiThread(() -> {
                        statusTextView.setText("Monitoring: Human and object detected");
                        statusTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
                    });
                }
            } else if (result.hasHuman) {
                shouldCaptureImage = false;
                runOnUiThread(() -> {
                    statusTextView.setText("Human detected. Waiting for object...");
                    statusTextView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark, null));
                });
            } else {
                shouldCaptureImage = false;
                runOnUiThread(() -> {
                    statusTextView.setText("Detecting...");
                    statusTextView.setTextColor(getResources().getColor(android.R.color.black, null));
                });
            }
        }

        return rotated;
    }
    
    private void saveImageToGallery(Mat frame) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Convert Mat to Bitmap
                    Bitmap bitmap = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(frame, bitmap);
                    
                    // Save to gallery
                    String filename = "Litter_Detection_" + 
                        new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg";
                    
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/GarbageThrowDetector");
                        values.put(MediaStore.Images.Media.IS_PENDING, 1);
                    }
                    
                    Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    
                    if (uri != null) {
                        OutputStream outputStream = getContentResolver().openOutputStream(uri);
                        if (outputStream != null) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                            outputStream.close();
                            
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                values.clear();
                                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                                getContentResolver().update(uri, values, null, null);
                            }
                            
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Image saved: " + filename, Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Image saved to gallery: " + filename);
                            });
                        }
                    }
                    
                    bitmap.recycle();
                } catch (Exception e) {
                    Log.e(TAG, "Error saving image: " + e.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Failed to save image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }
}



