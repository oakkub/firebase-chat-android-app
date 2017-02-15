package com.oakkub.chat.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OaKKuB on 3/6/2016.
 */
public class PermissionUtil {

    public static boolean isPermissionAllowed(Activity activity, String permission, int requestCode) {
        if (!isPermissionGranted(activity, permission)) {
            ActivityCompat.requestPermissions(activity, new String[] { permission }, requestCode);
            return false;
        }

        return true;
    }

    public static boolean isPermissionAllowed(Fragment fragment, String permission, int requestCode) {
        if (!isPermissionGranted(fragment.getContext(), permission)) {
            fragment.requestPermissions(new String[] { permission }, requestCode);
            return false;
        }

        return true;
    }

    public static boolean isPermissionGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private static List<String> getNeededPermissions(Activity activity, List<String> permissionList) {
        List<String> permissionNeeded = new ArrayList<>(permissionList.size());

        for (int i = 0, size = permissionList.size(); i < size; i++) {
            String permission = permissionList.get(i);

            if (ActivityCompat.checkSelfPermission(activity, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    permissionNeeded.add(permission);
                }
            }
        }

        return permissionNeeded;
    }

}
