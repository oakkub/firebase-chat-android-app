package com.oakkub.chat.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.oakkub.chat.managers.AppController;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by OaKKuB on 10/21/2015.
 */
public class NetworkUtil {

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
