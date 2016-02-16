package com.oakkub.chat.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.services.GCMNotifyService;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.GCMUtil;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.utils.UserInfoUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;

/**
 * A placeholder fragment containing a simple view.
 */
public class AddFriendFragment extends BaseFragment {

    private static final String TAG = AddFriendFragment.class.getSimpleName();

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Firebase firebaseAddFriend;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_FRIENDS)
    Firebase firebaseUserFriends;

    @Inject
    Lazy<SharedPreferences> prefs;

    private ArrayList<String> friendKeyList;
    private ArrayList<UserInfo> friendInfoList;

    private String myId;
    private UserInfo removedFriendInfo;
    private UserInfo failedFriendInfo;
    private UserInfo successFriendInfo;

    private OnAddFriendListener onAddFriendListener;

    public static AddFriendFragment newInstance(String myId) {
        Bundle args = new Bundle();
        args.putString(ARGS_MY_ID, myId);

        AddFriendFragment fragment = new AddFriendFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        onAddFriendListener = (OnAddFriendListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);
        getDataArgs();
        friendKeyList = new ArrayList<>();
        getUserFriend();
    }

    private void getDataArgs() {
        Bundle args = getArguments();

        myId = args.getString(ARGS_MY_ID);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (friendInfoList != null) {
            onAddFriendListener.onFriendListAdded(friendInfoList);
            friendInfoList = null;
        }

        if (removedFriendInfo != null) {
            onAddFriendListener.onRemovedFriend(removedFriendInfo);
            removedFriendInfo = null;
        }

        if (failedFriendInfo != null) {
            onAddFriendListener.onFriendAddedFailed(failedFriendInfo);
            failedFriendInfo = null;
        }

        if (successFriendInfo != null) {
            onAddFriendListener.onFriendAddedSuccess(successFriendInfo);
            successFriendInfo = null;
        }
    }

    private void getUserFriend() {
        firebaseUserFriends.child(myId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot children : dataSnapshot.getChildren()) {
                            friendKeyList.add(children.getKey());
                        }
                        getRecommendedFriend();
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e(TAG, firebaseError.getMessage());
                    }
                });
    }

    private void getRecommendedFriend() {
        firebaseAddFriend
                .orderByChild(FirebaseUtil.CHILD_REGISTERED_DATE)
                .limitToLast(20)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        getRecommendedFriendList(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e(TAG, firebaseError.getMessage());
                    }
                });
    }

    private void getRecommendedFriendList(DataSnapshot dataSnapshot) {
        ArrayList<UserInfo> recommendedFriendList =
                new ArrayList<>((int) dataSnapshot.getChildrenCount());

        for (DataSnapshot childrenSnapshot : dataSnapshot.getChildren()) {
            String userKey = childrenSnapshot.getKey();

            if (!userKey.equals(myId) && !isAlreadyFriend(userKey)) {
                UserInfo friendUserInfo = UserInfoUtil.get(userKey, childrenSnapshot);
                recommendedFriendList.add(friendUserInfo);
            }
        }

        reverseFriendData(recommendedFriendList);
    }

    private void reverseFriendData(ArrayList<UserInfo> recommendedFriendList) {
        if (recommendedFriendList.size() > 0) {
            Collections.reverse(recommendedFriendList);

            if (onAddFriendListener != null) {
                onAddFriendListener.onFriendListAdded(recommendedFriendList);
            } else {
                friendInfoList = recommendedFriendList;
            }
        }
    }

    private boolean isAlreadyFriend(String friendKey) {
        return friendKeyList.contains(friendKey);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        onAddFriendListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void addFriend(UserInfo friendInfo) {
        checkIfFriendExisted(friendInfo);
    }

    private void checkIfFriendExisted(final UserInfo friendInfo) {
        firebaseUserFriends.child(myId)
                .child(friendInfo.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        prepareToAddFriend(dataSnapshot, friendInfo);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e(TAG, firebaseError.getMessage());
                    }
                });
    }

    private void prepareToAddFriend(DataSnapshot dataSnapshot, UserInfo friendInfo) {
        // if exists, it means we are already friend.
        if (dataSnapshot.exists()) {
            if (onAddFriendListener != null) {
                onAddFriendListener.onRemovedFriend(friendInfo);
            } else {
                removedFriendInfo = friendInfo;
            }
        } else {
            postFriendDataToServer(friendInfo, false);
        }
    }

    private void postFriendDataToServer(final UserInfo friendInfo, boolean removeFriend) {
        // removeFriend flag use for check if we gonna addLast friend or remove friend from server
        Map<String, Object> friendKey = getFriendKey(friendInfo, removeFriend);
        firebaseUserFriends.updateChildren(friendKey, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                onAddFriendCompleted(firebaseError, friendInfo);
            }
        });
    }

    private Map<String, Object> getFriendKey(UserInfo friendUserInfo, boolean removeFriend) {
        String friendKey = friendUserInfo.getKey();

        ArrayMap<String, Object> addFriendMap = new ArrayMap<>(2);
        addFriendMap.put(TextUtil.getPath(myId, friendKey), removeFriend ? null : true);
        addFriendMap.put(TextUtil.getPath(friendKey, myId), removeFriend ? null : true);

        return addFriendMap;
    }

    private void onAddFriendCompleted(FirebaseError firebaseError, UserInfo friendInfo) {
        if (firebaseError != null) {
            if (onAddFriendListener != null) {
                onAddFriendListener.onFriendAddedFailed(friendInfo);
            } else {
                failedFriendInfo = friendInfo;
            }
            return;
        }

        if (onAddFriendListener != null) {
            onAddFriendListener.onFriendAddedSuccess(friendInfo);
        } else {
            successFriendInfo = friendInfo;
        }
        sendAddFriendNotification(friendInfo);
    }

    private void sendAddFriendNotification(UserInfo friendUserInfo) {
        String displayName = prefs.get().getString(UserInfoUtil.DISPLAY_NAME, "");

        Intent notificationService = new Intent(getActivity(), GCMNotifyService.class);
        notificationService.putExtra(GCMUtil.KEY_TO, friendUserInfo.getInstanceID());
        notificationService.putExtra(GCMUtil.DATA_TITLE,
                getString(R.string.new_friend));
        notificationService.putExtra(GCMUtil.DATA_MESSAGE,
                getString(R.string.notification_message_you_are_added_as_friend, displayName));

        getActivity().startService(notificationService);
    }

    public interface OnAddFriendListener {
        void onFriendListAdded(ArrayList<UserInfo> friendList);

        void onFriendAddedSuccess(UserInfo friendInfo);

        void onRemovedFriend(UserInfo friendInfo);

        void onFriendAddedFailed(UserInfo friendInfo);
    }

}
