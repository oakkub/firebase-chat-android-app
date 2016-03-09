package com.oakkub.chat.fragments;

import android.os.Bundle;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.eventbus.EventBusGroupRoom;
import com.oakkub.chat.utils.FirebaseUtil;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;
import de.greenrobot.event.EventBus;

/**
 * Created by OaKKuB on 1/5/2016.
 */
public class GroupListFetchingFragment extends BaseFragment {

    @Inject
    @Named(FirebaseUtil.NAMED_USER_GROUPS)
    Lazy<Firebase> userGroupsFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_INFO)
    Lazy<Firebase> roomInfoFirebase;

    private int totalRoom;
    private int totalRoomFetched;

    private ArrayList<String> groupRoomsId;
    private ArrayList<Room> groupRoomsList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);

        groupRoomsId = new ArrayList<>();
        groupRoomsList = new ArrayList<>();
    }

    public void fetchGroupList(String myId) {
        userGroupsFirebase.get().child(myId).keepSynced(true);
        userGroupsFirebase.get().child(myId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().removeEventListener(this);
                        if (!dataSnapshot.exists()) {
                            sendGroupRoom();
                            return;
                        }

                        totalRoom = (int) dataSnapshot.getChildrenCount();
                        groupRoomFetching(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void groupRoomFetching(DataSnapshot dataSnapshot) {
        for (DataSnapshot children : dataSnapshot.getChildren()) {

            String groupRoomKey = children.getKey();

            if (!groupRoomsId.contains(groupRoomKey)) {
                groupRoomsId.add(groupRoomKey);
                fetchGroupInfo(groupRoomKey);
            }
        }
    }

    private void fetchGroupInfo(String groupRoomKey) {
        roomInfoFirebase.get().child(groupRoomKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        groupRoomInfoFetched(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void groupRoomInfoFetched(DataSnapshot dataSnapshot) {
        Room room = dataSnapshot.getValue(Room.class);
        room.setRoomId(dataSnapshot.getKey());

        groupRoomsList.add(room);

        ++totalRoomFetched;

        if (totalRoomFetched == totalRoom) {
            sendGroupRoom();
        }
    }

    private void sendGroupRoom() {
        EventBus.getDefault().post(new EventBusGroupRoom(groupRoomsList));
    }

}
