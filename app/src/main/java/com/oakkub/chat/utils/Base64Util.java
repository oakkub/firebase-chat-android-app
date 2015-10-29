package com.oakkub.chat.utils;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.regex.Pattern;

/**
 * Created by OaKKuB on 10/26/2015.
 */
public class Base64Util {

    public static boolean isBase64(String text) {

        final String base64Regex =
                "/^(?:[A-Z0-9+\\/]{4})*(?:[A-Z0-9+\\/]{2}==|[A-Z0-9+\\/]{3}=|[A-Z0-9+\\/]{4})$/i";
        Pattern base64Pattern = Pattern.compile(base64Regex);

        return text.contains("http") || (text.length() % 4 == 0 && base64Pattern.matcher(text).matches());
    }

    public static String bitmapToBase64(Bitmap bitmap) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        String base64 = Base64.encodeToString(byteArray, Base64.URL_SAFE);

        return base64;
    }

    public static String base64ToDataURI(String base64) {
        return String.format("data:mime/type;base64,%s", base64);
    }

}
