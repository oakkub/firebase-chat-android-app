package com.oakkub.chat.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.oakkub.chat.managers.AppController;

import java.net.InetAddress;
import java.net.UnknownHostException;

import okhttp3.MediaType;

/**
 * Created by OaKKuB on 10/21/2015.
 */
public class NetworkUtil {

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_AUTHORIZATION = "Authorization";

    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String CHARSET = "charset=utf-8";

    public static final MediaType MEDIA_TYPE_JSON
            = MediaType.parse(JSON_CONTENT_TYPE + "; " + CHARSET);

    public static boolean isNetworkConnected(Context context) {

        ConnectivityManager connectivityManager = AppController.getComponent(context).connectivityManager();
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    public static boolean isNetworkAvailable() {
        try {
            return !InetAddress.getByName("google.com").equals("");
        } catch (UnknownHostException e) {
            return false;
        }
    }

}
