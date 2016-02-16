package com.oakkub.chat.utils;

import android.net.Uri;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

/**
 * Created by OaKKuB on 1/20/2016.
 */
public class FrescoUtil {

    public static DraweeController getResizeController(int targetWidth, int targetHeight, Uri uri, DraweeController oldController) {

        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(new ResizeOptions(targetWidth, targetHeight))
                .build();

        return Fresco.newDraweeControllerBuilder()
                .setOldController(oldController)
                .setImageRequest(imageRequest)
                .build();
    }

}
