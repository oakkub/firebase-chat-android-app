package com.oakkub.chat.managers;

import android.app.Application;
import android.content.Context;

import com.facebook.FacebookSdk;
import com.facebook.common.soloader.SoLoaderShim;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.firebase.client.Firebase;
import com.oakkub.chat.modules.AppControllerModule;

/**
 * Created by OaKKuB on 10/11/2015.
 */
public class AppController extends Application {

    static {
        try {
            // work around for using resize method image on SimpleDraweeView
            SoLoaderShim.loadLibrary("webp");
        } catch (UnsatisfiedLinkError nle) {
        }
    }

    private AppComponent appComponent;

    public static AppComponent getComponent(Context context) {
        return ((AppController) context.getApplicationContext()).appComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Contextor.getInstance().init(this);

        Fresco.initialize(this);
        initFirebase();

        registerActivityLifecycleCallbacks(new MyLifeCycleHandler());
        initDependencyInjector();
    }

    private void initFirebase() {
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        Firebase.getDefaultConfig().setPersistenceCacheSizeBytes(20000000); // 20 MB
    }

    private void initDependencyInjector() {
        appComponent = DaggerAppComponent.builder()
                .appControllerModule(new AppControllerModule(this))
                .build();
    }
}
