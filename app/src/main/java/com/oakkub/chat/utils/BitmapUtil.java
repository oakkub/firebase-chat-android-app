package com.oakkub.chat.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

public class BitmapUtil {

    private static final String TAG = BitmapUtil.class.getSimpleName();

    private static final int LIMIT_WIDTH = 1280;
    private static final int LIMIT_HEIGHT = 1280;

    public static Bitmap getResized(FileDescriptor fileDescriptor, String absolutePath, boolean isThumbnail) {
        float angleRotation = getRotation(absolutePath);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
        int rawWidth = options.outWidth;
        int rawHeight = options.outHeight;
        int inSampleSize = getInSampleSize(rawWidth, rawHeight, isThumbnail);

        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;

        Bitmap scaledBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
        return angleRotation > 0 ?
                getRotatedBitmap(scaledBitmap, options, angleRotation) : scaledBitmap;
    }

    public static Bitmap getResized(File file, boolean isThumbnail) {
        String filePath = file.getAbsolutePath();
        float angleRotation = getRotation(filePath);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(filePath, options);
        int rawWidth = options.outWidth;
        int rawHeight = options.outHeight;
        int inSampleSize = getInSampleSize(rawWidth, rawHeight, isThumbnail);

/*
        Log.d(TAG, "getResized: raw width: " + rawWidth);
        Log.d(TAG, "getResized: raw height: " + rawHeight);
        Log.d(TAG, "getResized: inSampleSize: " + inSampleSize);
*/

        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;

        Bitmap scaledBitmap = BitmapFactory.decodeFile(filePath, options);
/*
        Log.d(TAG, "getRotatedBitmap: scaled width: " + options.outWidth);
        Log.d(TAG, "getRotatedBitmap: scaled height: " + options.outHeight);
*/
        return angleRotation > 0 ?
                getRotatedBitmap(scaledBitmap, options, angleRotation) : scaledBitmap;
    }

    public static int getInSampleSize(int rawWidth, int rawHeight, boolean isThumbnail) {
        int inSampleSize = 1;
        int dividedBy = isThumbnail ? 2 : 1;

        if (rawWidth >= (LIMIT_WIDTH / dividedBy) || rawHeight >= (LIMIT_HEIGHT / dividedBy)) {

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((rawWidth / inSampleSize) >= LIMIT_WIDTH / dividedBy
                    || (rawHeight / inSampleSize) >= LIMIT_HEIGHT / dividedBy) {
                inSampleSize *= 2;
            }

        }

        return inSampleSize;
    }

    private static Bitmap getRotatedBitmap(Bitmap bitmapToBeRotated, BitmapFactory.Options options, float angleRotation) {
        int resultWidth = options.outWidth;
        int resultHeight = options.outHeight;

        Matrix matrix = new Matrix();
        matrix.setRotate(angleRotation, resultWidth / 2, resultHeight / 2);

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapToBeRotated, 0, 0,
                resultWidth, resultHeight, matrix, true);
        bitmapToBeRotated.recycle();

        Log.d(TAG, "getRotatedBitmap: rotated width: " + rotatedBitmap.getWidth());
        Log.d(TAG, "getRotatedBitmap: rotated height: " + rotatedBitmap.getHeight());

        return rotatedBitmap;
    }

    public static float getRotation(String filePath) {
        ExifInterface exif;

        try {
            exif = new ExifInterface(filePath);
        } catch (IOException e) {
            Log.e(TAG, "error ExifInterface: " + e.getMessage());
            return 0;
        }

        return getExifRotation(
                exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL));
    }

    private static int getExifRotation(int exifOrientation) {
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return ExifInterface.ORIENTATION_UNDEFINED;
        }
    }

    public static String getImageRatio(float width, float height) {
        float ratio = width / height;
        Log.d(TAG, "getImageRatio: w:" + width);
        Log.d(TAG, "getImageRatio: h:" + height);
        Log.d(TAG, "getImageRatio: r:" + ratio);
        String properties = "";
        if (ratio > 0.5f && ratio < 0.6f) {
            // 9:16 portrait
            properties += "9:16";
        } else if (ratio >= 1.7f && ratio < 1.8f) {
            // 16:9 wide screen
            properties += "16:9";
        } else if (ratio >= 1.2f && ratio < 1.3f) {
            // 5:4
            properties += "5:4";
        } else if ((ratio >= 1.3f && ratio < 1.4f) || (ratio >= 0.7f && ratio < 0.8f)){
            // default square (4:3)
            properties += "4:3";
        } else if (width > height) {
            // assume it's a portrait (16:9)
            properties += "16:9";
        } else if (height > width){
            // assume it's a landscape (9:16)
            properties += "9:16";
        }

        Log.d(TAG, "getImageRatio: " + properties);

        return properties;
    }

}
