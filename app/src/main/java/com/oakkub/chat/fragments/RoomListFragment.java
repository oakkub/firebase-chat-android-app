package com.oakkub.chat.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import org.magicwerk.brownies.collections.GapList;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RoomListFragment extends Fragment {

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

    private RoomListAdapter roomListAdapter;
    private GapList<String> myRoomList;

    private String myId;

    public static RoomListFragment newInstance() {

        return new RoomListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myRoomList = new GapList<>();

        AppController.getComponent(getActivity()).inject(this);

        setRetainInstance(true);

        roomListAdapter = new RoomListAdapter();
        getUserRoomsFromServer();
    }

    private void getUserRoomsFromServer() {
        myId = userRoomsFirebase.getAuth().getUid();

        userRoomsFirebase.child(myId).orderByValue()
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        Log.e("CHILD_ADD S", String.valueOf(s));
                        Log.e("CHILD_ADD", String.valueOf(dataSnapshot.getValue()));
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        Log.e("CHILD_CHANGED S", String.valueOf(s));
                        Log.e("CHILD_CHANGED", String.valueOf(dataSnapshot.getValue()));
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        Log.e("CHILD_MOVED S", String.valueOf(s));
                        Log.e("CHILD_MOVED", String.valueOf(dataSnapshot.getValue()));
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });

        userRoomsFirebase.child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getAllUserRooms(dataSnapshot);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private void getAllUserRooms(DataSnapshot dataSnapshot) {
        myRoomList.ensureCapacity((int) dataSnapshot.getChildrenCount());

        for (DataSnapshot children : dataSnapshot.getChildren()) {
            final String roomId = children.getKey();

            myRoomList.add(roomId);
            getUserRoom(roomId);
        }

    }

    private void getUserRoom(String roomId) {
        roomFirebase.child(roomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        addRoomToAdapter(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void addRoomToAdapter(DataSnapshot dataSnapshot) {
        Map<String, Object> dataSnapshotMap = (Map<String, Object>) dataSnapshot.getValue();

        Log.e("room", String.valueOf(dataSnapshot.getValue()));

        if (dataSnapshotMap.get(FirebaseUtil.CHILD_ROOM_TYPE)
                .equals(FirebaseUtil.VALUE_ROOM_TYPE_PRIVATE)) {

            Room room = dataSnapshot.getValue(Room.class);
            room.setRoomId(dataSnapshot.getKey());

            String friendId = findFriendIdFromPrivateRoom(dataSnapshot);
            getFriendInfoForPrivateRoom(friendId, room);

        } else {

        }
    }

    private void getFriendInfoForPrivateRoom(String friendId, final Room room) {

        userInfoFirebase.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                UserInfo friendInfo = dataSnapshot.getValue(UserInfo.class);

                room.setName(friendInfo.getDisplayName());
                room.setRoomImagePath(friendInfo.getProfileImageURL());

                roomListAdapter.add(room);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
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

    private void setRecyclerView() {
        DefaultItemAnimator defaultItemAnimator = AppController.getComponent(getActivity()).defaultItemAnimator();

        messageList.setHasFixedSize(true);
        messageList.setItemAnimator(defaultItemAnimator);
        messageList.setLayoutManager(new LinearLayoutManager(getActivity()));
        messageList.setAdapter(roomListAdapter);
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
