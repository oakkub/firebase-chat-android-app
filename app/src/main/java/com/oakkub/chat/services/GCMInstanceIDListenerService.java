package com.oakkub.chat.services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.utils.PrefsUtil;

/**
 * Created by OaKKuB on 11/1/2015.
 */
public class GCMInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = GCMInstanceIDListenerService.class.getSimpleName();

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    @Override
    public void onTokenRefresh() {

        Log.e(TAG, "onTokenRefresh");
        SharedPreferences.Editor editor = AppController.getComponent(this).sharedPreferencesEditor();
        editor.putBoolean(PrefsUtil.PREF_SHOULD_UPDATE_INSTANCE_ID, true).apply();

        Intent registrationTokenService = new Intent(this, GCMRegistrationIntentService.class);
        startService(registrationTokenService);
    }
}
