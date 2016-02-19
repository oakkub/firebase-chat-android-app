package com.oakkub.chat.fragments;

import android.content.Context;
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
import com.oakkub.chat.activities.ChatRoomActivity;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.OnScrolledEventListener;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.eventbus.EventBusNewRoom;
import com.oakkub.chat.models.eventbus.EventBusRemovedRoom;
import com.oakkub.chat.models.eventbus.EventBusRoomListEdited;
import com.oakkub.chat.models.eventbus.EventBusUpdatedRoom;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.views.adapters.RoomListAdapter;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.widgets.recyclerview.RecyclerViewScrollDirectionListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import icepick.State;

public class RoomListFragment extends BaseFragment implements OnAdapterItemClick {

    public static final String ARGS_MY_ID = "args:myId";

    private static final String ROOM_LIST_STATE = "state:roomList";
    private static final String TAG = RoomListFragment.class.getSimpleName();

    @Bind(R.id.recyclerview)
    RecyclerView roomList;

    @State
    String myId;

    private RoomListAdapter roomListAdapter;
    private OnScrolledEventListener onScrolledEventListener;

    public static RoomListFragment newInstance(String myId) {

        Bundle args = new Bundle();
        args.putString(ARGS_MY_ID, myId);

        RoomListFragment roomListFragment = new RoomListFragment();
        roomListFragment.setArguments(args);

        return roomListFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        onScrolledEventListener = (OnScrolledEventListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        getDataFromArguments(savedInstanceState);

        roomListAdapter = new RoomListAdapter(this, myId);
        EventBus.getDefault().register(this);
    }

    private void getDataFromArguments(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;

        myId = getArguments().getString(ARGS_MY_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.recyclerview, container, false);
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
    public void onDetach() {
        super.onDetach();

        onScrolledEventListener = null;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    private void setRecyclerView() {
        roomList.setItemAnimator(null);
        roomList.setHasFixedSize(true);
        roomList.setLayoutManager(new LinearLayoutManager(getActivity()));
        roomList.addOnScrollListener(new RecyclerViewScrollDirectionListener() {
            @Override
            public void onScrollUp() {
                if (onScrolledEventListener != null) {
                    onScrolledEventListener.onScrollUp();
                }
            }

            @Override
            public void onScrollDown() {
                if (onScrolledEventListener != null) {
                    onScrolledEventListener.onScrollDown();
                }
            }
        });

        roomList.setAdapter(roomListAdapter);
    }

    @Override
    public void onAdapterClick(View itemView, int position) {
        Room room = roomListAdapter.getItem(position);

        switch (room.getType()) {
            case FirebaseUtil.VALUE_ROOM_TYPE_PRIVATE:
                // private room will use room image for friend image
                // and friend id for fetching display name
                Intent privateRoomIntent = ChatRoomActivity.getIntentPrivateRoom(getActivity(), room, myId);
                startActivity(privateRoomIntent);
                break;
            case FirebaseUtil.VALUE_ROOM_TYPE_GROUP:
                Intent groupRoomIntent = ChatRoomActivity.getIntentGroupRoom(getActivity(), room, myId);
                startActivity(groupRoomIntent);
                break;
            case FirebaseUtil.VALUE_ROOM_TYPE_PUBLIC:
                Intent publicRoomIntent = ChatRoomActivity.getIntentPublicRoom(getActivity(), room, myId, true);
                startActivity(publicRoomIntent);
                break;
        }
    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        Log.e(TAG, "onAdapterLongClick: " + position);
        return false;
    }

    public void onEvent(EventBusRoomListEdited eventBusRoomListEdited) {
        if (roomListAdapter == null) return;
        roomListAdapter.replace(eventBusRoomListEdited.room);
    }

    public void onEvent(EventBusNewRoom eventBusNewRoom) {
        if (roomListAdapter == null) return;

        Room firstRoom = roomListAdapter.getFirstItem();
        Room newRoom = eventBusNewRoom.room;

        newRoom.setLatestMessageTime(eventBusNewRoom.latestActiveTime);

        if (roomListAdapter.contains(newRoom)) return;

        if (firstRoom != null) {

            for (int i = 0, size = roomListAdapter.getItemCount(); i < size; i++) {

                Room room = roomListAdapter.getItem(i);

                if (newRoom.getLatestMessageTime() > room.getLatestMessageTime()) {

                    roomListAdapter.add(i, newRoom);

                    // scroll to first position if necessary
                    if (((LinearLayoutManager) roomList.getLayoutManager())
                            .findFirstCompletelyVisibleItemPosition() < 2) {
                        roomList.scrollToPosition(0);
                    }

                    break;
                }

                if (i == (size - 1) && newRoom.getLatestMessageTime() < room.getLatestMessageTime()) {
                    roomListAdapter.addLast(newRoom);
                }
            }

        } else {
            roomListAdapter.addFirst(newRoom);
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(EventBusUpdatedRoom updatedRoom) {
        if (roomListAdapter == null) return;

        Room existingUpdatedRoom = updatedRoom.room;
        Room existingRoom = roomListAdapter.getItem(roomListAdapter.findPosition(existingUpdatedRoom));

        existingRoom.setLatestMessage(existingUpdatedRoom.getLatestMessage());
        existingRoom.setLatestMessageTime(updatedRoom.latestActiveTime);
        existingRoom.setLatestMessageUser(existingUpdatedRoom.getLatestMessageUser());

        roomListAdapter.moveItem(existingRoom, 0);
        roomListAdapter.replace(existingRoom);
    }

    public void onEvent(EventBusRemovedRoom removedRoom) {
        if (roomListAdapter == null) return;
        roomListAdapter.remove(removedRoom.room);
    }
}
