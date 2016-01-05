package com.oakkub.chat.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.EventBusFriendListInfo;
import com.oakkub.chat.models.EventBusNewMessagesFriendInfo;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.FirebaseUtil;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;
import de.greenrobot.event.EventBus;

/**
 * Created by OaKKuB on 12/9/2015.
 */
public class FriendsFetchingFragment extends Fragment {

    private static final String TAG = FriendsFetchingFragment.class.getSimpleName();

    public static final String ACTION = "action:" + TAG;
    public static final String FROM_NEW_FRIEND = "action:fromNewFriend";
    public static final String FROM_NEW_MESSAGES = "action:fromNewMessages";

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Lazy<Firebase> firebaseFriends;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_FRIENDS)
    Lazy<Firebase> firebaseUserFriends;

    private ArrayList<String> friendKeyList;
    private ArrayList<UserInfo> friendInfoList;

    private int totalFriend;
    private int totalFriendFetched;

    private String action;

    public static FriendsFetchingFragment newInstance(String action) {
        Bundle args = new Bundle();
        args.putString(ACTION, action);

        FriendsFetchingFragment friendsFetchingFragment = new FriendsFetchingFragment();
        friendsFetchingFragment.setArguments(args);

        return friendsFetchingFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);

        friendKeyList = new ArrayList<>();
        friendInfoList = new ArrayList<>();
        action = getArguments().getString(ACTION);
    }

    public void fetchUserFriends(String myId) {
        firebaseUserFriends.get().keepSynced(true);

        firebaseUserFriends.get().child(myId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        fetchFriendKey(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void fetchFriendKey(DataSnapshot dataSnapshot) {
        totalFriend = (int) dataSnapshot.getChildrenCount();

        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            String friendKey = snapshot.getKey();

            if (!friendKeyList.contains(friendKey)) {
                friendKeyList.add(friendKey);
                fetchFriendInfo(friendKey);
            }
        }
    }

    private void fetchFriendInfo(String friendKey) {
        firebaseFriends.get().keepSynced(true);

        firebaseFriends.get().child(friendKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        sendFriendToAdapter(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void sendFriendToAdapter(DataSnapshot dataSnapshot) {
        String friendKey = dataSnapshot.getKey();
        UserInfo friendInfo = initFriendInfo(dataSnapshot, friendKey);

        friendInfoList.add(friendInfo);
        ++totalFriendFetched;

        if (totalFriendFetched == totalFriend) {
            sendFriend(friendInfoList);
        }
    }

    private void sendFriend(ArrayList<UserInfo> friendInfoList) {
        switch (action) {
            case FROM_NEW_FRIEND:
                EventBus.getDefault().post(new EventBusFriendListInfo(friendInfoList));
                break;
            case FROM_NEW_MESSAGES:
                EventBus.getDefault().post(new EventBusNewMessagesFriendInfo(friendInfoList));
                break;
        }
    }

    private UserInfo initFriendInfo(DataSnapshot dataSnapshot, String friendKey) {
        UserInfo friendUserInfo = dataSnapshot.getValue(UserInfo.class);

        friendUserInfo.setUserKey(friendKey);
        friendUserInfo.setType(UserInfo.FRIEND);

        return friendUserInfo;
    }

    public interface FriendRequestListener {

        void onNewFriend();

    }

}
