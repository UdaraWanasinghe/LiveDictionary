package com.udara.developer.livedictionary;


import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

class DictionaryManager {
    private HashMap<String, List<String>> langMap;

    public DictionaryManager(Context context) {
        try {
            initialize(context);
            Log.d("MY_APP", "Dic " + search("article").get(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initialize(Context context) throws IOException {
        Scanner scanner = new Scanner(context.getAssets().open("langdata"));
        HashMap<String, List<String>> langMap = new HashMap<>();

        String[] parts = scanner.nextLine().split("\t");
        String word = parts[0];
        String mean = parts[1];
        List<String> list = new ArrayList<>();
        list.add(mean);
        langMap.put(word, list);
        while (scanner.hasNextLine()) {
            parts = scanner.nextLine().split("\t");
            if (parts[0].equals(word)) {
                list.add(parts[1]);
            } else {
                word = parts[0];
                mean = parts[1];
                list = new ArrayList<>();
                list.add(mean);
                langMap.put(word, list);
            }
        }
        this.langMap = langMap;
    }

    public List<String> search(String word) {
        if (langMap != null) {
            return langMap.get(word);
        }
        return null;
    }
}