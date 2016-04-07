package com.oakkub.chat.modules;

import android.app.Application;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationManagerCompat;
import android.view.inputmethod.InputMethodManager;

import com.oakkub.chat.dagger.PerApp;

import dagger.Module;
import dagger.Provides;

/**
 * Created by OaKKuB on 10/22/2015.
 */
@Module
public class SystemServiceModule {

    @PerApp
    @Provides
    InputMethodManager  provideInputMethodManager(Application application) {
        return (InputMethodManager) application.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @PerApp
    @Provides
    ConnectivityManager provideConnectivityManager(Application application) {
        return (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @PerApp
    @Provides
    NotificationManagerCompat provideNotificationManager(Application application) {
        return NotificationManagerCompat.from(application);
    }

    @PerApp
    @Provides
    Vibrator provideVibrator(Application application) {
        return (Vibrator) application.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @PerApp
    @Provides
    ClipboardManager provideClipboardManager(Application application) {
        return (ClipboardManager) application.getSystemService(Context.CLIPBOARD_SERVICE);
    }

}
