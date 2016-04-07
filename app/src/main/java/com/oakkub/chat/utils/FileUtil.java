package com.oakkub.chat.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.oakkub.chat.R;
import com.oakkub.chat.managers.Contextor;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by OaKKuB on 1/31/2016.
 */
public class FileUtil {

    private static final String TAG = FileUtil.class.getSimpleName();

    public static ParcelFileDescriptor getParcelFileDescriptor(Uri uri, String mode) {
        Context context = Contextor.getInstance().getContext();
        try {
            return context.getContentResolver().openFileDescriptor(uri, mode);
        } catch (IOException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }
        return null;
    }

    public static File getCameraStorageDirectory() {
        return getCameraStorageDirectory("images");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getCameraStorageDirectory(String finalDir) {
        Context context = Contextor.getInstance().getContext();

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File appDir = new File(storageDir, context.getString(R.string.app_name));
        File imageDir = new File(appDir, finalDir);
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageName = "IMG_" + timeStamp + "_";
        Log.d(TAG, "createImageFile: " + imageName);

        File imageFile;
        try {
            imageFile = File.createTempFile(imageName, ".jpg", imageDir);
        } catch (IOException e) {
            Log.e(TAG, "getCameraStorageDirectory: " + e.getMessage() );
            return null;
        }

        Log.d(TAG, "createImageFile: " + imageFile.getAbsolutePath());
        return imageFile;
    }

}
