package com.oakkub.chat.managers;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by OaKKuB on 3/3/2016.
 */
public class MyLifeCycleHandler implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = MyLifeCycleHandler.class.getSimpleName();
    private static int totalActivity;
    private static String currentActivityName;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        ++totalActivity;
        currentActivityName = activity.getClass().getSimpleName();
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        --totalActivity;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public static boolean isForeground() {
        return totalActivity > 0;
    }

    public static String getCurrentActivityName() {
        return currentActivityName;
    }
}
