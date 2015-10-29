package com.oakkub.chat.modules;

import android.app.Application;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.oakkub.chat.dagger.PerApp;
import com.oakkub.chat.managers.AppController;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by OaKKuB on 10/22/2015.
 */
@Module
public class AppControllerModule {

    private final AppController appController;

    public AppControllerModule(AppController appController) {
        this.appController = appController;
    }

    @PerApp
    @Provides
    AppController getAppController() {
        return appController;
    }

    @PerApp
    @Provides
    Application getApplication() {
        return appController;
    }

    @Provides
    Context getContext() {
        return appController;
    }

}
