package com.oakkub.chat.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.utils.GoogleUtil;
import com.oakkub.chat.utils.NetworkUtil;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by OaKKuB on 11/3/2015.
 */
public class GCMNotificationService extends IntentService {

    public static final String TITLE = "title";
    public static final String MESSAGE = "message";
    public static final String PROFILE_URL = "profileURL";
    public static final String TO = "to";
    private static final String TAG = GCMNotificationService.class.getSimpleName();
    private static final String MESSAGE_ID = "notification";

    public GCMNotificationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        final String token = intent.getStringExtra(TO);
        final String title = intent.getStringExtra(TITLE);
        final String message = intent.getStringExtra(MESSAGE);
        final String profileImageURL = intent.getStringExtra(PROFILE_URL);

        postAddFriendGCMRequest(token, title, message, profileImageURL);
    }

    private void postAddFriendGCMRequest(String token, String title, String message, String profileImageURL) {
        OkHttpClient okHttpClient = AppController.getComponent(this).okHttpClient();

        JSONObject gcmBody = initGCMBody(token, title, message, profileImageURL);

        RequestBody requestBody =
                RequestBody.create(NetworkUtil.MEDIA_TYPE_JSON, gcmBody.toString());
        Request request = initGCMRequest(requestBody);

        try {
            Response response = okHttpClient.newCall(request).execute();
            checkGCMResponse(response);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private JSONObject initGCMBody(String token, String title, String message, String profileImageURL) {
        JSONObject gcmBodyJson = new JSONObject();

        try {
            gcmBodyJson.put(GoogleUtil.KEY_TO, token);
            gcmBodyJson.put(GoogleUtil.KEY_DATA, getDataJson(title, message, profileImageURL));
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        return gcmBodyJson;
    }

    private JSONObject getDataJson(String title, String message, String profileImageURL) throws JSONException {
        JSONObject dataJSON = new JSONObject();

        dataJSON.put(TITLE, title);
        dataJSON.put(MESSAGE, message);
        dataJSON.put(PROFILE_URL, profileImageURL);

        return dataJSON;
    }

    private Request initGCMRequest(RequestBody requestBody) {
        return new Request.Builder()
                .url(GoogleUtil.GCM_SEND_URL)
                .header(NetworkUtil.HEADER_CONTENT_TYPE, NetworkUtil.JSON_CONTENT_TYPE)
                .header(NetworkUtil.HEADER_AUTHORIZATION,
                       "key=" + getString(R.string.google_api_key))
                .post(requestBody)
                .build();
    }

    private void checkGCMResponse(Response response) {
        try {
            if (!response.isSuccessful()) {
                Log.e(TAG, String.valueOf(response.body().string()));
                Log.e(TAG, String.valueOf(response.code()));
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

}
