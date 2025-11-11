package com.garbagethrowdetector;

import org.opencv.core.Point;
import org.opencv.core.Rect;

public class DetectionResult {
    public boolean hasHuman = false;
    public boolean hasGarbage = false;
    public Point humanPosition = null;
    public Point garbagePosition = null;
    public Rect humanRect = null;
    public Rect garbageRect = null;
}


