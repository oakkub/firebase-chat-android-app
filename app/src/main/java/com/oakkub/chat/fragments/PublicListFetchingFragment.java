package com.oakkub.chat.fragments;

import android.os.Bundle;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.eventbus.EventBusPublicRoom;
import com.oakkub.chat.utils.FirebaseUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;

/**
 * Created by OaKKuB on 2/2/2016.
 */
public class PublicListFetchingFragment extends BaseFragment {

    @Inject
    @Named(FirebaseUtil.NAMED_USER_PUBLIC)
    Lazy<Firebase> userPublicFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_INFO)
    Lazy<Firebase> roomInfoFirebase;

    private ArrayList<String> publicRoomsId;
    private ArrayList<Room> roomInfoList;
    private int totalRoom;
    private int totalRoomFetched;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);

        publicRoomsId = new ArrayList<>();
        roomInfoList = new ArrayList<>();
    }

    public void fetchPublicList(String myId) {
        userPublicFirebase.get().child(myId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().removeEventListener(this);
                        if (!dataSnapshot.exists()) {
                            sendPublicRoom();
                            return;
                        }

                        totalRoom = (int) dataSnapshot.getChildrenCount();
                        publicListFetching(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void publicListFetching(DataSnapshot dataSnapshot) {
        boolean hasNewData = false;

        for (DataSnapshot children : dataSnapshot.getChildren()) {

            String publicRoomKey = children.getKey();

            if (!publicRoomsId.contains(publicRoomKey)) {
                hasNewData = true;
                publicRoomsId.add(publicRoomKey);
                fetchPublicRoomInfo(publicRoomKey);
            }
        }

        if (!hasNewData) {
            sendPublicRoom();
        }
    }

    private void fetchPublicRoomInfo(String roomId) {
        roomInfoFirebase.get().child(roomId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().removeEventListener(this);
                        publicRoomInfoFetched(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void publicRoomInfoFetched(DataSnapshot dataSnapshot) {
        Room room = dataSnapshot.getValue(Room.class);
        if (room == null) return;

        room.setRoomId(dataSnapshot.getKey());

        roomInfoList.add(room);
        ++totalRoomFetched;

        if (totalRoomFetched == totalRoom) {
            sendPublicRoom();
        }
    }

    private void sendPublicRoom() {
        EventBus.getDefault().post(new EventBusPublicRoom(roomInfoList));
    }
}
