package com.oakkub.chat.services;

import android.app.Notification;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.utils.GoogleUtil;

/**
 * Created by OaKKuB on 11/1/2015.
 */
public class GCMListenerService extends GcmListenerService {

    private static final String TAG = GCMListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data) {
        final String title = data.getString(GCMNotificationService.TITLE);
        final String message = data.getString(GCMNotificationService.MESSAGE);
        debug(from, data);

        newFriendNotification(title, message);

    }

    private void newFriendNotification(String title, String message) {

        NotificationCompat.Builder newFriendNotification = getDefaultNotification(title, message)
                .setSmallIcon(R.drawable.ic_person_add_24dp);

        notify(0, newFriendNotification.build());
    }

    private NotificationCompat.Builder getDefaultNotification(String title, String message) {
        return new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setTicker(title)
                .setContentText(message)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE);
    }

    private void notify(int id, Notification notification) {
        AppController.getComponent(this).notificationManager()
                .notify(id, notification);
    }

    private void debug(String from, Bundle data) {
        Log.e(TAG, "From: " + from);
        Log.e(TAG, "Title: " + data.getString(GCMNotificationService.TITLE));
        Log.e(TAG, "Message: " + data.getString(GCMNotificationService.MESSAGE));
        Log.e(TAG, "Profile URL: " + data.getString(GCMNotificationService.PROFILE_URL));

        if (from.startsWith(GoogleUtil.START_TOPICS)) {
            Log.e(TAG, "TOPICS");

        } else {
            Log.e(TAG, "DEFAULT MESSAGE");

        }
    }

}
