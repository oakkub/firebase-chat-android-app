package com.oakkub.chat.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.views.adapters.RoomListAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RoomListFragment extends BaseFragment implements RoomListFetchingFragment.OnRoomListChangeListener {

    public static final String ARGS_MY_ID = "args:myId";

    private static final String ROOM_LIST_FRAGMENT_TAG = "tag:roomListFirebase";
    private static final String ROOM_LIST_STATE = "state:roomList";
    private static final String TAG = RoomListFragment.class.getSimpleName();

    @Bind(R.id.message_list_recycler_view)
    RecyclerView messageList;

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

        roomListAdapter = new RoomListAdapter();
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
        addRoomListFirebaseFragment();
        setRecyclerView();
    }

    private void addRoomListFirebaseFragment() {
        FragmentManager childFragmentManager = getChildFragmentManager();

        if (childFragmentManager.findFragmentByTag(ROOM_LIST_FRAGMENT_TAG) == null) {

            String myId = getArguments().getString(ARGS_MY_ID);

            RoomListFetchingFragment roomListFetchingFragment = RoomListFetchingFragment.newInstance(myId);
            childFragmentManager.beginTransaction()
                    .add(roomListFetchingFragment, ROOM_LIST_FRAGMENT_TAG)
                    .commit();
        }
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

    private void setRecyclerView() {
        messageList.setHasFixedSize(true);
        messageList.setLayoutManager(new LinearLayoutManager(getActivity()));
        messageList.setAdapter(roomListAdapter);
    }

    @Override
    public void onNewRoom(Room room) {
        Log.e(TAG, "onNewRoom: " + room.getLatestMessage());
        roomListAdapter.addFirst(room);
    }

    @Override
    public void onRoomChange(Room room) {
        Log.e(TAG, "onRoomChange: " + room.getLatestMessage());
        if (room.getLatestMessageTime() > roomListAdapter.getFirstItem().getLatestMessageTime()) {
            roomListAdapter.moveItem(room, 0);
        }

        Room updatedRoom = roomListAdapter.getItem(0);
        updatedRoom.setRoomId(room.getRoomId());
        updatedRoom.setLatestMessage(room.getLatestMessage());
        updatedRoom.setLatestMessageTime(room.getLatestMessageTime());
        updatedRoom.setLatestMessageUser(room.getLatestMessageUser());

        roomListAdapter.replace(updatedRoom);
    }
}
