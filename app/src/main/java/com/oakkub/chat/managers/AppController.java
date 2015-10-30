package com.oakkub.chat.managers;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.facebook.FacebookSdk;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.firebase.client.Firebase;
import com.oakkub.chat.modules.AppControllerModule;

/**
 * Created by OaKKuB on 10/11/2015.
 */
public class AppController extends Application {

    private AppComponent appComponent;

    public static AppComponent getComponent(Context context) {
        return ((AppController) context.getApplicationContext()).appComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        FacebookSdk.sdkInitialize(this);
        Firebase.setAndroidContext(this);
        Fresco.initialize(this);

        MultiDex.install(this);

        initDependencyInjector();
    }

    private void initDependencyInjector() {

        appComponent = DaggerAppComponent.builder()
                .appControllerModule(new AppControllerModule(this))
                .build();

    }

}
