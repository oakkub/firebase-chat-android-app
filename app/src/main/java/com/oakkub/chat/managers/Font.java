package com.oakkub.chat.managers;

import android.graphics.Typeface;
import android.support.v4.util.ArrayMap;

/**
 * Created by OaKKuB on 10/14/2015.
 */
public class Font {

    private static ArrayMap<String, Typeface> fontCache = new ArrayMap<>(1);
    private static Font font;

    private Font() {}

    public static Font getInstance() {
        if (font == null) font = new Font();
        return font;
    }

    public Typeface get(String fontName) {
        Typeface typeface = fontCache.get(fontName);

        if (typeface == null) {
            typeface = Typeface.createFromAsset(
                    Contextor.getInstance().getContext().getAssets(), fontName);
            fontCache.put(fontName, typeface);
        }

        return typeface;
    }

}
