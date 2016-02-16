package com.oakkub.chat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.FirebaseUtil;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by OaKKuB on 2/3/2016.
 */
public class UserInfoFetchingFragment extends BaseFragment implements ValueEventListener {

    private static final String TAG = UserInfoFetchingFragment.class.getSimpleName();

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Firebase userInfoFirebase;

    private String userId;
    private UserInfo userInfoResult;
    private OnUserInfoReceivedListener onUserInfoReceivedListener;

    public static UserInfoFetchingFragment newInstance(String userId) {
        Bundle args = new Bundle();
        args.putString(ARGS_MY_ID, userId);

        UserInfoFetchingFragment userInfoFetchingFragment = new UserInfoFetchingFragment();
        userInfoFetchingFragment.setArguments(args);

        return userInfoFetchingFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        onUserInfoReceivedListener = (OnUserInfoReceivedListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);
        getDataIntent();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (userInfoResult != null) {
            onUserInfoReceivedListener.onUserInfoReceived(userInfoResult);
            userInfoResult = null;
        }

        fetch();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onUserInfoReceivedListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        userInfoFirebase.child(userId).removeEventListener(this);
    }

    private void getDataIntent() {
        Bundle args = getArguments();
        userId = args.getString(ARGS_MY_ID);
    }

    public void fetch() {
        userInfoFirebase.child(userId).keepSynced(true);
        userInfoFirebase.child(userId).addValueEventListener(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (!dataSnapshot.exists()) return;

        UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
        userInfo.setKey(dataSnapshot.getKey());

        if (onUserInfoReceivedListener != null) {
             onUserInfoReceivedListener.onUserInfoReceived(userInfo);
        } else {
            userInfoResult = userInfo;
        }
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        Log.e(TAG, "onCancelled: " + firebaseError.getMessage());
    }

    public interface OnUserInfoReceivedListener {
        void onUserInfoReceived(UserInfo userInfo);
    }
}
