package com.udara.developer.livedictionary;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

class PermissionManager {
    private Activity activity;
    private PermissionListener permissionListener;

    private String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private final int PERMISSION_REQUEST_CODE = 100;

    PermissionManager(Activity activity, PermissionListener permissionListener) {
        this.activity = activity;
        this.permissionListener = permissionListener;
    }

    /**
     * Check whether all permissions are granted
     *
     * @return true if all permissions are granted, else false
     */
    boolean checkPermissions() {
        boolean isGranted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
                break;
            }
        }
        return isGranted;
    }

    /**
     * Request for required permissions
     */
    void requestPermissions() {
        ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQUEST_CODE);
    }

    /**
     * Handle permission grant result
     *
     * @param requestCode  permission request code
     * @param grantResults permission grant result
     */
    void onPermissionResult(int requestCode, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean isGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false;
                    break;
                }
            }
            permissionListener.onPermissionResult(isGranted);
        }
    }

    interface PermissionListener {
        void onPermissionResult(boolean isGranted);
    }
}
