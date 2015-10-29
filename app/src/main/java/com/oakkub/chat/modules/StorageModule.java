package com.oakkub.chat.modules;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.oakkub.chat.dagger.PerApp;

import dagger.Module;
import dagger.Provides;

/**
 * Created by OaKKuB on 10/22/2015.
 */
@Module
public class StorageModule {

    private static final String DEFAULT_PREF = "default_pref";

    @PerApp
    @Provides
    SharedPreferences provideSharedPreference(Application application) {
        return application.getSharedPreferences(DEFAULT_PREF, Context.MODE_PRIVATE);
    }

    @PerApp
    @Provides
    SharedPreferences.Editor provideSharedPreferenceEditor(SharedPreferences sharedPreferences) {
        return sharedPreferences.edit();
    }

}
