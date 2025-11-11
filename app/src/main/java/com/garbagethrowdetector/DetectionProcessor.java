package com.garbagethrowdetector;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class DetectionProcessor {
    private static final String TAG = "DetectionProcessor";
    
    private CascadeClassifier humanCascade;
    private CascadeClassifier objectCascade;
    private boolean cascadesLoaded = false;
    
    // For simplified object detection (using motion/contour detection)
    private Mat previousFrame;
    private boolean isFirstFrame = true;
    
    // Sensitivity parameters (0-100, default 50)
    private int humanSensitivity = 50;
    
    public DetectionProcessor() {
        // Note: For production, you would need to load trained cascade classifiers
        // For this implementation, we'll use motion detection and contour analysis
        // to detect humans and objects
        previousFrame = new Mat();
    }
    
    public void setHumanSensitivity(int sensitivity) {
        this.humanSensitivity = Math.max(0, Math.min(100, sensitivity));
    }
    
    public DetectionResult detect(Mat gray, Mat rgba) {
        DetectionResult result = new DetectionResult();
        
        if (isFirstFrame) {
            gray.copyTo(previousFrame);
            isFirstFrame = false;
            return result;
        }
        
        // Detect human using full-body detection (simplified approach)
        result = detectHumanAndObjects(gray, rgba);
        
        // Update previous frame
        gray.copyTo(previousFrame);
        
        return result;
    }
    
    private DetectionResult detectHumanAndObjects(Mat gray, Mat rgba) {
        DetectionResult result = new DetectionResult();
        
        // Frame difference for motion detection
        Mat frameDiff = new Mat();
        Core.absdiff(previousFrame, gray, frameDiff);
        
        // Threshold to create binary image (adjust based on sensitivity)
        Mat thresh = new Mat();
        int threshold = (int)(30 * (100 - humanSensitivity) / 100.0) + 10; // Lower threshold = more sensitive
        Imgproc.threshold(frameDiff, thresh, threshold, 255, Imgproc.THRESH_BINARY);
        
        // Morphological operations to clean up
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new org.opencv.core.Size(5, 5));
        Imgproc.morphologyEx(thresh, thresh, Imgproc.MORPH_CLOSE, kernel);
        Imgproc.morphologyEx(thresh, thresh, Imgproc.MORPH_OPEN, kernel);
        
        // Find contours
        java.util.List<org.opencv.core.MatOfPoint> contours = new java.util.ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        
        // Analyze contours to detect humans and objects
        // Adjust thresholds based on sensitivity (higher sensitivity = lower thresholds)
        double minArea = 5000 * (100 - humanSensitivity) / 100.0 + 2000;
        double minHumanArea = 15000 * (100 - humanSensitivity) / 100.0 + 8000;
        double minAspectRatio = 1.5 - (humanSensitivity / 200.0); // More lenient with higher sensitivity
        double maxAspectRatio = 3.5 + (humanSensitivity / 100.0);
        
        for (org.opencv.core.MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            
            if (area > minArea) {
                Rect boundingRect = Imgproc.boundingRect(contour);
                double aspectRatio = (double) boundingRect.height / boundingRect.width;
                double extent = area / (boundingRect.width * boundingRect.height);
                
                // Human detection: typically taller than wide, moderate extent
                if (aspectRatio > minAspectRatio && aspectRatio < maxAspectRatio && 
                    extent > 0.25 && area > minHumanArea) {
                    result.hasHuman = true;
                    result.humanPosition = new Point(
                        boundingRect.x + boundingRect.width / 2.0,
                        boundingRect.y + boundingRect.height / 2.0
                    );
                    result.humanRect = boundingRect;
                    
                    // Draw rectangle around human
                    Imgproc.rectangle(rgba, boundingRect.tl(), boundingRect.br(), 
                        new Scalar(0, 255, 0), 3);
                    Imgproc.putText(rgba, "Human", new Point(boundingRect.x, boundingRect.y - 10),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0, 255, 0), 2);
                }
                // Object/garbage detection: smaller, can be various shapes
                else if (area > 5000 && area < 15000 && extent > 0.4) {
                    result.hasGarbage = true;
                    result.garbagePosition = new Point(
                        boundingRect.x + boundingRect.width / 2.0,
                        boundingRect.y + boundingRect.height / 2.0
                    );
                    result.garbageRect = boundingRect;
                    
                    // Draw rectangle around object
                    Imgproc.rectangle(rgba, boundingRect.tl(), boundingRect.br(), 
                        new Scalar(255, 0, 0), 2);
                    Imgproc.putText(rgba, "Object", new Point(boundingRect.x, boundingRect.y - 10),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.6, new Scalar(255, 0, 0), 2);
                }
            }
        }
        
        // Clean up
        frameDiff.release();
        thresh.release();
        kernel.release();
        hierarchy.release();
        for (org.opencv.core.MatOfPoint contour : contours) {
            contour.release();
        }
        
        return result;
    }
    
    public void release() {
        if (previousFrame != null) {
            previousFrame.release();
        }
    }
}


