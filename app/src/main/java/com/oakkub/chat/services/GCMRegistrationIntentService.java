package com.oakkub.chat.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.eventbus.EventBusGoogleInstanceID;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.GCMUtil;

import java.io.IOException;

import de.greenrobot.event.EventBus;

/**
 * Created by OaKKuB on 11/1/2015.
 */
public class GCMRegistrationIntentService extends IntentService {

    public static final String LOGIN_ACTION = "ACTION_LOGIN";
    public static final String UPDATE_ACTION = "action:update";
    private static final String TAG = GCMRegistrationIntentService.class.getSimpleName();
    private static final String[] TOPICS = {"NEW_MESSAGE", "NEW_FRIEND"};

    public GCMRegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {

            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.google_project_id),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Log.d(TAG, "Registration token: " + token);

            saveTokenToPreferences(token);
            sendRegistrationTokenToServer(intent, token);
            subscribeTopics(token);

        } catch (IOException e) {
            Log.e(TAG, "error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveTokenToPreferences(String token) {
        SharedPreferences.Editor editor = AppController.getComponent(this).sharedPreferencesEditor();
        editor.putString(GCMUtil.PREFS_TOKEN, token).apply();
    }

    private void sendRegistrationTokenToServer(Intent intent, String token) {
        // store instanceID in our server, if we have.
        Firebase firebase = AppController.getComponent(this).firebase();
        AuthData authData = firebase.getAuth();

        if (authData != null) {

            Firebase tokenFirebase = firebase.child(FirebaseUtil.KEY_USERS)
                    .child(FirebaseUtil.KEY_USERS_USER_INFO)
                    .child(authData.getUid());

            sendTokenToFirebase(tokenFirebase, intent, token);
        } else {

            if (intent.getAction() != null && intent.getAction().equals(UPDATE_ACTION)) {
                return;
            }
            postErrorEventBus(intent);
        }
    }

    private void sendTokenToFirebase(Firebase firebase, final Intent intent, final String token) {

        ArrayMap<String, Object> tokenMap = getTokenHashMap(token);

        firebase.updateChildren(tokenMap, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (intent.getAction() != null && intent.getAction().equals(UPDATE_ACTION)) {
                    return;
                }

                if (firebaseError != null) {
                    Log.e(TAG, firebaseError.getMessage());
                    postErrorEventBus(intent);
                } else {
                    postEventBus(intent);
                }
            }
        });

    }

    private ArrayMap<String, Object> getTokenHashMap(String token) {
        ArrayMap<String, Object> tokenMap = new ArrayMap<>(1);
        tokenMap.put(GCMUtil.KEY_INSTANCE_ID, token);

        return tokenMap;
    }

    private void subscribeTopics(String token) throws IOException {
        GcmPubSub gcmPubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            gcmPubSub.subscribe(token,
                    String.format("%s%s", GCMUtil.START_TOPICS, topic), null);
        }
    }

    private boolean checkIfInstanceIDIsFetched(SharedPreferences prefs, Intent intent) throws IOException {
        final String prefsToken = prefs.getString(GCMUtil.PREFS_TOKEN, "");

        if (!prefsToken.equals("") && prefsToken.length() > 0) {
            sendRegistrationTokenToServer(intent, prefsToken);
            subscribeTopics(prefsToken);
            return true;
        }

        return false;
    }

    private void postEventBus(Intent intent) {
        if (canPostEventBus(intent)) {
            EventBus.getDefault().post(new EventBusGoogleInstanceID());
        }
    }

    private void postErrorEventBus(Intent intent) {
        if (canPostEventBus(intent)) {
            EventBus.getDefault().post(null);
        }
    }

    private boolean canPostEventBus(Intent intent) {
        return intent.getAction() != null;
    }
}
