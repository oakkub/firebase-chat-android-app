package com.oakkub.chat.utils;

import android.support.v4.util.ArrayMap;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by OaKKuB on 11/1/2015.
 */
public class GCMUtil {

    private static final String TAG = GCMUtil.class.getSimpleName();

    public static final String GCM_SEND_URL = "https://gcm-http.googleapis.com/gcm/send";
    public static final String ADDRESS_GCM_API = "@gcm.googleapis.com";
    public static final String PREFS_TOKEN = "prefsToken";
    public static final String KEY_INSTANCE_ID = "instanceID";
    public static final String KEY_MESSAGE = "message";
    public static final String PREFS_SEND_TOKEN_TO_SERVER = "sendTokenToServer";

    public static final String START_TOPICS = "/topics/";
    public static final String KEY_TO = "to";
    public static final String KEY_DATA = "data";
    public static final String KEY_PRIORITY = "priority";
    public static final String KEY_REGISTRATION_IDS = "registration_ids";

    public static final String DATA_TITLE = "title";
    public static final String DATA_MESSAGE = "message";
    public static final String DATA_SENT_BY = "sentBy";
    public static final String DATA_PRIVATE_ROOM = "isPrivateRoom";
    public static final String DATA_ROOM_ID = "room";
    public static final String PROFILE_URL = "profileURL";


    public static final int REQUEST_CODE_UPDATE_GOOGLE_PLAY = 1000;

    public static JSONObject initGCMBody(String token) {
        JSONObject gcmBodyJson = new JSONObject();

        try {
            gcmBodyJson.put(KEY_TO, token);
        } catch (JSONException e) {
            Log.e(TAG, "initGCMBody: " + e.getMessage() );
        }

        return gcmBodyJson;
    }

    public static JSONObject initGroupGCMBody(ArrayList<String> instanceIds) {
        JSONObject gcmBodyJson = new JSONObject();

        try {
            JSONArray instanceIdsArray = new JSONArray();
            for (int i = 0, size = instanceIds.size(); i < size; i++) {
                instanceIdsArray.put(instanceIds.get(i));
            }
            gcmBodyJson.put(KEY_REGISTRATION_IDS, instanceIdsArray);
        } catch (JSONException e) {
            Log.e(TAG, "initGroupGCMBody: " + e.getMessage() );
        }

        return gcmBodyJson;
    }

    public static void putGCMData(JSONObject jsonBody, ArrayMap<String, String> dataMap) {
        JSONObject dataJSON = new JSONObject();

        try {
            for (int i = 0, size = dataMap.size(); i < size; i++) {
                dataJSON.put(dataMap.keyAt(i), dataMap.valueAt(i));
            }
            jsonBody.put(KEY_DATA, dataJSON);
        } catch (JSONException e) {
            Log.e(TAG, "putGCMData: " + e.getMessage() );
        }

    }

    public static void putJsonValue(JSONObject json, String key, String value) {
        try {
            json.put(key, value);
        } catch (JSONException e) {
            Log.e(TAG, "putJsonValue: " + e.getMessage() );
        }
    }
}
