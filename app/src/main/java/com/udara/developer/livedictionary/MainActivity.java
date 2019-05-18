package com.udara.developer.livedictionary;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Dimension;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;

import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    TextureView cameraView;
    TextView textView;
    Button button;
    CropView cropView;
    ImageView imageView;

    TessBaseAPI tessBaseAPI;
    DictionaryManager dictionaryManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        cameraView = findViewById(R.id.camera_view);
        button = findViewById(R.id.button);
        cropView = findViewById(R.id.crop_view);
        imageView = findViewById(R.id.image_view);

        FileExtractor fileExtractor = new FileExtractor(this);
        if (fileExtractor.checkFiles()) {

        } else {
            final ProgressDialog progressDialog = showProgressDialog("Extracting resources");
            fileExtractor.extractFiles(new FileExtractor.FileExtractListener() {
                @Override
                public void onComplete(boolean isSuccess, String message) {
                    progressDialog.dismiss();
                    if (!isSuccess) {
                        showAlertDialog("Error", message);
                    }
                }
            });
        }

//        tessBaseAPI = new TessBaseAPI();
//
//        initCameraX();
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
////                Log.d("MY_APP", "Loading...");
//                tessBaseAPI.init(Environment.getExternalStorageDirectory().getAbsolutePath(), "eng");
////                Log.d("MY_APP", "Complete...");
//                dictionaryManager = new DictionaryManager(MainActivity.this);
//            }
//        }).start();
    }

    boolean isEngineReady = true;

    private void initCameraX() {
        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setTargetResolution(new Size(1280, 1920))
                .setTargetAspectRatio(new Rational(16, 9))
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                .build();
        Preview preview = new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                cameraView.setSurfaceTexture(output.getSurfaceTexture());
            }
        });

        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder()
                .setTargetResolution(new Size(1280, 1920))
                .setTargetAspectRatio(new Rational(16, 9))
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                .build();
        final ImageCapture imageCapture = new ImageCapture(imageCaptureConfig);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "On image capture...", Toast.LENGTH_SHORT).show();
            }
        });
        CameraX.bindToLifecycle(this, preview, imageCapture);

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (isEngineReady) {
                    isEngineReady = false;
                    final Bitmap bitmap = cameraView.getBitmap();
                    if (bitmap != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                float[] dimens = cropView.getCropViewDimen();
                                final Bitmap newBitmap = Bitmap.createBitmap(
                                        bitmap,
                                        (int) dimens[4],
                                        (int) dimens[5],
                                        (int) dimens[2],
                                        (int) dimens[3]
                                );
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        imageView.setImageBitmap(newBitmap);
                                    }
                                });
//                                Log.d("MY_APP", "Recognizing...");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Recognizing...", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                tessBaseAPI.setImage(newBitmap);
                                final String text = tessBaseAPI.getUTF8Text();
                                final ResultIterator resultIterator = tessBaseAPI.getResultIterator();
                                Log.d("MY_APP", "Dic start");
                                ArrayList<RectData> rectDataList = new ArrayList<>();
                                if (resultIterator != null) {
                                    resultIterator.begin();
                                    do {
                                        String textStr = resultIterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD);
                                        Rect rect = resultIterator.getBoundingRect(TessBaseAPI.PageIteratorLevel.RIL_WORD);
                                        rectDataList.add(new RectData(rect, textStr, dictionaryManager == null ? null : dictionaryManager.search(textStr)));
                                        Log.d("MY_APP", "Tess: " + textStr);
                                    } while (resultIterator.next(TessBaseAPI.PageIteratorLevel.RIL_WORD));

                                    cropView.drawBoundingBoxes(rectDataList);
                                }
                                Log.d("MY_APP", "Dic stop");
                                tessBaseAPI.clear();
                                bitmap.recycle();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        textView.setText(text);
                                        Toast.makeText(MainActivity.this, "Done...", Toast.LENGTH_SHORT).show();
//                                        Log.d("MY_APP", "Done...");
                                    }
                                });
                                isEngineReady = true;
                            }
                        }).start();
                    } else {
                        isEngineReady = true;
                        Log.d("MY_APP", "Bitmap null");
                    }
                }
            }
        }, 3000, 1000, TimeUnit.MILLISECONDS);
    }


    private ProgressDialog showProgressDialog(String message) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
        TextView textView = progressDialog.getWindow().findViewById(android.R.id.message);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(Dimension.SP, 14);
        return progressDialog;
    }

    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
