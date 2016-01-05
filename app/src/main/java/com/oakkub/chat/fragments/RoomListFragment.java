package com.oakkub.chat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oakkub.chat.R;
import com.oakkub.chat.activities.PrivateChatRoomActivity;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.EventBusNewRoom;
import com.oakkub.chat.models.EventBusUpdatedRoom;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.views.adapters.RoomListAdapter;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class RoomListFragment extends BaseFragment implements OnAdapterItemClick {

    public static final String ARGS_MY_ID = "args:myId";

    private static final String ROOM_LIST_STATE = "state:roomList";
    private static final String TAG = RoomListFragment.class.getSimpleName();

    @Bind(R.id.message_list_recycler_view)
    RecyclerView messageList;

    private RoomListAdapter roomListAdapter;
    private String myId;

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

        myId = getArguments().getString(ARGS_MY_ID);
        roomListAdapter = new RoomListAdapter(this);
        EventBus.getDefault().register(this);
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
    public void onDestroy() {
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    private void setRecyclerView() {
        messageList.setItemAnimator(null);
        messageList.setHasFixedSize(true);
        messageList.setLayoutManager(new LinearLayoutManager(getActivity()));
        messageList.setAdapter(roomListAdapter);
    }

    private void setPrivateRoomIntent(Intent privateRoomIntent, Room room) {

        privateRoomIntent.putStringArrayListExtra(PrivateChatRoomActivityFragment.EXTRA_FRIEND_ID,
                new ArrayList<>(Collections.singletonList(FirebaseUtil.privateRoomFriendKey(myId, room.getRoomId()))));
        privateRoomIntent.putStringArrayListExtra(PrivateChatRoomActivityFragment.EXTRA_FRIEND_PROFILE,
                new ArrayList<>(Collections.singletonList(room.getImagePath())));
    }

    @Override
    public void onAdapterClick(View itemView, int position) {
        Room room = roomListAdapter.getItem(position);

        Intent privateRoomIntent = new Intent(getActivity(), PrivateChatRoomActivity.class);
        privateRoomIntent.putExtra(PrivateChatRoomActivityFragment.EXTRA_MY_ID, myId);
        privateRoomIntent.putExtra(PrivateChatRoomActivityFragment.EXTRA_ROOM_ID, room.getRoomId());

        if (room.getType().equals(FirebaseUtil.VALUE_ROOM_TYPE_PRIVATE)) {
            // private room will use room image for friend image
            // and friend id for fetching display name
            setPrivateRoomIntent(privateRoomIntent, room);
        } else {
            // we already have room name
            privateRoomIntent.putExtra(PrivateChatRoomActivityFragment.EXTRA_ROOM_NAME, room.getName());
            privateRoomIntent.putExtra(PrivateChatRoomActivityFragment.EXTRA_ROOM_IMAGE, room.getImagePath());
        }

        startActivity(privateRoomIntent);
    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        Log.e(TAG, "onAdapterLongClick: " + position);
        return false;
    }

    public void onEvent(EventBusNewRoom newRoom) {
        if (roomListAdapter == null) return;

        Room firstRoom = roomListAdapter.getFirstItem();

        if (firstRoom != null) {
            if (firstRoom.getLatestMessageTime() > newRoom.room.getLatestMessageTime()) {
                // if first room is the latest, then add newRoom after this room
                // group room will be fetched faster than private room,
                // cause them to be sent here faster than others.
                roomListAdapter.add(1, newRoom.room);
            } else {
                roomListAdapter.addFirst(newRoom.room);
            }
        } else {
            roomListAdapter.addFirst(newRoom.room);
        }
    }

    public void onEvent(EventBusUpdatedRoom updatedRoom) {
        if (roomListAdapter == null) return;

        Room existingUpdatedRoom = updatedRoom.room;
        if (existingUpdatedRoom.getLatestMessageTime() > roomListAdapter.getFirstItem().getLatestMessageTime()) {
            roomListAdapter.moveItem(existingUpdatedRoom, 0);
        }

        // set info of newerRoom to oldRoom
        Room oldRoom = roomListAdapter.getFirstItem();
        oldRoom.setLatestMessage(existingUpdatedRoom.getLatestMessage());
        oldRoom.setLatestMessageTime(existingUpdatedRoom.getLatestMessageTime());
        oldRoom.setLatestMessageUser(existingUpdatedRoom.getLatestMessageUser());

        roomListAdapter.replace(oldRoom);
    }
}
