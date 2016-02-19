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

    public GCMNotifyService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        final String token = intent.getStringExtra(GCMUtil.KEY_TO);
        final String title = intent.getStringExtra(GCMUtil.DATA_TITLE);
        final String message = intent.getStringExtra(GCMUtil.DATA_MESSAGE);
        final String sentBy = intent.getStringExtra(GCMUtil.DATA_SENT_BY);
        final String roomId = intent.getStringExtra(GCMUtil.DATA_ROOM_ID);

        if (roomId != null) {
            SparseStringArray instanceIds = intent.getParcelableExtra(GCMUtil.KEY_REGISTRATION_IDS);
            postAddFriendGCMRequest(token, title, message, sentBy, roomId, instanceIds);
        }
    }

    private void postAddFriendGCMRequest(String token, String title, String message,
                                         String sentBy, String roomId, SparseStringArray instanceIdList) {

        if (title == null || title.isEmpty()) {
            SharedPreferences prefs = AppController.getComponent(this).sharedPreferences();
            title = prefs.getString(UserInfoUtil.DISPLAY_NAME, "");
        }

        ArrayMap<String, String> dataMap = new ArrayMap<>(50);
        dataMap.put(GCMUtil.DATA_TITLE, title);
        dataMap.put(GCMUtil.DATA_MESSAGE, message);
        dataMap.put(GCMUtil.DATA_SENT_BY, sentBy);
        dataMap.put(GCMUtil.DATA_ROOM_ID, roomId);

        JSONObject gcmBody = instanceIdList == null ? GCMUtil.initGCMBody(token) : GCMUtil.initGroupGCMBody(instanceIdList);
        GCMUtil.putJsonValue(gcmBody, GCMUtil.KEY_PRIORITY, "high");

        GCMUtil.putGCMData(gcmBody, dataMap);
        Log.d(TAG, "postAddFriendGCMRequest: " + gcmBody.toString());
        sendGCM(gcmBody);
    }

    private void sendGCM(JSONObject gcmJson) {
        OkHttpClient okHttpClient = AppController.getComponent(this).okHttpClient();
        RequestBody requestBody =
                    RequestBody.create(NetworkUtil.MEDIA_TYPE_JSON, gcmJson.toString());
        Request request = initGCMRequest(requestBody);

        try {
            Response response = okHttpClient.newCall(request).execute();
            checkGCMResponse(response);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
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
