package com.udara.developer.livedictionary;

import android.content.Context;

import com.googlecode.tesseract.android.TessBaseAPI;

class OcrEngine {
    private Context context;
    private TessBaseAPI baseAPI;
    private TessBaseAPI.ProgressNotifier progressNotifier;
    private OcrEngineListener ocrEngineListener;

    OcrEngine(Context context, TessBaseAPI.ProgressNotifier progressNotifier, OcrEngineListener ocrEngineListener) {
        this.context = context;
        this.progressNotifier = progressNotifier;
        this.ocrEngineListener = ocrEngineListener;
        initEngine();
    }

    private void initEngine() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ocrEngineListener.onInitStart();
                baseAPI = new TessBaseAPI(progressNotifier);
                if (context.getExternalCacheDir() == null) {
                    ocrEngineListener.onInitError();
                    return;
                }
                baseAPI.init(context.getExternalCacheDir().getAbsolutePath(), "eng");
                ocrEngineListener.onInitComplete();
            }
        }).start();
    }

    interface OcrEngineListener {
        void onInitStart();

        void onInitComplete();

        void onInitError();
    }

}
