package com.oakkub.chat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.utils.FirebaseUtil;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by OaKKuB on 2/10/2016.
 */
public class RoomAdminAuthenticationFragment extends BaseFragment {

    private static final String ARGS_ROOM_ID = "args:roomId";

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_ADMIN_MEMBERS)
    Firebase adminRoomFirebase;

    private String myId;
    private String roomId;

    private boolean isAuthenticated;
    private boolean dataExists;

    private OnRoomAdminAuthenticationListener authenticationListener;

    public static RoomAdminAuthenticationFragment newInstance(String myId, String roomId) {
        Bundle args = new Bundle();
        args.putString(ARGS_MY_ID, myId);
        args.putString(ARGS_ROOM_ID, roomId);

        RoomAdminAuthenticationFragment fragment = new RoomAdminAuthenticationFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        authenticationListener = (OnRoomAdminAuthenticationListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        getDataArgs();
        setRetainInstance(true);
    }

    private void getDataArgs() {
        Bundle args = getArguments();

        myId = args.getString(ARGS_MY_ID);
        roomId = args.getString(ARGS_ROOM_ID);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (isAuthenticated) {
            authenticationListener.onAuthenticated(dataExists);
            isAuthenticated = false;
            dataExists = false;
        }

        authenticate();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        authenticationListener = null;
    }

    private void authenticate() {
        adminRoomFirebase.child(roomId).child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean exists = dataSnapshot.exists();

                if (authenticationListener != null) {
                    authenticationListener.onAuthenticated(exists);
                } else {
                    isAuthenticated = true;
                    dataExists = exists;
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public interface OnRoomAdminAuthenticationListener {
        void onAuthenticated(boolean isAuthenticated);
    }
}
