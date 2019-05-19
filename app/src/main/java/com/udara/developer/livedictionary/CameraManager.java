package com.udara.developer.livedictionary;

import android.util.Size;
import android.view.TextureView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;

class CameraManager {
    private AppCompatActivity activity;
    private TextureView cameraView;

    CameraManager(AppCompatActivity activity, TextureView cameraView) {
        this.activity = activity;
        this.cameraView = cameraView;
    }

    void initCamera() {
        CameraX.unbindAll();
        PreviewConfig previewConfig = new PreviewConfig.Builder().build();
        Preview preview = new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                cameraView.setSurfaceTexture(output.getSurfaceTexture());
            }
        });

        CameraX.bindToLifecycle(activity, preview);
    }
}
