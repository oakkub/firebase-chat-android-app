package com.oakkub.chat.managers.loaders;

import android.content.Context;
import android.content.SharedPreferences;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.PrefsUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;

/**
 * Created by OaKKuB on 2/26/2016.
 */
public class FindFriendLoader extends MyLoader<List<UserInfo>> {

    private static final String TAG = FindFriendLoader.class.getSimpleName();

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Lazy<Firebase> firebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_FRIENDS)
    Firebase userFriendsFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Firebase userInfoFirebase;

    @Inject
    SharedPreferences prefs;

    private ArrayList<UserInfo> friendInfoList;
    private String uid;
    private int totalFetched;
    private int totalFriend;

    public FindFriendLoader(Context context) {
        super(context);
        AppController.getComponent(getContext()).inject(this);
        uid = prefs.getString(PrefsUtil.PREF_UID, null);
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        findFriend();
    }

    private void findFriend() {
        userFriendsFirebase.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().removeEventListener(this);
                if (!dataSnapshot.exists()) {
                    return;
                }

                int size = (int) dataSnapshot.getChildrenCount();
                totalFriend = size;
                friendInfoList = new ArrayList<>(size);

                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    findFriendInfo(children.getKey());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void findFriendInfo(String key) {
        userInfoFirebase.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().removeEventListener(this);
                totalFetched++;

                UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                userInfo.setKey(dataSnapshot.getKey());
                friendInfoList.add(userInfo);

                if (totalFetched == totalFriend) {
                    deliverResult(friendInfoList);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
}
