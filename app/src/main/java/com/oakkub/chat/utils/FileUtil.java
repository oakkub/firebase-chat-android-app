package com.oakkub.chat.utils;

import android.os.Environment;
import android.util.Log;

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
    private static final String CAMERA_STORAGE = "Chatto";

    public static File getCameraStorage() {
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File finalDir = new File(storageDir, CAMERA_STORAGE);
        if (!finalDir.exists()) {
            finalDir.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageName = "IMG_" + timeStamp + "_";
        Log.d(TAG, "createImageFile: " + imageName);

        File imageFile;
        try {
            imageFile = File.createTempFile(imageName, ".jpg", finalDir);
        } catch (IOException e) {
            Log.e(TAG, "getCameraStorage: " + e.getMessage() );
            return null;
        }

        Log.d(TAG, "createImageFile: " + imageFile.getAbsolutePath());
        return imageFile;
    }

}
