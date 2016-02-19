package com.oakkub.chat.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.oakkub.chat.views.widgets.MyToast;

/**
 * Created by OaKKuB on 1/31/2016.
 */
public class IntentUtil {

    public static Intent openImageViewer(Context context, boolean allowMultipleSelection) {
        Intent imageViewerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        imageViewerIntent.addCategory(Intent.CATEGORY_OPENABLE);
        imageViewerIntent.setType("image/*");
        imageViewerIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        if (Build.VERSION.SDK_INT >= 18 && allowMultipleSelection) {
            imageViewerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        if (imageViewerIntent.resolveActivity(context.getPackageManager()) == null) {
            MyToast.make("Cannot open image viewer").show();
            return null;
        }
        return imageViewerIntent;
    }

    public static Intent openCamera(Context context, Uri uriCameraImageFile) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriCameraImageFile);
        if (cameraIntent.resolveActivity(context.getPackageManager()) == null) {
            MyToast.make("Cannot use camera").show();
            return null;
        }
        return cameraIntent;
    }

}
