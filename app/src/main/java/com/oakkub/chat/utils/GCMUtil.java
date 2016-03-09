package com.oakkub.chat.utils;

import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.oakkub.chat.managers.SparseStringArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public static final String NOTIFY_TYPE = "notifyType";
    public static final String CHAT_NEW_MESSAGE_NOTIFY_TYPE = "notify:chatNewMessage";
    public static final String FRIEND_REQUEST_NOTIFY_TYPE = "notify:friendRequest";
    public static final String FRIEND_ACCEPTED_NOTIFY_TYPE = "notify:friendAccepted";

    public static final String START_TOPICS = "/topics/";
    public static final String KEY_TO = "to";
    public static final String KEY_DATA = "data";
    public static final String KEY_PRIORITY = "priority";
    public static final String KEY_REGISTRATION_IDS = "registration_ids";
    public static final String KEY_COLLAPSE_KEY = "collapse_key";

    public static final String MESSAGE_COLLAPSE_KEY = "message";

    public static final String DATA_TITLE = "title";
    public static final String DATA_MESSAGE = "message";
    public static final String DATA_SENT_BY = "sentBy";
    public static final String DATA_DISPLAY_NAME = "displayName";
    public static final String DATA_ROOM_ID = "room";
    public static final String DATA_IMAGE_URI = "imageUri";
    public static final String PROFILE_URL = "profileURL";

    public static final int REQUEST_CODE_UPDATE_GOOGLE_PLAY = 1000;

    public static JSONObject initGCMBody(String token) {
        JSONObject gcmBodyJson = new JSONObject();

        try {
            gcmBodyJson.put(KEY_TO, token);
        } catch (JSONException e) {
            Log.e(TAG, "initGCMBody: " + e.getMessage());
        }

        return gcmBodyJson;
    }

    /*public static JSONObject initGroupGCMBody(ArrayList<String> instanceIds) {
        JSONObject gcmBodyJson = new JSONObject();

        try {
            JSONArray instanceIdsArray = new JSONArray();
            for (int i = 0, size = instanceIds.size(); i < size; i++) {
                instanceIdsArray.put(instanceIds.get(i));
            }
            gcmBodyJson.put(KEY_REGISTRATION_IDS, instanceIdsArray);
        } catch (JSONException e) {
            Log.e(TAG, "initGroupGCMBody: " + e.getMessage());
        }

        return gcmBodyJson;
    }*/

    public static JSONObject initGroupGCMBody(SparseStringArray instanceIds) {
        JSONObject gcmBodyJson = new JSONObject();

        try {
            JSONArray instanceIdsArray = new JSONArray();
            for (int i = 0, size = instanceIds.size(); i < size; i++) {
                instanceIdsArray.put(instanceIds.valueAt(i));
            }
            gcmBodyJson.put(KEY_REGISTRATION_IDS, instanceIdsArray);
        } catch (JSONException e) {
            Log.e(TAG, "initGroupGCMBody: " + e.getMessage());
        }

        return gcmBodyJson;
    }

    public static void putData(JSONObject jsonBody, ArrayMap<String, String> dataMap) {
        try {

            JSONObject dataJSON = new JSONObject();
            for (int i = 0, size = dataMap.size(); i < size; i++) {
                dataJSON.put(dataMap.keyAt(i), dataMap.valueAt(i));
            }

            jsonBody.put(KEY_DATA, dataJSON);
        } catch (JSONException e) {
            Log.e(TAG, "putData: " + e.getMessage());
        }

    }

    public static void putValue(JSONObject json, String key, String value) {
        try {
            json.put(key, value);
        } catch (JSONException e) {
            Log.e(TAG, "putValue: " + e.getMessage());
        }
    }
}
