package com.oakkub.chat.modules;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.oakkub.chat.dagger.PerApp;

import dagger.Module;
import dagger.Provides;

/**
 * Created by OaKKuB on 10/22/2015.
 */
@Module
public class StorageModule {

    @PerApp
    @Provides
    SharedPreferences provideSharedPreference(Application application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @PerApp
    @Provides
    SharedPreferences.Editor provideSharedPreferenceEditor(SharedPreferences sharedPreferences) {
        return sharedPreferences.edit();
    }

}
