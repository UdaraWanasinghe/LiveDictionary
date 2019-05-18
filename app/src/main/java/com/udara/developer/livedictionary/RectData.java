package com.udara.developer.livedictionary;

import android.graphics.Rect;

import java.util.List;

class RectData {
    Rect rect;
    String text;
    List<String> definitions;

    public RectData(Rect rect, String text, List<String> definitions) {
        this.rect = rect;
        this.text = text;
        this.definitions = definitions;
    }
}