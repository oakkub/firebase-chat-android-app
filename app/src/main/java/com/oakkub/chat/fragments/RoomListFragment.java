package com.oakkub.chat.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.views.adapters.RoomListAdapter;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.State;

public class RoomListFragment extends BaseFragment implements ChildEventListener {

    public static final String ARGS_MY_ID = "args:myId";

    private static final String ROOM_LIST_FRAGMENT_TAG = "tag:roomListFirebase";
    private static final String ROOM_LIST_STATE = "state:roomList";
    private static final int MESSAGE_LIMIT = 20;

    @Bind(R.id.message_list_recycler_view)
    RecyclerView messageList;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS)
    Firebase roomFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_ROOMS)
    Firebase userRoomsFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Firebase userInfoFirebase;

    @State
    ArrayList<String> myRoomList;

    @State
    String myId;

    private RoomListAdapter roomListAdapter;

    public static RoomListFragment newInstance(String myId) {

        Bundle args = new Bundle();
        args.putString(ARGS_MY_ID, myId);

        RoomListFragment roomListFragment = new RoomListFragment();
        roomListFragment.setArguments(args);

        return roomListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        addRoomListFirebaseFragment();

        roomListAdapter = new RoomListAdapter();
        saveValueInstance(savedInstanceState);
    }

    private void addRoomListFirebaseFragment() {
        FragmentManager childFragmentManager = getChildFragmentManager();
        Fragment fragment = childFragmentManager.findFragmentByTag(ROOM_LIST_FRAGMENT_TAG);

        if (fragment == null) {
            fragment = new Fragment();
            childFragmentManager.beginTransaction()
                    .add(fragment, ROOM_LIST_FRAGMENT_TAG)
                    .commit();
        }
    }

    private void saveValueInstance(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            myRoomList = new ArrayList<>(MESSAGE_LIMIT);
            myId = getArguments().getString(ARGS_MY_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_message_list, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRecyclerView();
    }

    @Override
    public void onStart() {
        super.onStart();

        fetchUserRoomsFromServer();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        roomListAdapter.onSaveInstanceState(ROOM_LIST_STATE, outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) return;

        roomListAdapter.onRestoreInstanceState(ROOM_LIST_STATE, savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();

        userRoomsFirebase.child(myId).removeEventListener(this);
    }

    private void setRecyclerView() {
        messageList.setHasFixedSize(true);
        messageList.setLayoutManager(new LinearLayoutManager(getActivity()));
        messageList.setAdapter(roomListAdapter);
    }

    private void fetchUserRoomsFromServer() {
        userRoomsFirebase.child(myId)
                .orderByValue()
                .limitToLast(MESSAGE_LIMIT)
                .addChildEventListener(this);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
        getUserRoom(dataSnapshot);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildKey) {
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

    private void getUserRoom(DataSnapshot dataSnapshot) {
        final String roomId = dataSnapshot.getKey();

        if (!myRoomList.contains(roomId)) {
            myRoomList.add(roomId);
            getUserRoom(roomId);
        }
    }

    private void getUserRoom(final String roomId) {
        roomFirebase.child(roomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        addRoomToAdapter(dataSnapshot, roomId);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void addRoomToAdapter(DataSnapshot dataSnapshot, String roomId) {
        Room room = dataSnapshot.getValue(Room.class);
        room.setRoomId(roomId);

        String friendId = findFriendIdFromPrivateRoom(dataSnapshot);
        getFriendInfoForPrivateRoom(friendId, room);
    }

    private void getFriendInfoForPrivateRoom(String friendId, final Room room) {
        // get friend info for room name and image room.
        userInfoFirebase.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserInfo friendInfo = dataSnapshot.getValue(UserInfo.class);

                room.setRoomName(friendInfo.getDisplayName());
                room.setRoomImagePath(friendInfo.getProfileImageURL());

                roomListAdapter.addFirst(room);
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

}
