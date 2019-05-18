package com.udara.developer.livedictionary;

import android.content.Context;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class OcrEngine {
    private Context context;
    private TessBaseAPI baseAPI;
    private TessBaseAPI.ProgressNotifier progressNotifier;

    public OcrEngine(Context context) {
        this.context = context;
        initEngine();
    }

    private boolean initEngine() {
        baseAPI = new TessBaseAPI();
        baseAPI.init(context.getExternalCacheDir().getAbsolutePath(), "eng");
        return true;
    }

}
