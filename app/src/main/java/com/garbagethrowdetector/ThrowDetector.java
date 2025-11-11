package com.garbagethrowdetector;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ThrowDetector {
    private static final int HISTORY_SIZE = 10;
    private double throwVelocityThreshold = 50.0; // pixels per frame (adjustable)
    private double throwDistanceThreshold = 100.0; // pixels (adjustable)
    private static final int COOLDOWN_FRAMES = 30; // frames to wait after detection
    
    private List<Point> garbageHistory;
    private List<Point> humanHistory;
    private int framesSinceLastThrow;
    private boolean throwInProgress;
    
    // Sensitivity parameter (0-100, default 50)
    private int throwSensitivity = 50;
    
    public ThrowDetector() {
        garbageHistory = new ArrayList<>();
        humanHistory = new ArrayList<>();
        framesSinceLastThrow = 0;
        throwInProgress = false;
        updateThresholds();
    }
    
    public void setThrowSensitivity(int sensitivity) {
        this.throwSensitivity = Math.max(0, Math.min(100, sensitivity));
        updateThresholds();
    }
    
    private void updateThresholds() {
        // Higher sensitivity = lower thresholds (easier to detect)
        // Sensitivity 0 = very strict, Sensitivity 100 = very lenient
        double sensitivityFactor = (100 - throwSensitivity) / 100.0;
        throwVelocityThreshold = 30.0 + (sensitivityFactor * 40.0); // Range: 30-70
        throwDistanceThreshold = 50.0 + (sensitivityFactor * 100.0); // Range: 50-150
    }
    
    public boolean detectThrow(Point humanPos, Point garbagePos, Mat outputFrame) {
        framesSinceLastThrow++;
        
        // Reset after cooldown period
        if (framesSinceLastThrow > COOLDOWN_FRAMES) {
            throwInProgress = false;
        }
        
        // Add current positions to history
        garbageHistory.add(new Point(garbagePos.x, garbagePos.y));
        humanHistory.add(new Point(humanPos.x, humanPos.y));
        
        // Keep history size limited
        if (garbageHistory.size() > HISTORY_SIZE) {
            garbageHistory.remove(0);
            humanHistory.remove(0);
        }
        
        // Need at least 3 frames to detect motion
        if (garbageHistory.size() < 3 || throwInProgress) {
            return false;
        }
        
        // Calculate velocity and distance changes
        boolean throwDetected = analyzeMotion(outputFrame);
        
        if (throwDetected) {
            throwInProgress = true;
            framesSinceLastThrow = 0;
            return true;
        }
        
        return false;
    }
    
    private boolean analyzeMotion(Mat outputFrame) {
        int size = garbageHistory.size();
        
        // Get recent positions
        Point currentGarbage = garbageHistory.get(size - 1);
        Point previousGarbage = garbageHistory.get(size - 2);
        Point olderGarbage = garbageHistory.get(size - 3);
        
        Point currentHuman = humanHistory.get(size - 1);
        Point previousHuman = humanHistory.get(size - 2);
        
        // Calculate velocity (distance traveled in one frame)
        double garbageVelocity = calculateDistance(currentGarbage, previousGarbage);
        double garbageAcceleration = calculateDistance(currentGarbage, previousGarbage) - 
                                     calculateDistance(previousGarbage, olderGarbage);
        
        // Calculate distance between human and garbage
        double humanGarbageDistance = calculateDistance(currentHuman, currentGarbage);
        double previousHumanGarbageDistance = calculateDistance(previousHuman, previousGarbage);
        
        // Detect upward and forward motion (typical throwing motion)
        double verticalMotion = currentGarbage.y - previousGarbage.y; // Negative = upward
        double horizontalMotion = Math.abs(currentGarbage.x - previousGarbage.x);
        
        // Draw motion vector
        Imgproc.arrowedLine(outputFrame, previousGarbage, currentGarbage, 
            new Scalar(255, 255, 0), 3, 8, 0, 0.3);
        
        // Throw detection criteria:
        // 1. High velocity of garbage object
        // 2. Upward motion (negative Y change)
        // 3. Object moving away from human
        // 4. Significant horizontal or vertical displacement
        boolean highVelocity = garbageVelocity > throwVelocityThreshold;
        boolean upwardMotion = verticalMotion < -10; // Moving up
        boolean movingAway = humanGarbageDistance > previousHumanGarbageDistance + 20;
        boolean significantMotion = garbageVelocity > 30 && (Math.abs(horizontalMotion) > 20 || Math.abs(verticalMotion) > 20);
        
        if (highVelocity && (upwardMotion || movingAway) && significantMotion) {
            // Draw throw detection indicator
            Imgproc.circle(outputFrame, currentGarbage, 30, new Scalar(0, 0, 255), -1);
            Imgproc.putText(outputFrame, "THROW!", 
                new Point(currentGarbage.x - 40, currentGarbage.y - 40),
                Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(0, 0, 255), 3);
            
            return true;
        }
        
        return false;
    }
    
    private double calculateDistance(Point p1, Point p2) {
        double dx = p1.x - p2.x;
        double dy = p1.y - p2.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public void reset() {
        garbageHistory.clear();
        humanHistory.clear();
        throwInProgress = false;
        framesSinceLastThrow = 0;
    }
}


