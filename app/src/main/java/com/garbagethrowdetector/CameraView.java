package com.garbagethrowdetector;

import android.content.Context;
import android.util.AttributeSet;

import org.opencv.android.JavaCamera2View;

public class CameraView extends JavaCamera2View {

    public CameraView(Context context, int cameraId) {
        super(context, cameraId);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
