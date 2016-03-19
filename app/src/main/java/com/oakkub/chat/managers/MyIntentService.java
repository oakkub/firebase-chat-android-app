package com.oakkub.chat.managers;

import android.app.IntentService;
import android.os.Handler;

/**
 * Created by OaKKuB on 3/16/2016.
 */
public abstract class MyIntentService extends IntentService {

    private Handler handler;

    public MyIntentService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        handler = new Handler();
        super.onCreate();
    }

    protected void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }
}
