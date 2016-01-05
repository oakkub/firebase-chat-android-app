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

        return !(text.startsWith("http://") ||
                text.startsWith("https://") ||
                text.startsWith("file://") ||
                text.startsWith("content://") ||
                text.startsWith("asset://") ||
                text.startsWith("res://") ||
                text.startsWith("data:mime/type;base64,"));

        /*return !(text.startsWith("http://") ||
               text.startsWith("https://") ||
               text.startsWith("file://") ||
               text.startsWith("content://") ||
               text.startsWith("asset://") ||
               text.startsWith("res://")) &&
               (text.length() % 4 == 0 && base64Pattern.matcher(text).matches());*/
    }

    public static String toBase64(Bitmap bitmap) {
        return toBase64(bitmap, 100);
    }

    public static String toBase64(Bitmap bitmap, int quality) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        boolean compressSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);

        if (compressSuccess) {
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            bitmap.recycle();

            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } else {
            try {
                throw new Exception("This bitmap cannot compressed.");
            } catch (Exception e) {
                return "";
            }
        }
    }

    public static String toDataUri(String base64) {
        if (isBase64(base64)) return "data:mime/type;base64," + base64;
        else return base64;
    }

}
