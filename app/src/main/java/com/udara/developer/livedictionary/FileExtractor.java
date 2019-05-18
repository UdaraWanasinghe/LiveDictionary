package com.udara.developer.livedictionary;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class FileExtractor {
    private Context context;

    FileExtractor(Context context) {
        this.context = context;
    }

    /**
     * Check for availability of data files in app cache directory
     *
     * @return true if data files are available, else false
     */
    boolean checkFiles() {
        File file1 = new File(context.getExternalCacheDir(), "/langdata");
        File file2 = new File(context.getExternalCacheDir(), "/tessdata/eng.traineddata");
        return file1.exists() && file2.exists();
    }

    /**
     * Extract data.zip file in the assets folder into app cache directory
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void extractFiles(final FileExtractListener fileExtractListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (context.getExternalCacheDir() == null) {
                    onComplete(fileExtractListener, false, "Cache directory is not available");
                    return;
                }
                String path = context.getExternalCacheDir().getAbsolutePath();
                try {
                    String filename;
                    InputStream is = context.getAssets().open("data.zip");
                    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
                    ZipEntry ze;
                    byte[] buffer = new byte[1024];
                    int count;

                    while ((ze = zis.getNextEntry()) != null) {
                        filename = ze.getName();
                        if (ze.isDirectory()) {
                            File fmd = new File(path, filename);
                            fmd.mkdirs();
                            continue;
                        }

                        File file = new File(path, filename);
                        if (!file.exists()) {
                            file.getParentFile().mkdirs();
                        }

                        FileOutputStream fout = new FileOutputStream(file);

                        while ((count = zis.read(buffer)) != -1) {
                            fout.write(buffer, 0, count);
                        }

                        fout.close();
                        zis.closeEntry();
                    }

                    zis.close();
                    onComplete(fileExtractListener, true, "Successfully extracted");
                } catch (IOException e) {
                    e.printStackTrace();
                    onComplete(fileExtractListener, false, e.getMessage());
                }
            }
        }).start();

    }

    private void onComplete(final FileExtractListener fileExtractListener, final boolean isSuccess, final String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                fileExtractListener.onComplete(isSuccess, message);
            }
        });
    }

    interface FileExtractListener {
        void onComplete(boolean isSuccess, String message);
    }
}
