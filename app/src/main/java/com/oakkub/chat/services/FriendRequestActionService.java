package com.oakkub.chat.services;

import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.util.ArrayMap;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.MyIntentService;
import com.oakkub.chat.utils.FirebaseMapUtil;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.GCMUtil;
import com.oakkub.chat.utils.UserInfoUtil;
import com.oakkub.chat.views.widgets.MyToast;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;

/**
 * Created by OaKKuB on 2/24/2016.
 */
public class FriendRequestActionService extends MyIntentService {

    private static final String TAG = FriendRequestActionService.class.getSimpleName();

    public static final String EXTRA_CODE = "extra:code";
    public static final String EXTRA_FRIEND_KEY = "extra:friendKey";
    public static final String EXTRA_DISPLAY_NAME = "extra:displayName";

    public static final int CODE_ACCEPT_FRIEND = 0;
    public static final int CODE_REJECT_FRIEND = 1;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Lazy<Firebase> firebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Lazy<Firebase> userInfoFirebase;

    @Inject
    AuthData authData;

    private String uid;
    private String friendKey;
    private String displayName;
    private int code;

    public FriendRequestActionService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppController.getComponent(this).inject(this);

        code = intent.getIntExtra(EXTRA_CODE, -1);
        friendKey = intent.getStringExtra(EXTRA_FRIEND_KEY);
        displayName = intent.getStringExtra(EXTRA_DISPLAY_NAME);

        if (!checkAuth()) return;
        uid = authData.getUid();

        handle();
    }

    private boolean checkAuth() {
        if (authData == null) {
            showError();
            return false;
        }
        return true;
    }

    private void handle() {
        removeNotification();

        switch (code) {
            case CODE_ACCEPT_FRIEND:
            case CODE_REJECT_FRIEND:
                handleRequest();
                break;
        }
    }

    private void showError() {
        final String text = code == CODE_ACCEPT_FRIEND ? getString(R.string.accept) : getString(R.string.reject);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MyToast.make(getString(R.string.you_need_to_login_in_order_to_n, text)).show();
            }
        });
    }

    private void handleRequest() {
        firebase.get().updateChildren(getMapFriendRequest(), new FriendRequestCompletionListener());
    }

    private ArrayMap<String, Object> getMapFriendRequest() {
        ArrayMap<String, Object> map = new ArrayMap<>(3);

        FirebaseMapUtil.mapFriendReceivedRequest(map, friendKey, uid, true);
        FirebaseMapUtil.mapFriendPendingRequest(map, uid, friendKey, true);
        FirebaseMapUtil.mapUserFriend(map, uid, friendKey, code == CODE_REJECT_FRIEND);
        FirebaseMapUtil.mapUserFriend(map, friendKey, uid, code == CODE_REJECT_FRIEND);

        return map;
    }

    private void removeNotification() {
        NotificationManagerCompat notificationManager = AppController.getComponent(this).notificationManager();
        notificationManager.cancel(friendKey.hashCode());
    }

    private class FriendRequestCompletionListener implements Firebase.CompletionListener {

        @Override
        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
            if (firebaseError != null) {
                showError();
                return;
            }

            if (code == CODE_ACCEPT_FRIEND) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MyToast.make(getString(R.string.you_accept_n_as_friend, displayName)).show();
                    }
                });

                fetchInstanceIdToSendNotification();
            }
        }
    }

    private void fetchInstanceIdToSendNotification() {
        userInfoFirebase.get().child(friendKey).child(UserInfoUtil.INSTANCE_ID)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().removeEventListener(this);
                String instanceId = dataSnapshot.getValue(String.class);
                sendFriendAcceptedNotification(instanceId);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void sendFriendAcceptedNotification(String instanceId) {
        if (!checkAuth()) return;

        String displayName = AppController.getComponent(this)
                .sharedPreferences().getString(UserInfoUtil.DISPLAY_NAME, "");

        Intent intent = new Intent(this, GCMNotifyService.class);
        intent.putExtra(GCMUtil.KEY_TO, instanceId);
        intent.putExtra(GCMUtil.DATA_SENT_BY, uid);
        intent.putExtra(GCMUtil.DATA_TITLE, getString(R.string.accept_friend_request));
        intent.putExtra(GCMUtil.DATA_MESSAGE,
                getString(R.string.n_accepted_you_as_friend, displayName));
        intent.putExtra(GCMUtil.NOTIFY_TYPE, GCMUtil.FRIEND_ACCEPTED_NOTIFY_TYPE);
        startService(intent);
    }
}
