package com.oakkub.chat.utils;

import android.os.Build;

/**
 * Created by OaKKuB on 11/9/2015.
 */
public class VersionUtil {

    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

}
