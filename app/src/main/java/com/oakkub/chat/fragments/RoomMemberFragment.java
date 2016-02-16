package com.oakkub.chat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.FirebaseUtil;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;

/**
 * Created by OaKKuB on 1/8/2016.
 */
public class RoomMemberFragment extends BaseFragment {

    private static final String ARGS_ROOM_ID = "args:roomId";

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_MEMBERS)
    Lazy<Firebase> roomMembersFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Lazy<Firebase> userInfoFirebase;

    private OnRoomMemberRequest onRoomMemberRequest;

    private ArrayList<String> memberIdList;
    private ArrayList<UserInfo> memberInfoList;

    private int totalMember;
    private int totalMemberFetched;

    private String roomId;
    private String myId;

    private boolean roomMemberSend;

    public static RoomMemberFragment newInstance(String roomId, String myId) {
        Bundle args = new Bundle();
        args.putString(ARGS_ROOM_ID, roomId);
        args.putString(ARGS_MY_ID, myId);

        RoomMemberFragment roomMemberFragment = new RoomMemberFragment();
        roomMemberFragment.setArguments(args);

        return roomMemberFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        FragmentActivity activity = getActivity();
        onRoomMemberRequest = (OnRoomMemberRequest) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);
        getDataArguments();

        memberIdList = new ArrayList<>();
        memberInfoList = new ArrayList<>();
    }

    private void getDataArguments() {
        Bundle args = getArguments();

        myId = args.getString(ARGS_MY_ID);
        roomId = args.getString(ARGS_ROOM_ID);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        fetchMemberId();

        if (roomMemberSend) {
            roomMemberSend = false;
            onRoomMemberRequest.onRoomMemberFetched(memberInfoList);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        onRoomMemberRequest = null;
    }

    private void fetchMemberId() {
        roomMembersFirebase.get().child(roomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        totalMember = (int) dataSnapshot.getChildrenCount();
                        memberIdFetched(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void memberIdFetched(DataSnapshot dataSnapshot) {
        for (DataSnapshot children : dataSnapshot.getChildren()) {
            String memberKey = children.getKey();

            if (!memberIdList.contains(memberKey)) {
                memberIdList.add(memberKey);

                fetchMemberInfo(memberKey);
            }
        }
    }

    private void fetchMemberInfo(String memberKey) {
        userInfoFirebase.get().child(memberKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        memberInfoFetched(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void memberInfoFetched(DataSnapshot dataSnapshot) {
        UserInfo memberInfo = dataSnapshot.getValue(UserInfo.class);
        memberInfo.setKey(dataSnapshot.getKey());

        memberInfoList.add(memberInfo);

        ++totalMemberFetched;

        if (totalMemberFetched == totalMember) {
            if (onRoomMemberRequest != null) {
                onRoomMemberRequest.onRoomMemberFetched(memberInfoList);
            } else {
                roomMemberSend = true;
            }
        }
    }

    public interface OnRoomMemberRequest {
        void onRoomMemberFetched(ArrayList<UserInfo> memberList);
    }

}
