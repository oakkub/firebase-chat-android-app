package com.oakkub.chat.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.UserSnapshotOnlineInfo;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.PrefsUtil;
import com.oakkub.chat.utils.Util;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;

/**
 * Created by OaKKuB on 3/23/2016.
 */
public class InternetConnectionChangeReceiver extends BroadcastReceiver {

    private static final String TAG = InternetConnectionChangeReceiver.class.getSimpleName();

    public static final String ACTION_INTERNET_CONNECTION =
            "com.oakkub.chat.broadcast.InternetConnectionChangeReceiver.ExplicitCall";
    private static final String ACTION_CONNECTIVITY_CHANGE =
            "android.net.conn.CONNECTIVITY_CHANGE";

    private static final String[] actions = new String[] {
            ACTION_CONNECTIVITY_CHANGE, ACTION_INTERNET_CONNECTION
    };

    @Inject
    @Named(FirebaseUtil.NAMED_CONNECTION)
    Lazy<Firebase> connectionFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ONLINE_USERS)
    Lazy<Firebase> onlineUserFirebase;

    @Inject
    Lazy<SharedPreferences> prefs;

    private String uid;

    @Override
    public void onReceive(Context context, Intent intent) {
        AppController.getComponent(context).inject(this);
        uid = prefs.get().getString(PrefsUtil.PREF_UID, "");

        if (!isReceiverAvailable(intent) ||
            !Util.isInternetAvailable() ||
            uid.isEmpty()) return;
        
        connectionFirebase.get().addValueEventListener(connectionValueEventListener);
    }

    private boolean isReceiverAvailable(Intent intent) {
        String intentAction = intent.getAction();
        boolean isAvailable = false;

        for (String action : actions) {
            if (action.equals(intentAction)) {
                isAvailable = true;
                break;
            }
        }

        return isAvailable;
    }

    private ValueEventListener connectionValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            boolean isConnected = dataSnapshot.getValue(Boolean.class);

            if (isConnected) {
                Firebase currentOnlineRef = onlineUserFirebase.get().child(uid).getRef();

                UserSnapshotOnlineInfo connectedUserInfo = new UserSnapshotOnlineInfo(true);
                UserSnapshotOnlineInfo disconnectedUserInfo = new UserSnapshotOnlineInfo(false);

                currentOnlineRef.onDisconnect().setValue(disconnectedUserInfo);
                currentOnlineRef.setValue(connectedUserInfo);
            }

            dataSnapshot.getRef().removeEventListener(this);
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    };

}
