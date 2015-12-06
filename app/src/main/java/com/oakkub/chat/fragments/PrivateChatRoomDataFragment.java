package com.oakkub.chat.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.firebase.client.Firebase;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.views.adapters.ChatListAdapter;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by OaKKuB on 11/25/2015.
 */
public class PrivateChatRoomDataFragment extends Fragment {

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Firebase firebase;

    private ChatListAdapter chatListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

    }
}
