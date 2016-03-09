package com.oakkub.chat.managers.loaders;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.util.ArrayMap;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.services.GCMNotifyService;
import com.oakkub.chat.utils.FirebaseMapUtil;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.GCMUtil;
import com.oakkub.chat.utils.PrefsUtil;
import com.oakkub.chat.utils.UserInfoUtil;
import com.oakkub.chat.views.widgets.MyToast;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;

/**
 * Created by OaKKuB on 2/26/2016.
 */
public class SendFriendRequestLoader extends MyLoader<List<UserInfo>> {

    private static final String TAG = SendFriendRequestLoader.class.getSimpleName();

    public static final int CODE_SUCCESS = 0;
    public static final int CODE_ALREADY_FRIEND = 1;
    public static final int CODE_FAILED = 2;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Lazy<Firebase> firebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_FRIENDS)
    Firebase userFriendsFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_FRIENDS_RECEIVED_REQUESTED)
    Firebase receivedRequestFirebase;

    @Inject
    SharedPreferences prefs;

    private UserInfo friendInfo;
    private String uid;
    private int resultCode;

    public SendFriendRequestLoader(Context context, UserInfo friendInfo) {
        super(context);
        AppController.getComponent(getContext()).inject(this);

        resultCode = -1;
        uid = prefs.getString(PrefsUtil.PREF_UID, null);
        this.friendInfo = friendInfo;
    }

    public int getResultCode() {
        return resultCode;
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        checkIfFriendExisted();
    }

    private void sendResult(int code) {
        resultCode = code;
        deliverResult(Collections.singletonList(friendInfo));
    }

    private void checkIfFriendExisted() {
        userFriendsFirebase.child(uid)
                .child(friendInfo.getKey())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().removeEventListener(this);
                        prepareToSendRequest(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        sendResult(CODE_FAILED);
                    }
                });
    }

    private void prepareToSendRequest(DataSnapshot dataSnapshot) {
        // if exists, it means we are already friend.
        if (dataSnapshot.exists()) {
            sendResult(CODE_ALREADY_FRIEND);

            MyToast.make(getContext().getString(R.string.error_message_already_friend,
                    friendInfo.getDisplayName())).show();
        } else {
            checkIfReceivedRequest();
        }
    }

    private void checkIfReceivedRequest() {
        receivedRequestFirebase.child(uid).child(friendInfo.getKey())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().removeEventListener(this);

                // this user already sent you a friend request
                if (dataSnapshot.exists()) {
                    sendResult(CODE_ALREADY_FRIEND);

                    Toast toast = MyToast.make(getContext().getString(
                            R.string.n_sent_friend_request_go_check_received_requests_tab,
                            friendInfo.getDisplayName()));
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    putFriendRequest();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                sendResult(CODE_FAILED);
            }
        });
    }

    private void putFriendRequest() {
        ArrayMap<String, Object> map = new ArrayMap<>(2);
        FirebaseMapUtil.mapSendFriendRequested(map, friendInfo.getKey(), uid, false);
        FirebaseMapUtil.mapFriendPendingRequest(map, friendInfo.getKey(), uid, false);

        firebase.get().updateChildren(map, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    sendResult(CODE_FAILED);

                    MyToast.make(getContext().getString(R.string.error_friend_requested_sent)).show();
                    return;
                }

                sendResult(CODE_SUCCESS);
                sendAddFriendNotification(friendInfo);
                MyToast.make(getContext().getString(R.string.success_friend_requested_sent)).show();
            }
        });
    }

    private void sendAddFriendNotification(UserInfo friendUserInfo) {
        String displayName = prefs.getString(UserInfoUtil.DISPLAY_NAME, "");

        Intent notificationService = new Intent(getContext(), GCMNotifyService.class);
        notificationService.putExtra(GCMUtil.KEY_TO, friendUserInfo.getInstanceID());
        notificationService.putExtra(GCMUtil.DATA_SENT_BY, uid);
        notificationService.putExtra(GCMUtil.DATA_TITLE,
                getContext().getString(R.string.friend_request));
        notificationService.putExtra(GCMUtil.DATA_MESSAGE,
                getContext().getString(R.string.n_send_you_a_friend_request, displayName));
        notificationService.putExtra(GCMUtil.NOTIFY_TYPE, GCMUtil.FRIEND_REQUEST_NOTIFY_TYPE);

        getContext().startService(notificationService);
    }

}
