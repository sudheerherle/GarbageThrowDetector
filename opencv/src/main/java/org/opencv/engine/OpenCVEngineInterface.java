package org.opencv.engine;

import android.os.RemoteException;

/**
 * Stub interface for OpenCV Engine Service (not needed when using static libraries)
 */
public interface OpenCVEngineInterface extends android.os.IInterface {
    abstract class Stub extends android.os.Binder implements OpenCVEngineInterface {
        public static OpenCVEngineInterface asInterface(android.os.IBinder obj) {
            return null; // Not used with static libraries
        }
    }
    
    int getEngineVersion() throws RemoteException;
    String getLibPathByVersion(String version) throws RemoteException;
    boolean installVersion(String version) throws RemoteException;
    String getLibraryList(String version) throws RemoteException;
}
