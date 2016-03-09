package com.oakkub.chat.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.SparseStringArray;
import com.oakkub.chat.utils.GCMUtil;
import com.oakkub.chat.utils.NetworkUtil;
import com.oakkub.chat.utils.UserInfoUtil;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by OaKKuB on 11/3/2015.
 */
public class GCMNotifyService extends IntentService {

    private static final String TAG = GCMNotifyService.class.getSimpleName();

    private String token;
    private String title;
    private String message;
    private String sentBy;
    private String sentByDisplayName;
    private String notifyType;

    private SharedPreferences prefs;

    public GCMNotifyService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        prefs = AppController.getComponent(this).sharedPreferences();
        sentByDisplayName = prefs.getString(UserInfoUtil.DISPLAY_NAME, "");

        token = intent.getStringExtra(GCMUtil.KEY_TO);
        title = intent.getStringExtra(GCMUtil.DATA_TITLE);
        message = intent.getStringExtra(GCMUtil.DATA_MESSAGE);
        sentBy = intent.getStringExtra(GCMUtil.DATA_SENT_BY);
        notifyType = intent.getStringExtra(GCMUtil.NOTIFY_TYPE);

        switch (notifyType) {
            case GCMUtil.CHAT_NEW_MESSAGE_NOTIFY_TYPE:
                notifyNewChatMessage(intent);
                break;
            case GCMUtil.FRIEND_REQUEST_NOTIFY_TYPE:
            case GCMUtil.FRIEND_ACCEPTED_NOTIFY_TYPE:
                notifyDefaultData();
                break;
        }
    }

    private ArrayMap<String, String> getDataBody() {
        ArrayMap<String, String> map = new ArrayMap<>(50);

        map.put(GCMUtil.DATA_TITLE, title);
        map.put(GCMUtil.DATA_MESSAGE, message);
        map.put(GCMUtil.DATA_SENT_BY, sentBy);
        map.put(GCMUtil.NOTIFY_TYPE, notifyType);
        map.put(GCMUtil.DATA_DISPLAY_NAME, sentByDisplayName);

        return map;
    }

    private void notifyDefaultData() {
        ArrayMap<String, String> dataMap = getDataBody();
        JSONObject gcmBody = GCMUtil.initGCMBody(token);
        GCMUtil.putValue(gcmBody, GCMUtil.KEY_PRIORITY, "high");
        GCMUtil.putData(gcmBody, dataMap);

        sendGCM(gcmBody);
    }

    private void notifyNewChatMessage(Intent intent) {
        String roomId = intent.getStringExtra(GCMUtil.DATA_ROOM_ID);
        SparseStringArray instanceIds = intent.getParcelableExtra(GCMUtil.KEY_REGISTRATION_IDS);

        postNewMessage(roomId, instanceIds);
    }

    private void postNewMessage(String roomId, SparseStringArray instanceIdList) {

        if (title == null || title.isEmpty()) {
            title = sentByDisplayName;
        }

        ArrayMap<String, String> dataMap = getDataBody();
        dataMap.put(GCMUtil.DATA_ROOM_ID, roomId);

        JSONObject gcmBody = instanceIdList == null ? GCMUtil.initGCMBody(token) : GCMUtil.initGroupGCMBody(instanceIdList);
        GCMUtil.putValue(gcmBody, GCMUtil.KEY_PRIORITY, "high");
        GCMUtil.putValue(gcmBody, GCMUtil.KEY_COLLAPSE_KEY, GCMUtil.MESSAGE_COLLAPSE_KEY);
        GCMUtil.putData(gcmBody, dataMap);

        Log.d(TAG, "postNewMessage: " + gcmBody.toString());
        sendGCM(gcmBody);
    }

    private void sendGCM(JSONObject gcmJson) {
        OkHttpClient okHttpClient = AppController.getComponent(this).okHttpClient();

        try {
            Response response = okHttpClient.newCall(getRequest(gcmJson)).execute();
            checkGCMResponse(response);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private Request getRequest(JSONObject gcmJson) {
        RequestBody requestBody =
                RequestBody.create(NetworkUtil.MEDIA_TYPE_JSON, gcmJson.toString());
        Log.d(TAG, "getRequest: " + gcmJson.toString());
        return initGCMRequest(requestBody);
    }

    private Request initGCMRequest(RequestBody requestBody) {
        return new Request.Builder()
                .url(GCMUtil.GCM_SEND_URL)
                .header(NetworkUtil.HEADER_CONTENT_TYPE, NetworkUtil.JSON_CONTENT_TYPE)
                .header(NetworkUtil.HEADER_AUTHORIZATION,
                       "key=" + getString(R.string.google_api_key))
                .post(requestBody)
                .build();
    }

    private void checkGCMResponse(Response response) {
        try {
            Log.e(TAG, "body: " + String.valueOf(response.body().string()));
            Log.e(TAG, "code: " + String.valueOf(response.code()));
        } catch (IOException e) {
            Log.e(TAG, "error: " + e.getMessage());
        }
    }

}
