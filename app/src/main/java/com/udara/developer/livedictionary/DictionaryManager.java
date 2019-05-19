package com.udara.developer.livedictionary;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

class DictionaryManager {
    private Context context;
    private HashMap<String, List<String>> langMap;
    private DictionaryInitializeListener dictionaryInitializeListener;

    DictionaryManager(Context context, DictionaryInitializeListener dictionaryInitializeListener) {
        this.context = context;
        this.dictionaryInitializeListener = dictionaryInitializeListener;
    }

    void initialize() {
        new Thread(new Runnable() {
            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                try {
                    dictionaryInitializeListener.onStart();

                    File serializedFile = new File(context.getExternalCacheDir(), "langdata.ser");
                    if (serializedFile.exists()) {
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(serializedFile));
                        ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
                        DictionaryManager.this.langMap = (HashMap) objectInputStream.readObject();
                        objectInputStream.close();
                        dictionaryInitializeListener.onComplete();
                        return;
                    }

                    long totalSize;
                    long currentRead = 0;

                    int lineSBytes = System.lineSeparator().getBytes().length;

                    File dataFile = new File(context.getExternalCacheDir(), "/langdata");
                    totalSize = dataFile.length();

                    Scanner scanner = new Scanner(new BufferedInputStream(new FileInputStream(dataFile)));
                    langMap = new HashMap<>();

                    String line = scanner.nextLine();
                    currentRead += line.getBytes().length + lineSBytes;
                    updateProgress(totalSize, currentRead);

                    String[] parts = line.split("\t");
                    String word = parts[0];
                    String mean = parts[1];

                    List<String> list = new ArrayList<>();

                    list.add(mean);
                    langMap.put(word, list);

                    while (scanner.hasNextLine()) {
                        line = scanner.nextLine();
                        currentRead += line.getBytes().length + lineSBytes;

                        parts = line.split("\t");

                        if (parts[0].equals(word)) {
                            list.add(parts[1]);
                        } else {
                            updateProgress(totalSize, currentRead);
                            word = parts[0];
                            mean = parts[1];
                            list = new ArrayList<>();
                            list.add(mean);
                            langMap.put(word, list);
                        }
                    }

                    FileOutputStream fileOutputStream = new FileOutputStream(serializedFile);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(fileOutputStream));
                    objectOutputStream.writeObject(langMap);
                    objectOutputStream.close();

                    dictionaryInitializeListener.onComplete();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    List<String> search(String word) {
        if (langMap != null) {
            return langMap.get(word);
        }
        return null;
    }

    private void updateProgress(final long totalSize, final long currentRead) {
        int percent = (int) (currentRead * 100 / totalSize);
        dictionaryInitializeListener.onProgress(percent);
    }

    interface DictionaryInitializeListener {
        void onStart();

        void onProgress(int percent);

        void onComplete();

    }
}