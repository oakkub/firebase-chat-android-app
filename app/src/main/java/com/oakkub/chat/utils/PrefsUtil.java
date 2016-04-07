package com.oakkub.chat.utils;

import android.content.Context;

import com.oakkub.chat.managers.AppController;

/**
 * Created by OaKKuB on 10/26/2015.
 */
public class PrefsUtil {

    public static final String PREF_UID = "prefs:uid";
    public static final String PREF_EMAIL = "prefs:email";
    public static final String PREF_SHOULD_UPDATE_INSTANCE_ID = "prefs:shouldUpdateInstanceId";
    public static final String PREF_CONNECTION_CHANGE = "prefs:internetConnectionChange";

    public static boolean shouldUpdateInstanceId(Context context) {
        return AppController.getComponent(context).sharedPreferences().getBoolean(PREF_SHOULD_UPDATE_INSTANCE_ID, false);
    }

}
