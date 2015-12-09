package com.oakkub.chat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.FirebaseUtil;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;
import icepick.State;

/**
 * Created by OaKKuB on 12/6/2015.
 */
public class RoomListFetchingFragment extends BaseFragment implements ChildEventListener {

    private static final int MESSAGE_LIMIT = 20;
    private static final String ARGS_MY_ID = "args:myId";
    private static final String TAG = RoomListFetchingFragment.class.getSimpleName();

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS)
    Lazy<Firebase> roomFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Lazy<Firebase> userInfoFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_ROOMS)
    Firebase userRoomsFirebase;

    @State
    ArrayList<String> myRoomList;

    @State
    ArrayList<Room> newRoomList;

    @State
    ArrayList<Room> changeRoomList;

    @State
    String myId;

    private OnRoomListChangeListener onRoomListChangeListener;

    public static RoomListFetchingFragment newInstance(String myId) {
        Bundle args = new Bundle();
        args.putString(ARGS_MY_ID, myId);

        RoomListFetchingFragment roomListFetchingFragment = new RoomListFetchingFragment();
        roomListFetchingFragment.setArguments(args);

        return roomListFetchingFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        onRoomListChangeListener = (OnRoomListChangeListener) getParentFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        if (savedInstanceState == null) {
            Bundle args = getArguments();
            myId = args.getString(ARGS_MY_ID);

            myRoomList = new ArrayList<>(MESSAGE_LIMIT);
        }

        Log.e(TAG, "onCreate: " + String.valueOf(myId));
        fetchUserRoomsFromServer();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (changeRoomList != null) {
            if (!changeRoomList.isEmpty()) {
                for (int i = 0, size = changeRoomList.size(); i < size; i++) {
                    onRoomListChangeListener.onRoomChange(changeRoomList.get(i));
                }
            }

            changeRoomList = null;
        }

        if (newRoomList != null) {
            if (!newRoomList.isEmpty()) {
                for (int i = 0, size = newRoomList.size(); i < size; i++) {
                    onRoomListChangeListener.onNewRoom(newRoomList.get(i));
                }
            }

            newRoomList = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.e(TAG, "onDetach: ");
        onRoomListChangeListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
        userRoomsFirebase.child(myId).removeEventListener(this);
    }

    private boolean isRoomListChangeListenerAvailable() {
        return onRoomListChangeListener != null;
    }

    private void fetchUserRoomsFromServer() {
        userRoomsFirebase.child(myId)
                .orderByValue()
                .limitToLast(MESSAGE_LIMIT)
                .addChildEventListener(this);
    }

    private void fetchRoomIdUser(DataSnapshot dataSnapshot) {
        final String roomId = dataSnapshot.getKey();

        if (!myRoomList.contains(roomId)) {
            myRoomList.add(roomId);
            fetchUserRoomById(roomId);
        }
    }

    private void fetchUserRoomById(final String roomId) {
        roomFirebase.get().child(roomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        initRoom(dataSnapshot, roomId);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void initRoom(DataSnapshot dataSnapshot, String roomId) {
        Room room = dataSnapshot.getValue(Room.class);
        room.setRoomId(roomId);

        String friendId = findFriendIdFromPrivateRoom(dataSnapshot);
        fetchFriendInfoForPrivateRoom(friendId, room);
    }

    private void fetchFriendInfoForPrivateRoom(String friendId, final Room room) {
        // get friend info for room name and image room.
        userInfoFirebase.get().child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserInfo friendInfo = dataSnapshot.getValue(UserInfo.class);

                room.setRoomName(friendInfo.getDisplayName());
                room.setRoomImagePath(friendInfo.getProfileImageURL());

                fetchRoomSuccess(room);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private void fetchRoomSuccess(Room room) {
        if (isRoomListChangeListenerAvailable()) {
            onRoomListChangeListener.onNewRoom(room);
        } else {
            if (newRoomList == null) {
                newRoomList = new ArrayList<>();
            }
            newRoomList.add(room);
        }
    }

    private String findFriendIdFromPrivateRoom(DataSnapshot dataSnapshot) {
        // find image for private room
        final String[] key = dataSnapshot.getKey().split("_");
        final String[] users = new String[]{key[1], key[2]};

        for (String userId : users) {
            if (!userId.equals(myId)) {
                return userId;
            }
        }
        return "";
    }

    private void fetchUpdatedRoom(final String roomKey) {
        roomFirebase.get().child(roomKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Room room = dataSnapshot.getValue(Room.class);
                        room.setRoomId(roomKey);

                        updatedRoomFetched(room);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void updatedRoomFetched(Room room) {
        if (isRoomListChangeListenerAvailable()) {
            onRoomListChangeListener.onRoomChange(room);
        } else {
            if (changeRoomList == null) {
                changeRoomList = new ArrayList<>();
            }

            changeRoomList.add(room);
        }
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
        fetchRoomIdUser(dataSnapshot);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildKey) {
        final String roomKey = dataSnapshot.getKey();
        fetchUpdatedRoom(roomKey);
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String previousChildKey) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }

    public interface OnRoomListChangeListener {
        void onNewRoom(Room room);

        void onRoomChange(Room room);
    }

}
