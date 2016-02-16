package com.oakkub.chat.managers;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.common.soloader.SoLoaderShim;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.firebase.client.Firebase;
import com.oakkub.chat.modules.AppControllerModule;

import io.fabric.sdk.android.Fabric;

/**
 * Created by OaKKuB on 10/11/2015.
 */
public class AppController extends Application {

    private AppComponent appComponent;

    static {
        //noinspection EmptyCatchBlock
        try {
            SoLoaderShim.loadLibrary("webp");
        } catch(UnsatisfiedLinkError nle) {
        }
    }

    public static AppComponent getComponent(Context context) {
        return ((AppController) context.getApplicationContext()).appComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Contextor.getInstance().init(this);
        initFresco();
        Fabric.with(this, new Crashlytics());
        FacebookSdk.sdkInitialize(this);
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

    private void initDependencyInjector() {
        appComponent =  DaggerAppComponent.builder()
                .appControllerModule(new AppControllerModule(this))
                .build();
    }
}
