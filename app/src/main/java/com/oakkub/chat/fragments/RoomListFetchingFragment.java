package com.oakkub.chat.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.EventBusNewRoom;
import com.oakkub.chat.models.EventBusUpdatedRoom;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.FirebaseUtil;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import de.greenrobot.event.EventBus;

/**
 * Created by OaKKuB on 12/6/2015.
 */
public class RoomListFetchingFragment extends Fragment implements ChildEventListener {

    private static final int MESSAGE_LIMIT = 20;
    private static final String ARGS_MY_ID = "args:myId";
    private static final String TAG = RoomListFetchingFragment.class.getSimpleName();

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_INFO)
    Firebase roomFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Firebase userInfoFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_ROOMS)
    Firebase userRoomsFirebase;

    private ArrayList<String> myRoomList;
    private ArrayList<Room> roomList;
    private ArrayList<Room> changeRoomList;
    private String myId;

    public static RoomListFetchingFragment newInstance(String myId) {
        Bundle args = new Bundle();
        args.putString(ARGS_MY_ID, myId);

        RoomListFetchingFragment roomListFetchingFragment = new RoomListFetchingFragment();
        roomListFetchingFragment.setArguments(args);

        return roomListFetchingFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);

        Bundle args = getArguments();
        myId = args.getString(ARGS_MY_ID);

        myRoomList = new ArrayList<>(MESSAGE_LIMIT);
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchUserRoomsFromServer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
        userRoomsFirebase.child(myId).removeEventListener(this);
    }

    private void fetchUserRoomsFromServer() {
        userRoomsFirebase.keepSynced(true);
        userRoomsFirebase.child(myId)
                .orderByValue()
                .limitToLast(MESSAGE_LIMIT)
                .addChildEventListener(this);
    }

    private void fetchRoomIdUser(DataSnapshot dataSnapshot) {
        final String roomId = dataSnapshot.getKey();
        Log.d(TAG, "fetchRoomIdUser: " + roomId);
        if (!myRoomList.contains(roomId)) {
            myRoomList.add(roomId);
            fetchUserRoomById(roomId);
        }
    }

    private void fetchUserRoomById(final String roomId) {
        roomFirebase.child(roomId)
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

        // check type of room
        // if room type == 'private', it means this room is private room 1 - 1 chat
        // we gonna get name of friend to be use as room name
        if (room.getType().equals(FirebaseUtil.VALUE_ROOM_TYPE_PRIVATE)) {

            String friendId = findFriendIdFromPrivateRoom(dataSnapshot);
            fetchFriendInfoForPrivateRoom(friendId, room);
        } else {
            EventBus.getDefault().post(new EventBusNewRoom(room));
        }
    }

    private void fetchFriendInfoForPrivateRoom(String friendId, final Room room) {
        // get room name and room image from friend info
        userInfoFirebase.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserInfo friendInfo = dataSnapshot.getValue(UserInfo.class);

                room.setRoomName(friendInfo.getDisplayName());
                room.setRoomImagePath(friendInfo.getProfileImageURL());

                EventBus.getDefault().post(new EventBusNewRoom(room));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
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
        roomFirebase.child(roomKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Room room = dataSnapshot.getValue(Room.class);
                        room.setRoomId(roomKey);

                        EventBus.getDefault().post(new EventBusUpdatedRoom(room));
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
        fetchRoomIdUser(dataSnapshot);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildKey) {
        fetchUpdatedRoom(dataSnapshot.getKey());
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
