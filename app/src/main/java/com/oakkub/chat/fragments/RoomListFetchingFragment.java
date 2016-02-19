package com.oakkub.chat.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.SparseArray;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.models.eventbus.EventBusNewRoom;
import com.oakkub.chat.models.eventbus.EventBusRemovedRoom;
import com.oakkub.chat.models.eventbus.EventBusUpdatedRoom;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.RoomUtil;

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

    private SparseArray<Long> latestRoomActiveTimeList;
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

        roomList = new ArrayList<>();
        latestRoomActiveTimeList = new SparseArray<>();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        myId = args.getString(ARGS_MY_ID);
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchUserRoomsFromServer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

        latestRoomActiveTimeList.put(roomId.hashCode(), dataSnapshot.getValue(Long.class));
        fetchUserRoomById(roomId);
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

            String friendId = RoomUtil.findFriendIdFromPrivateRoom(myId, room.getRoomId());
            fetchFriendInfoForPrivateRoom(friendId, room);
        } else {
            addRoom(room);
            EventBus.getDefault().post(new EventBusNewRoom(room, latestRoomActiveTimeList.get(room.hashCode())));
        }
    }

    private void fetchFriendInfoForPrivateRoom(String friendId, final Room room) {
        // get room name and room image from friend info
        userInfoFirebase.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserInfo friendInfo = dataSnapshot.getValue(UserInfo.class);
                room.setName(friendInfo.getDisplayName());
                room.setImagePath(friendInfo.getProfileImageURL());

                addRoom(room);

                EventBus.getDefault().post(new EventBusNewRoom(room, latestRoomActiveTimeList.get(room.hashCode())));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private void fetchUpdatedRoom(final String roomKey) {
        roomFirebase.child(roomKey)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) return;

                        Room room = dataSnapshot.getValue(Room.class);
                        room.setRoomId(roomKey);

                        if (roomList.contains(room)) {
                            EventBus.getDefault().post(new EventBusUpdatedRoom(room, latestRoomActiveTimeList.get(room.hashCode())));
                        } else {
                            EventBus.getDefault().post(new EventBusNewRoom(room, latestRoomActiveTimeList.get(room.hashCode())));
                            addRoom(room);
                        }
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
        String roomKey = dataSnapshot.getKey();
        latestRoomActiveTimeList.put(roomKey.hashCode(), dataSnapshot.getValue(Long.class));
        fetchUpdatedRoom(roomKey);
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        String roomKey = dataSnapshot.getKey();
        for (int i = 0, size = roomList.size(); i < size; i++) {
            Room room = roomList.get(i);
            if (room.getRoomId().equals(roomKey)) {
                EventBus.getDefault().post(new EventBusRemovedRoom(room));
                roomList.remove(i);
                break;
            }
        }
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String previousChildKey) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }

    private void addRoom(Room room) {
        if (!roomList.contains(room)) {
            roomList.add(room);
        }
    }

    public interface OnRoomListChangeListener {
        void onNewRoom(Room room);
        void onRoomChange(Room room);
    }

}
