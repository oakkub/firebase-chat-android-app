package com.oakkub.chat.managers;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;

/**
 * Created by OaKKuB on 10/14/2015.
 */
public class Font {

    private static Hashtable<String, Typeface> fontCache = new Hashtable<>(1);

    public static Typeface get(Context context, String fontName) {

        Typeface typeface = fontCache.get(fontName);

        if (typeface == null) {

            typeface = Typeface.createFromAsset(context.getAssets(), fontName);

            fontCache.put(fontName, typeface);
        }

        return typeface;
    }

}
