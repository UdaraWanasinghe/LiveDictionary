package com.udara.developer.livedictionary;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        permissionManager = new PermissionManager(this, new PermissionManager.PermissionListener() {
            @Override
            public void onPermissionResult(boolean isGranted) {
                if (isGranted) {
                    onPermissionsGranted();
                } else {
                    permissionManager.requestPermissions();
                }
            }
        });
        if (permissionManager.checkPermissions()) {
            onPermissionsGranted();
        } else {
            permissionManager.requestPermissions();
        }
        super.onCreate(savedInstanceState);
    }

    private void onPermissionsGranted() {
        finish();
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.onPermissionResult(requestCode, grantResults);
    }
}
