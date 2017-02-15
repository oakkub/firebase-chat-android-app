package com.oakkub.chat.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

public class BitmapUtils {

    private static final String TAG = BitmapUtils.class.getSimpleName();

    public static Bitmap getBitmap(FileDescriptor fileDescriptor, String filePath) {
        return getResizedBitmap(fileDescriptor, filePath, -1, -1);
    }

    public static Bitmap getBitmap(File file) {
        return getResizedBitmap(file, -1, -1);
    }

    public static Bitmap getResizedBitmap(FileDescriptor fileDescriptor,
                                          String filePath,
                                          int targetWidth,
                                          int targetHeight) {
        BitmapFactory.Options options = getDefaultOptions();

        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
        shouldApplyInSampleSize(targetWidth, targetHeight, options);

        float orientation = getOrientation(filePath);
        Bitmap resultBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
        return rotateBitmap(resultBitmap, orientation);
    }

    public static Bitmap getResizedBitmap(File file,
                                          int targetWidth,
                                          int targetHeight) {
        String filePath = file.getAbsolutePath();
        BitmapFactory.Options options = getDefaultOptions();

        BitmapFactory.decodeFile(filePath, options);
        shouldApplyInSampleSize(targetWidth, targetHeight, options);

        float orientation = getOrientation(filePath);
        Bitmap resultBitmap = BitmapFactory.decodeFile(filePath, options);
        return rotateBitmap(resultBitmap, orientation);
    }

    private static void shouldApplyInSampleSize(int targetWidth,
                                                int targetHeight,
                                                BitmapFactory.Options options) {
        boolean shouldResized = (targetWidth == -1) || (targetHeight == -1);

        if (shouldResized) {
            applyInSampleSize(options, targetWidth, targetHeight);
        } else {
            options = null;
        }
    }

    private static Bitmap rotateBitmap(Bitmap targetBitmap, float orientation) {
        if (orientation > 0) return rotate(targetBitmap, orientation);
        else return targetBitmap;
    }

    private static void applyInSampleSize(BitmapFactory.Options options,
                                          int targetWidth,
                                          int targetHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = getInSampleSize(width, height, targetWidth, targetHeight);
        updateInSampleSize(options, inSampleSize);
    }

    private static void updateInSampleSize(BitmapFactory.Options options, int inSampleSize) {
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;
    }

    public static int getInSampleSize(int originalWidth, int originalHeight, int targetWidth, int targetHeight) {
        int inSampleSize = 1;

        if (originalWidth >= targetWidth || originalHeight >= targetHeight) {

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((originalWidth / inSampleSize) >= targetWidth ||
                    (originalHeight / inSampleSize) >= targetHeight) {
                inSampleSize *= 2;
            }

        }

        return inSampleSize;
    }

    public static Bitmap rotate(Bitmap targetBitmap, float orientation) {
        int width = targetBitmap.getWidth();
        int height = targetBitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.setRotate(orientation, width / 2, height / 2);

        Bitmap resultBitmap = Bitmap.createBitmap(targetBitmap, 0, 0, width, height, matrix, true);
        targetBitmap.recycle();

        return resultBitmap;
    }

    private static BitmapFactory.Options getDefaultOptions() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        return options;
    }

    public static float getOrientation(Uri uri) {
        return getOrientation(uri.getPath());
    }

    public static float getOrientation(String filePath) {
        ExifInterface exif;

        try {
            exif = new ExifInterface(filePath);
        } catch (IOException e) {
            Log.e(TAG, "error ExifInterface: " + e.getMessage());
            return ExifInterface.ORIENTATION_UNDEFINED;
        }

        return exifToDegrees(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL));
    }

    private static int exifToDegrees(int exifOrientation) {
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

}
