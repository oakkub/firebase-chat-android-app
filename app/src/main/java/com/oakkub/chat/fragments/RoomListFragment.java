package com.oakkub.chat.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oakkub.chat.R;
import com.oakkub.chat.activities.ChatRoomActivity;
import com.oakkub.chat.activities.MainActivity;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.OnScrolledEventListener;
import com.oakkub.chat.managers.RefreshListener;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.eventbus.EventBusEmptyRoomList;
import com.oakkub.chat.models.eventbus.EventBusNewRoom;
import com.oakkub.chat.models.eventbus.EventBusOlderRoom;
import com.oakkub.chat.models.eventbus.EventBusRemovedRoom;
import com.oakkub.chat.models.eventbus.EventBusRoomListEdited;
import com.oakkub.chat.models.eventbus.EventBusRoomListLoadingMore;
import com.oakkub.chat.models.eventbus.EventBusUpdatedRoom;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.views.adapters.RoomListAdapter;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.widgets.MySwipeRefreshLayout;
import com.oakkub.chat.views.widgets.MyTextView;
import com.oakkub.chat.views.widgets.recyclerview.RecyclerViewInfiniteScrollListener;
import com.oakkub.chat.views.widgets.recyclerview.RecyclerViewScrollDirectionListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class RoomListFragment extends BaseFragment implements OnAdapterItemClick,
    SwipeRefreshLayout.OnRefreshListener {

    public static final String ARGS_MY_ID = "args:uid";

    private static final String ROOM_LIST_STATE = "state:roomList";
    private static final String TAG = RoomListFragment.class.getSimpleName();

    @Bind(R.id.swipe_refresh_progress_bar_recycler_view_layout)
    MySwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.recyclerview)
    RecyclerView roomList;

    @Bind(R.id.swipe_refresh_text_view)
    MyTextView alretTextView;

    private RoomListAdapter roomListAdapter;

    private LinearLayoutManager linearLayoutManager;
    private RecyclerViewInfiniteScrollListener infiniteScrollListener;
    private OnScrolledEventListener onScrolledEventListener;
    private RefreshListener refreshListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        onScrolledEventListener = (OnScrolledEventListener) getActivity();
        refreshListener = (RefreshListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);

        roomListAdapter = new RoomListAdapter(this, uid);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.swipe_refresh_progressbar_recyclerview, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initInstances();

        if (savedInstanceState == null) {
            swipeRefreshLayout.show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        roomListAdapter.onSaveInstanceState(ROOM_LIST_STATE, outState);
        infiniteScrollListener.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) return;

        roomListAdapter.onRestoreInstanceState(ROOM_LIST_STATE, savedInstanceState);
        infiniteScrollListener.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        onScrolledEventListener = null;
        refreshListener = null;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        if (refreshListener != null) {
            refreshListener.onRefresh(MainActivity.ROOM_LIST_FRAGMENT_TAG);
        }
    }

    private void initInstances() {
        swipeRefreshLayout.setOnRefreshListener(this);

        linearLayoutManager = new LinearLayoutManager(getActivity());

        roomList.setItemAnimator(null);
        roomList.setHasFixedSize(true);
        roomList.setLayoutManager(linearLayoutManager);
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

        infiniteScrollListener = new RecyclerViewInfiniteScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page) {
                if (roomListAdapter.getItemCount() < RoomListFetchingFragment.MESSAGE_LIMIT) return;
                final Room room = roomListAdapter.getLastItem();

                if (room != null) {
                    getActivity().getWindow().getDecorView().getHandler().post(new Runnable() {
                        @Override
                        public void run() {

                            roomListAdapter.addFooterProgressBar();
                            EventBus.getDefault().post(new EventBusRoomListLoadingMore(room.getLatestMessageTime()));
                        }
                    });
                }
            }
        };
        roomList.addOnScrollListener(infiniteScrollListener);

        roomList.setAdapter(roomListAdapter);
    }

    @Override
    public void onAdapterClick(View itemView, int position) {
        Room room = roomListAdapter.getItem(position);

        switch (room.getType()) {
            case FirebaseUtil.VALUE_ROOM_TYPE_PRIVATE:
                // private room will use room image for friend image
                // and friend id for fetching display name
                Intent privateRoomIntent = ChatRoomActivity.getIntentPrivateRoom(getActivity(), room, uid);
                startActivity(privateRoomIntent);
                break;
            case FirebaseUtil.VALUE_ROOM_TYPE_GROUP:
                Intent groupRoomIntent = ChatRoomActivity.getIntentGroupRoom(getActivity(), room);
                startActivity(groupRoomIntent);
                break;
            case FirebaseUtil.VALUE_ROOM_TYPE_PUBLIC:
                Intent publicRoomIntent = ChatRoomActivity.getIntentPublicRoom(getActivity(), room, true);
                startActivity(publicRoomIntent);
                break;
        }
    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        return false;
    }

    public void onEvent(EventBusEmptyRoomList eventBusEmptyRoomList) {
        if (roomListAdapter.isEmpty() && !eventBusEmptyRoomList.isExists) {
            alretTextView.setText(R.string.you_dont_have_messages);
            alretTextView.visible();
        } else {
            alretTextView.gone();
        }
        swipeRefreshLayout.hide();
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
                    if (linearLayoutManager.findFirstCompletelyVisibleItemPosition() < 2) {
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

    public void onEvent(EventBusOlderRoom eventBusOlderRoom) {
        roomListAdapter.removeFooter();

        if (eventBusOlderRoom.room == null || eventBusOlderRoom.latestActiveTime == -1) {
            infiniteScrollListener.noMoreData();
            return;
        }

        Room olderRoom = eventBusOlderRoom.room;
        olderRoom.setLatestMessageTime(eventBusOlderRoom.latestActiveTime);
        roomListAdapter.addLast(olderRoom);

        // enforce to load more until there is no data left.
        infiniteScrollListener.setLoadMore(true);
    }

    @SuppressWarnings("unused")
    public void onEvent(EventBusUpdatedRoom eventBusUpdatedRoom) {
        if (roomListAdapter == null) return;
        Room updatedRoom = eventBusUpdatedRoom.room;

        int existingRoomIndex = roomListAdapter.findPosition(updatedRoom);
        Room existingRoom = roomListAdapter.getItem(roomListAdapter.findPosition(updatedRoom));

        existingRoom.setLatestMessage(updatedRoom.getLatestMessage());
        existingRoom.setLatestMessageTime(eventBusUpdatedRoom.latestActiveTime);
        existingRoom.setLatestMessageUser(updatedRoom.getLatestMessageUser());

        roomListAdapter.moveItem(existingRoom, 0);
        roomListAdapter.replace(existingRoom);

        vibrateNewMessage();
        if (linearLayoutManager.findFirstCompletelyVisibleItemPosition() <= 1) {
            roomList.smoothScrollToPosition(0);
        }
    }

    private void vibrateNewMessage() {
        /*if (getActivity() != null &&
                !MyLifeCycleHandler.getCurrentActivityName()
                        .equals(ChatRoomActivity.class.getSimpleName())) {
            Vibrator vibrator = AppController.getComponent(getActivity()).vibrator();
            vibrator.vibrate(100);
        }*/
    }

    public void onEvent(EventBusRemovedRoom removedRoom) {
        if (roomListAdapter == null) return;
        roomListAdapter.remove(removedRoom.room);
    }
}
