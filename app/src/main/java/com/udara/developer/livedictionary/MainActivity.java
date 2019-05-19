package com.udara.developer.livedictionary;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Dimension;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    TextureView cameraView;
    CropView cropView;
    ProgressBar progressBar;
    TextView textViewStatus;

    TessBaseAPI tessBaseAPI;

    CameraManager cameraManager;
    FileExtractor fileExtractor;
    DictionaryManager dictionaryManager;
    OcrEngine ocrEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = findViewById(R.id.camera_view);
        cropView = findViewById(R.id.crop_view);
        progressBar = findViewById(R.id.progress_bar);
        textViewStatus = findViewById(R.id.textView_status);

        cameraManager = new CameraManager(this, cameraView);
        fileExtractor = new FileExtractor(this);

        initialize();
    }

    private void initialize() {
        Log.d("MY_APP", "Starting camera");
        // initialize camera
        cameraView.post(new Runnable() {
            @Override
            public void run() {
                cameraManager.initCamera();
                onResume();
            }
        });

        // check ocr data
        if (fileExtractor.checkFiles()) {
            Log.d("MY_APP", "Data already extracted");
            progressBar.setIndeterminate(false);
            progressBar.setProgress(0);
            afterOcrDataAvailable();
        } else {
            Log.d("MY_APP", "Extracting data");
            textViewStatus.setText("Extracting data");
            progressBar.setIndeterminate(true);
            fileExtractor.extractFiles(new FileExtractor.FileExtractListener() {
                @Override
                public void onExtractComplete(boolean isSuccess, String message) {
                    if (isSuccess) {
                        Log.d("MY_APP", "Extraction complete");
                        progressBar.setIndeterminate(false);
                        progressBar.setProgress(100);
                        afterOcrDataAvailable();
                    } else {
                        Log.d("MY_APP", "Extraction failed");
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Error")
                                .setMessage(message)
                                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .setCancelable(false)
                                .show();
                    }
                }
            });
        }
    }

    private void afterOcrDataAvailable() {
        dictionaryManager = new DictionaryManager(
                this,
                new DictionaryManager.DictionaryInitializeListener() {
                    @Override
                    public void onStart() {
                        Log.d("MY_APP", "Loading dictionary");
                        textViewStatus.setText("Loading data");
                        progressBar.setIndeterminate(true);
                    }

                    @Override
                    public void onComplete() {
                        Log.d("MY_APP", "Dictionary loaded");
                        textViewStatus.setText("Data loaded");
                        onDictionaryLoaded();
                    }

                    @Override
                    public void onProgress(int percent) {
                        progressBar.setIndeterminate(false);
                        progressBar.setProgress(percent);
                    }
                });
        dictionaryManager.initialize();
    }

    private void onDictionaryLoaded() {
        ocrEngine = new OcrEngine(
                this,
                new TessBaseAPI.ProgressNotifier() {
                    @Override
                    public void onProgressValues(final TessBaseAPI.ProgressValues progressValues) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(progressValues.getPercent());
                            }
                        });
                    }
                },
                new OcrEngine.OcrEngineListener() {
                    @Override
                    public void onInitStart() {
                        progressBar.setIndeterminate(true);
                        textViewStatus.setText("Starting OCR engine");
                    }

                    @Override
                    public void onInitComplete() {
                        progressBar.setIndeterminate(false);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            progressBar.setProgress(100, true);
                        } else {
                            progressBar.setProgress(100);
                        }
                        textViewStatus.setText("OCR engine ready");
                    }

                    @Override
                    public void onInitError() {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Error")
                                .setMessage("Failed to start OCR engine")
                                .setCancelable(false)
                                .setPositiveButton(
                                        "Close",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        }
                                ).show();
                    }
                }
        );
    }

    boolean isEngineReady = true;

    private void initCameraX() {

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
