package com.oakkub.chat.managers;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.firebase.client.Firebase;
import com.oakkub.chat.modules.AppControllerModule;

import io.fabric.sdk.android.Fabric;

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

        Contextor.getInstance().init(this);
        Fabric.with(this, new Crashlytics());
        initFresco();
        initFirebase();
        Firebase.setAndroidContext(this);
        MultiDex.install(this);

        initDependencyInjector();
    }

    private void initFresco() {
        /*ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setDownsampleEnabled(true)
                .build();*/
        Fresco.initialize(this);
    }

    private void initFirebase() {
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        Firebase.getDefaultConfig().setPersistenceCacheSizeBytes(20000000);
    }

    private void initDependencyInjector() {
        appComponent =  DaggerAppComponent.builder()
                .appControllerModule(new AppControllerModule(this))
                .build();
    }
}
