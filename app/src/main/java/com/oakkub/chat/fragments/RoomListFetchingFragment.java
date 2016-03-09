package com.oakkub.chat.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.models.eventbus.EventBusEmptyRoomList;
import com.oakkub.chat.models.eventbus.EventBusNewRoom;
import com.oakkub.chat.models.eventbus.EventBusOlderRoom;
import com.oakkub.chat.models.eventbus.EventBusRemovedRoom;
import com.oakkub.chat.models.eventbus.EventBusRoomListLoadingMore;
import com.oakkub.chat.models.eventbus.EventBusUpdatedRoom;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.RoomUtil;
import com.oakkub.chat.views.widgets.MyToast;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import de.greenrobot.event.EventBus;

/**
 * Created by OaKKuB on 12/6/2015.
 */
    public class RoomListFetchingFragment extends BaseFragment implements ChildEventListener {

    public static final int MESSAGE_LIMIT = 20;
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

    private boolean isFromLoadingMore;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);

        roomList = new ArrayList<>();
        latestRoomActiveTimeList = new SparseArray<>();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchRoomList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        userRoomsFirebase.child(uid).removeEventListener(this);
    }

    public void fetchRoomList() {
        checkIfRoomExists();
        fetchUserRoomsFromServer();
    }

    private void checkIfRoomExists() {
        userRoomsFirebase.child(uid).limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                EventBus.getDefault().post(new EventBusEmptyRoomList(dataSnapshot.exists()));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
    }

    private void fetchUserRoomsFromServer() {
        userRoomsFirebase.keepSynced(true);
        userRoomsFirebase.child(uid)
                .orderByValue()
                .limitToLast(MESSAGE_LIMIT)
                .addChildEventListener(this);
    }

    public void onEvent(EventBusRoomListLoadingMore eventBusRoomListLoadingMore) {
        fetchOlderUserRoomsFromServer(eventBusRoomListLoadingMore.oldestTime);
    }

    private void sendEmptyData() {
        EventBus.getDefault().post(new EventBusOlderRoom(null, -1));
    }

    private void fetchOlderUserRoomsFromServer(long when) {
        userRoomsFirebase.child(uid)
                .orderByValue()
                .limitToLast(MESSAGE_LIMIT)
                .endAt(when)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressWarnings("UnusedAssignment")
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().removeEventListener(this);

                        if (!dataSnapshot.exists()) {
                            sendEmptyData();
                            return;
                        }

                        isFromLoadingMore = true;

                        int size = (int) dataSnapshot.getChildrenCount();
                        ArrayList<DataSnapshot> dataSnapshotList = new ArrayList<>(size);
                        for (DataSnapshot children : dataSnapshot.getChildren()) {
                            dataSnapshotList.add(children);
                        }

                        dataSnapshotList.remove(dataSnapshotList.size() - 1);
                        for (int i = 0, sizeList = dataSnapshotList.size(); i < sizeList; i++) {
                            fetchRoomIdUser(dataSnapshotList.get(i));
                        }

                        if (size < MESSAGE_LIMIT) {
                            MyToast.make("no more data").show();
                            sendEmptyData();
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void fetchRoomIdUser(DataSnapshot dataSnapshot) {
        String roomId = dataSnapshot.getKey();

        latestRoomActiveTimeList.put(roomId.hashCode(), dataSnapshot.getValue(Long.class));
        fetchUserRoomById(roomId);
    }

    private void fetchUserRoomById(final String roomId) {
        roomFirebase.child(roomId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().removeEventListener(this);
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
        if (room.getType().equals(RoomUtil.PRIVATE_TYPE)) {

            String friendId = RoomUtil.findFriendIdFromPrivateRoom(uid, room.getRoomId());
            fetchFriendInfoForPrivateRoom(friendId, room);
        } else {
            addRoom(room);
            sendData(room, latestRoomActiveTimeList.get(room.hashCode()));
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
                sendData(room, latestRoomActiveTimeList.get(room.hashCode()));
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
                        long latestActiveTime = latestRoomActiveTimeList.get(room.hashCode());

                        if (roomList.contains(room)) {
                            EventBus.getDefault().post(new EventBusUpdatedRoom(room,
                                    latestActiveTime));
                        } else {
                            sendData(room, latestActiveTime);
                            addRoom(room);
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void sendData(Room room, long latestActiveTime) {
        if (isFromLoadingMore) {
            EventBus.getDefault().post(new EventBusOlderRoom(room, latestActiveTime));
        } else {
            EventBus.getDefault().post(new EventBusNewRoom(room, latestActiveTime));
        }
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
        isFromLoadingMore = false;
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
                roomList.remove(i);
                EventBus.getDefault().post(new EventBusRemovedRoom(room));
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

}
